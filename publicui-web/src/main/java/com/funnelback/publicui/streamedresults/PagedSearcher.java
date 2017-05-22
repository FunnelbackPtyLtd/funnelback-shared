package com.funnelback.publicui.streamedresults;

import static com.funnelback.common.padre.QueryProcessorOptionKeys.NUM_RANKS;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.START_RANK;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.funnelback.common.Reference;
import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingExceptionPacketSizeTooBig;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.utils.PadreOptionsForSpeed;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PagedSearcher {

    private SearchQuestion baseSearchQuestion;
    private final int maxNumRanks; 
    private final int initialStartRank;
    private final boolean disableOptimisation;
    
    
    // For a 5M doc collection at 35MB it took 6minutes at 10MB it took over an hour
    // higher is better but smaller helps avoid OOMs.
    // This is the limit that will be used to enforce the padre packet size. 
    private final int MAX_RESPONSE_SIZE = getMaxResponseSizeBasedOnJVM();
    
    // Based on our previouse page we will AIM for out next page to be this size.
    private final int TARGET_RESPONSE_SIZE = (int) (MAX_RESPONSE_SIZE * 0.8);
    
    /**
     * Computes the maximum padre packet size we will use based on the JVMs max memory
     * 
     * <p>It tries to take into account how inefficent we are with the padre packet as well
     * as how many concurrent requests we want to be able to support. It is dynamic so setting
     * a larger JVM heap will </p>
     * @return
     */
    private final int getMaxResponseSizeBasedOnJVM() {
        final long JVMmemory = Runtime.getRuntime().maxMemory();
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
    
    
    public PagedSearcher(SearchQuestion searchQuestion, boolean disableOptimisation) {
        this.disableOptimisation = disableOptimisation;
        baseSearchQuestion = searchQuestion;
        
        // Either go up to the set num ranks or return all documents.
        maxNumRanks = getIntFromMap(baseSearchQuestion.getRawInputParameters(), NUM_RANKS)
            .filter(i -> i > 0).orElse(Integer.MAX_VALUE);
        
        // Either start at the given start rank or start at the first result.
        initialStartRank = getIntFromMap(baseSearchQuestion.getRawInputParameters(), START_RANK)
            .filter(i -> i > 0).orElse(1);
        
        // We override these
        baseSearchQuestion.getRawInputParameters().remove(NUM_RANKS);
        baseSearchQuestion.getRawInputParameters().remove(START_RANK);
    }
    
    public void runOnEachPage(Function<SearchQuestion, SearchTransaction> searchExecutor,
        Consumer<Reference<SearchTransaction>> onPage) {
        int startRank = initialStartRank;
        int numRank = 100; // Start low and work our way up.
        
        boolean tryForMore = true;
        while(tryForMore) {
            
            // Check if we have reached the num ranks requested.
            if(startRank - initialStartRank + numRank > maxNumRanks) {
                numRank = maxNumRanks - startRank + initialStartRank;
                if(numRank <= 0) {
                    break;
                }
            }
            
            SearchQuestion questionCutDown = SearchQuestionBinder.makeCloneOfReleventFields(baseSearchQuestion);
            questionCutDown.setMaxPadrePacketSize(Optional.of(MAX_RESPONSE_SIZE));
            
            questionCutDown.getRawInputParameters().put(QueryProcessorOptionKeys.NUM_RANKS, new String[]{numRank + ""});
            questionCutDown.getRawInputParameters().put(QueryProcessorOptionKeys.START_RANK, new String[]{startRank + ""});
            
            // We always must go as deep as possible
            // TODO allow setting of daat depth by the user or set this to as small as value as possible?
            questionCutDown.getRawInputParameters().put(QueryProcessorOptionKeys.DAAT, new String[]{"10000000"});
            questionCutDown.getDynamicQueryProcessorOptions().add(QueryProcessorOptionKeys.DAAT_TIMEOUT + "=3600");
            
            if(!this.disableOptimisation) {
                PadreOptionsForSpeed padreOptionsForSpeed = new PadreOptionsForSpeed();
                
                //TODO set these via RMCF
                questionCutDown.getDynamicQueryProcessorOptions().addAll(padreOptionsForSpeed.getOptionsThatDoNotAffectResultSet());
                questionCutDown.getDynamicQueryProcessorOptions().add(padreOptionsForSpeed.getHighServiceVolumeOption());
                
                
                // We will also disable rmcf even if it is enabled with facets later on as this is the most expensive of
                // all operations and the one must
                if(questionCutDown.getRawInputParameters().get(QueryProcessorOptionKeys.RMCF) != null) {
                    questionCutDown.getRawInputParameters().put(QueryProcessorOptionKeys.RMCF, new String[]{"[]"});
                }
            }
            
            
            
            long time = System.currentTimeMillis();
            SearchTransaction transaction = searchExecutor.apply(questionCutDown);
            
            if(transaction.getError() != null) {
                if(padrePacketWasTooLarge(transaction.getError().getAdditionalData())) {
                    if(numRank == 1) {
                        String s = "A single document could not be fetched within the defined soft limits, either increase"
                            + " the soft limits or accept that this request can not be done on your machine.";
                        log.warn(s);
                        throw new RuntimeException(s);
                    }
                    // Drop the num ranks as we are hitting some limits.
                    // We will take numRank/2 to ensure that we can always eventually get to a small request.
                    numRank = Integer.min(numRank/2, calculateTargetNumRanks(transaction, numRank));
                    // and try again.
                    continue;
                } else {
                    
                    throw new RuntimeException("Hard error occured");
                }
            }
            
            
            System.out.println(System.currentTimeMillis()-time);
            
            if(Optional.ofNullable(transaction.getResponse()).map(SearchResponse::getResultPacket).isPresent()) {
                tryForMore = transaction.getResponse().getResultPacket().getResultsSummary().getTotalMatching()> startRank -1 + numRank;
                System.out.println(transaction.getResponse().getResultPacket().getResults().size());
                System.out.println(transaction.getResponse().getUntruncatedPadreOutputSize()/1024/1024 + " MB");
            } else {
                // No ResultPacket means we have no more results.
                tryForMore = false;
            }
            
            // Set the start rank to the start of the next page.
            startRank += numRank;
            
            // Alter the size of the next page.
            numRank = calculateTargetNumRanks(transaction, numRank);
            
            // We make sure that only the onPage function has a reference to the SearchTransaction
            // as we are desperately trying to save memory.
            Reference<SearchTransaction> transactionReference = new Reference<SearchTransaction>(transaction);
            transaction=null;
            
            // Pass on the transaction
            onPage.accept(transactionReference);
            
        }
        
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
    public int calculateTargetNumRanks(SearchTransaction transaction, int currentNumRanks) {
        
        if(Optional.ofNullable(transaction)
            .map(SearchTransaction::getResponse).isPresent()) {
            
            
            
            SearchResponse searchResponse = transaction.getResponse();
            
            if(searchResponse.getUntruncatedPadreOutputSize() == null) {
                return currentNumRanks;
            }
            
            int numResults = Optional.of(searchResponse).map(SearchResponse::getResultPacket).map(ResultPacket::getResults).map(List::size)
                .orElse(currentNumRanks); 
            
            numResults = Integer.max(numResults, 1);
        
            float sizePerDoc = transaction.getResponse().getUntruncatedPadreOutputSize() / numResults;
            
            sizePerDoc = Float.max(sizePerDoc, 1);
            
            float approxNumDocsForTargetResponseSize = TARGET_RESPONSE_SIZE / sizePerDoc;
            
            int numRank = Integer.max((int) approxNumDocsForTargetResponseSize, 1);
            
            return numRank;
        }
        
        return currentNumRanks;
    }
    
    /**
     * Returns true if the Throwable is one that tells us we reached some
     * soft limit where we need to backoff.
     * 
     * @param e
     * @return
     */
    public boolean padrePacketWasTooLarge(Throwable e) {
        while(e != null) {
            if(e instanceof PadreForkingExceptionPacketSizeTooBig) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }
    
    public static <T> T getAndRemove(Reference<T> reference) {
        try {
            return reference.getValue();
        } finally {
            reference.setValue(null);
        }
    }
    
    private static Optional<Integer> getIntFromMap(Map<String, String[]> map, String key) {
        return Optional.ofNullable(map.get(key))
        .map(a -> a[a.length-1]) // get the last like padre would
        .map(PagedSearcher::parseInt)
        .filter(Optional::isPresent)
        .map(Optional::get);
    }
    
    private static Optional<Integer> parseInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
}
