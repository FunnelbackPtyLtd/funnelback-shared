package com.funnelback.publicui.streamedresults;

import static com.funnelback.common.padre.QueryProcessorOptionKeys.NUM_RANKS;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.START_RANK;
import static lombok.AccessLevel.PACKAGE;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.funnelback.common.Reference;
import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingExceptionPacketSizeTooBig;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.utils.PadreOptionsForSpeed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Process a query (by default) getting every result by paging over the result list.
 * 
 * <p>This class runs a a query many times so that we can travers every result. This class
 * takes care of setting num_ranks to a value that wont cause a OOM but doesn't cuse 
 * unnecessary extra calls to padre-sw. See runOnEachPage</p>
 *
 */
@Log4j2
public class PagedQuery {

    private SearchQuestion baseSearchQuestion;
    @Getter(PACKAGE) private final int numDocsWanted; 
    @Getter(PACKAGE) private final int initialStartRank;
    @Getter(PACKAGE) private final boolean disableOptimisation;
    
    
    // For a 5M doc collection at 35MB it took 6minutes at 10MB it took over an hour
    // higher is better but smaller helps avoid OOMs.
    // This is the limit that will be used to enforce the padre packet size. 
    // Note we re-calculate each request as the amount of memory the JVM has may change 
    @Setter(PACKAGE) private int maxResponseSize = getMaxResponseSizeBasedOnJVM();
    
    // Based on our previouse page we will AIM for out next page to be this size.
    @Setter(PACKAGE) private int targetResponseSize = calculateTargetRersponseSize(maxResponseSize);
    
    /**
     * 
     * @param searchQuestion
     * @param disableOptimisation
     */
    public PagedQuery(SearchQuestion searchQuestion, boolean disableOptimisation) {
        this.disableOptimisation = disableOptimisation;
        baseSearchQuestion = searchQuestion;
        
        //TODO TEST these.
        // Either go up to the set num ranks or return all documents.
        numDocsWanted = getLastIntFromMap(baseSearchQuestion.getRawInputParameters(), NUM_RANKS)
            .filter(i -> i > 0).orElse(Integer.MAX_VALUE);
        
        // Either start at the given start rank or start at the first result.
        initialStartRank = getLastIntFromMap(baseSearchQuestion.getRawInputParameters(), START_RANK)
            .filter(i -> i > 0).orElse(1);
        
        // We override these
        baseSearchQuestion.getRawInputParameters().remove(NUM_RANKS);
        baseSearchQuestion.getRawInputParameters().remove(START_RANK);
    }
    
    
    
    
    /**
     * Executes the query using the given SearchExecutor passing the resulting SearchTransaction to the onPage Consumer.
     * 
     * @param searchExecutor When given a SearchQuestio this should execute the search and return 
     * the resulting SearchTransaction
     * @param onPage A Consumer which will be given the resulting SearchTransaction (as a reference)
     * once we are done with it. The consumer may null out the value in the reference to allow
     * earlier GC.
     */
    public void runOnEachPage(Function<SearchQuestion, SearchTransaction> searchExecutor,
        Consumer<Reference<SearchTransaction>> onPage) {
        
        Optional<StartRankAndNumRank> startRankAndNumRank = Optional.of(StartRankAndNumRank.builder()
            .startRank(initialStartRank)
            .numRank(100) // Start small and work our way up.
            .build());
        while(startRankAndNumRank.isPresent()) {
            startRankAndNumRank = runSearchAndGetStartRankAndNumRankForNextSearch(startRankAndNumRank.get().getStartRank(),
                startRankAndNumRank.get().getNumRank(), 
                searchExecutor, 
                onPage);
        }
    }
    
    @AllArgsConstructor
    @Builder
    static class StartRankAndNumRank {
        @Getter private final int startRank;
        @Getter private final int numRank;
    }
    
    /**
     * 
     * @param startRank
     * @param numRank
     * @param searchExecutor
     * @param onPage
     * @return if present the StartRank and NumRank that should be used for the next search if empty()
     * signals that no more seaches should be done as we have all results.
     */
    Optional<StartRankAndNumRank> runSearchAndGetStartRankAndNumRankForNextSearch(int startRank, int numRank, 
        Function<SearchQuestion, SearchTransaction> searchExecutor,
        Consumer<Reference<SearchTransaction>> onPage) {
        // Check if we have reached the num ranks requested.
        Optional<Integer> optNumRanksToUse = numRanksForNextRequestThatDoesNotExceedDocsWanted(startRank, numRank);
        if(!optNumRanksToUse.isPresent()) {
            return Optional.empty(); // We have all the documents we need.
        }
        numRank = optNumRanksToUse.get();
        
        SearchQuestion questionCutDown = createQuestionForNextRequest(startRank, numRank);
        
        // Run the query.
        SearchTransaction transaction = searchExecutor.apply(questionCutDown);
        
        // Inspect possible errors.
        if(hasRecoverableError(numRank, transaction)) {
            return Optional.of(new StartRankAndNumRank(startRank, calculateNumRanksOnError(numRank, transaction)));
        }
        
        
        boolean shouldFetchMoreResults = shouldFetchMoreResults(startRank, numRank, transaction);
        
        // Set the start rank to the start of the next page.
        startRank += numRank;
        
        // Alter the size of the next page.
        numRank = calculateTargetNumRanks(transaction, numRank); //TODO reverse this
        
        // We make sure that only the onPage function has a reference to the SearchTransaction
        // as we are desperately trying to save memory.
        Reference<SearchTransaction> transactionReference = new Reference<SearchTransaction>(transaction);
        transaction=null;
        
        // Pass on the transaction
        onPage.accept(transactionReference);
        
        if(shouldFetchMoreResults) {
            return Optional.ofNullable(new StartRankAndNumRank(startRank, numRank));
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * Works out if we need to make more requests based on the search just ran.
     * 
     * @param startRank the start_rank that was used on the given transaction.
     * @param numRank the num_rsnks that was used on the given transaction.
     * @param transaction of the search just run.
     * @return true if we have more results to get.
     */
    boolean shouldFetchMoreResults(int startRank, int numRank, SearchTransaction transaction) {
        if(Optional.ofNullable(transaction)
            .map(SearchTransaction::getResponse)
            .map(SearchResponse::getResultPacket).isPresent()) {
            
            ResultPacket resultPacket = transaction.getResponse().getResultPacket();
            if(Optional.of(resultPacket).map(ResultPacket::getResults).isPresent() &&
                Optional.of(resultPacket).map(ResultPacket::getResultsSummary).map(ResultsSummary::getTotalMatching).isPresent()) {
                
                log.debug("Padre packet size was: {}MB for {} results", 
                    transaction.getResponse().getUntruncatedPadreOutputSize()/1024/1024,
                    transaction.getResponse().getResultPacket().getResults().size());
                
                return transaction.getResponse().getResultPacket().getResultsSummary().getTotalMatching() > startRank -1 + numRank;
            }
            
        }
        
        // No ResultPacket means we have no more results.
        return false;
    }
    
    /**
     * Creates the SearchQuestion to be run on the next question.
     * @param startRank
     * @param numRank
     * @return
     */
    SearchQuestion createQuestionForNextRequest(int startRank, int numRank) {
        SearchQuestion questionCutDown = SearchQuestionBinder.makeCloneOfReleventFields(baseSearchQuestion);
        questionCutDown.setQuestionType(baseSearchQuestion.getQuestionType());
        
        // Ensure we don't get packets that are too big.
        questionCutDown.setMaxPadrePacketSize(Optional.of(maxResponseSize));
        
        // Override any num_rank and start_rank values set.
        questionCutDown.getRawInputParameters().put(QueryProcessorOptionKeys.NUM_RANKS, new String[]{numRank + ""});
        questionCutDown.getRawInputParameters().put(QueryProcessorOptionKeys.START_RANK, new String[]{startRank + ""});
        
        // Set the daat depth to as deep as the largest request otherwise the sort order
        // at daat=1000 may differ from daat=2000
        questionCutDown.getRawInputParameters().put(QueryProcessorOptionKeys.DAAT, new String[]{getDaatDepth() + ""});
        
        // Max out the daat timeout
        questionCutDown.getDynamicQueryProcessorOptions().add("-" + QueryProcessorOptionKeys.DAAT_TIMEOUT + "=3600");
        
        // Apply optimisations if enabled
        applyOptimisations(questionCutDown, new PadreOptionsForSpeed());
        
        return questionCutDown;
    }
    
    /**
     * Calculate the DAAT depth required.
     * 
     * <p>We don't just blindly set 10M as it may be the case we only want the first
     * n matching but going 10M deep is to expensive e.g. from DLS.</p>
     * 
     * @return
     */
    int getDaatDepth() {
        // Set these to int here as this method depends on the values being ints.
        int numRanks = this.numDocsWanted;
        int startRank = this.initialStartRank - 1;
        
        long depth = ((long) numRanks) + ((long) startRank);
        
        return (int) Long.min(depth, Integer.MAX_VALUE); 
    }
    
    /**
     * Applies optimisations to the SearchQuestion if enabled.
     * 
     * @param searchQuestion
     * @param padreOptionsForSpeed
     */
    void applyOptimisations(SearchQuestion searchQuestion, PadreOptionsForSpeed padreOptionsForSpeed) {
        if(!this.disableOptimisation) {
            Stream.concat(padreOptionsForSpeed.getOptionsThatDoNotAffectResultSetAsPairs().stream(), 
                Stream.of(padreOptionsForSpeed.getHighServiceVolumeOptionAsPair()))
            .forEach(option -> {
                // This is interesting we want to override things like RMCF and count g bits when facets are enabled.
                // but we still need faceted nav to cut down the query.
                if(!searchQuestion.getRawInputParameters().containsKey(option.getOption())) {
                    searchQuestion.getRawInputParameters().put(option.getOption(), new String[]{option.getValue()});
                }
            });
        }
    }
    
    
    /**
     * Works out how many num_ranks we should get for the next requst. Or returns empty() if
     * no more requests should be made.
     * 
     * @param startRankOfNextReqest
     * @param numRankOfNextReqest
     * @return
     */
    Optional<Integer> numRanksForNextRequestThatDoesNotExceedDocsWanted(int startRankOfNextReqest, int numRankOfNextReqest) {
        int numDocsLeftToGet = numDocsWanted - startRankOfNextReqest + initialStartRank;
        if(numDocsLeftToGet <= 0) {
            return Optional.empty();
        }
        return Optional.of(Math.min(numDocsLeftToGet, numRankOfNextReqest));
    }
    
    /**
     * Calculates the num_ranks to set to get a padre result packet of size TARGET_RESPONSE_SIZE
     * 
     * This is based on the size of the previous page. We should probably change this to
     * be based on the size of all previous pages.
     * 
     * @param transaction
     * @param currentNumRanks
     * @return
     */
    int calculateTargetNumRanks(SearchTransaction transaction, int currentNumRanks) {
        
        if(Optional.ofNullable(transaction)
            .map(SearchTransaction::getResponse).isPresent()) {
            
            
            
            SearchResponse searchResponse = transaction.getResponse();
            
            Integer untruncatedPadreSize = searchResponse.getUntruncatedPadreOutputSize();
            if(untruncatedPadreSize == null || untruncatedPadreSize == 0) {
                return currentNumRanks;
            }
            
            int numResults = Optional.of(searchResponse).map(SearchResponse::getResultPacket).map(ResultPacket::getResults).map(List::size)
                .orElse(currentNumRanks); 
            
            numResults = Integer.max(numResults, 1);
        
            float sizePerDoc = untruncatedPadreSize / numResults;
            
            sizePerDoc = Float.max(sizePerDoc, 1);
            
            float approxNumDocsForTargetResponseSize = targetResponseSize / sizePerDoc;
            
            int numRank = Integer.max((int) approxNumDocsForTargetResponseSize, 1);
            
            return numRank;
        }
        
        return currentNumRanks;
    }
    
    /**
     * Checks the search transaction for errors and checks if we can recover from the error.
     * 
     * @param numRank
     * @param transaction
     * @return true if an error occured that we can recover from. returns false if no error occured.
     * @throws RuntimeException thrown when an error occured that we can not recover from.
     */
    boolean hasRecoverableError(int numRank, SearchTransaction transaction) throws RuntimeException {
        if(transaction.getError() != null) {
            if(padrePacketWasTooLarge(transaction.getError().getAdditionalData())) {
                if(numRank == 1) {
                    String s = "A single document is bigger than the maximum PADRE result packet size we"
                        + " are willing to accept, try increasing the amount of JVM memory.";
                    log.warn(s);
                    throw new RuntimeException(s);
                }
                // The padre packet was too large we can re-cover from this.
                return true;
            } else {
                throw new RuntimeException("Hard error occurred", transaction.getError().getAdditionalData());
            }
        }
        return false;
    }
    
    /**
     * Calculates the num_ranks we should use on the next request if something went wrong
     * last week.
     * 
     * @param numRank
     * @param transaction
     * @return
     */
    int calculateNumRanksOnError(int numRank, SearchTransaction transaction) {
        // Drop the num ranks as we are hitting some limits.
        // We will take numRank/2 to ensure that we can always eventually get to a small request.
        return Integer.min(numRank/2, calculateTargetNumRanks(transaction, numRank));
    }
    
    /**
     * Returns true if the Throwable is one that tells us we reached some
     * soft limit where we need to backoff.
     * 
     * @param e
     * @return
     */
    boolean padrePacketWasTooLarge(Throwable e) {
        while(e != null) {
            if(e instanceof PadreForkingExceptionPacketSizeTooBig) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }
    
    /**
     * Gets the last value from the array at the given key then tries to parse that as int.
     * 
     * <p>If the key is missing, array is empty, last value is not an int, 
     * empty() is returned</p>
     * 
     * @param map
     * @param key
     * @return
     */
    static Optional<Integer> getLastIntFromMap(Map<String, String[]> map, String key) {
        return Optional.ofNullable(map.get(key))
        .filter(a -> a.length > 0)
        .map(a -> a[a.length-1]) // get the last like PADRE would
        .map(PagedQuery::parseInt)
        .filter(Optional::isPresent)
        .map(Optional::get);
    }
    
    /**
     * Attempts to parse an int returning empty() if it could not be parsed or the int represented by the string.
     *  
     * @param s
     * @return
     */
    static Optional<Integer> parseInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Computes the maximum padre packet size we will use based on the JVMs max memory
     * 
     * <p>It tries to take into account how inefficent we are with the padre packet as well
     * as how many concurrent requests we want to be able to support. It is dynamic so setting
     * a larger JVM heap will </p>
     * @return
     */
    private int getMaxResponseSizeBasedOnJVM() {
        return getMaxResponseSizeBasedOnJVM(Runtime.getRuntime().maxMemory());
    }
    
    int getMaxResponseSizeBasedOnJVM(final long JVMmemory) {
        final int inefficencyFactory = 3; // This is how inefficient we are with the padre packet we get:
                                        // 3 from storing the padre output in a byte[] (note that worst case for this is 3
                                        // because of ByteArrayoutputStream doubling of buf + creating the array). We get 2 from Java
                                        // strings being twice the size of their UTF-8 counterparts + 1 for the array
        final int minGarenteedConcurrentRequests = 4;
        
        final long memoryWeCanUseEstimate = (long) (JVMmemory * 0.9);
        
        long maxPadrePacketSize = memoryWeCanUseEstimate / (minGarenteedConcurrentRequests * inefficencyFactory);
        
        // Don't go under 5MB
        if(maxPadrePacketSize < 5 * 1024 * 1024) {
            maxPadrePacketSize = 5 * 1024 * 1024;
        }
        
        if(maxPadrePacketSize >= (long) Integer.MAX_VALUE) {
            maxPadrePacketSize = Integer.MAX_VALUE;
        }
        
        
        log.debug("Max padre packet size this can handle is: ~" + maxPadrePacketSize/1024/1024 + "MB");
        
        return (int) maxPadrePacketSize;
    }
    
    /**
     * Calculates the target padre response size based on the maximum responise size allowed.
     * 
     * <p>Each time we make a request we look at the number of bytes padre returned and how many results
     * and use that to work out how many results we should request next time so that get about
     * the target response size. The target responise size is smaller than the maxResponseSize
     * because the maxResponseSize is a hard limit that if reached causes the results of the query to be thrown
     * away.</p>
     * @param maxResponseSize
     * @return
     */
    int calculateTargetRersponseSize(int maxResponseSize) {
        return (int) (maxResponseSize * 0.8);
    }
    
}
