package com.funnelback.publicui.search.lifecycle.inputoutput;

import static com.funnelback.common.config.DefaultValues.ModernUI.EXTRA_SEARCH_TIMEOUT_MS;
import static com.funnelback.common.config.Keys.ModernUI.EXTRA_SEARCH_TIMEOUT;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.funnelback.config.keys.Keys;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.inputoutput.extrasearch.ExecutorHelper;
import com.funnelback.publicui.search.lifecycle.inputoutput.extrasearch.LimitedCPUUsageExecutorHelper;
import com.funnelback.publicui.search.lifecycle.inputoutput.extrasearch.PassThroughExecutorHelper;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.springmvc.web.interceptor.Log4j2ThreadContextInterceptor;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
/**
 * <p>Executes the extra searches on the input phase using the questions
 * in {@link SearchTransaction#getExtraSearchesQuestions()} and submitting
 * them to a {@link TaskExecutor}.</p>
 * 
 * <p>Wait for the extra searches to complete on the output phase.</p>
 */
@Component
@Log4j2
public class ExtraSearchesExecutor implements InputProcessor, OutputProcessor {

    @Autowired @Setter
    private SearchTransactionProcessor transactionProcessor;

    @Autowired
    private TaskExecutor executor;

    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (searchTransaction != null && searchTransaction.getExtraSearchesQuestions().size() > 0) {

            final SearchUser user = getSearchUser(searchTransaction);
            
            LimitedCPUUsageExecutorHelper executorHelper = new LimitedCPUUsageExecutorHelper(executor,
                searchTransaction.getQuestion().getCurrentProfileConfig());
            
            for (final Entry<String, SearchQuestion> entry : searchTransaction.getExtraSearchesQuestions().entrySet()) {
                submitExtraSearchTaskIfNotAlreadySubmitted(searchTransaction, 
                    entry.getKey(), 
                    entry.getValue(), 
                    user, 
                    executorHelper);
            }
        }
    }
    
    private SearchUser getSearchUser(SearchTransaction searchTransaction) {
        return (searchTransaction.getSession() != null)
            ? searchTransaction.getSession().getSearchUser()
            : null;
    }
    
    private boolean hasExtraSearchBeenSubmitted(SearchTransaction searchTransaction, String extraSearchName) {
        return searchTransaction.getExtraSearches().containsKey(extraSearchName) 
            || searchTransaction.getExtraSearchesTasks().containsKey(extraSearchName);
    }
    
    private void submitExtraSearchTaskIfNotAlreadySubmitted(SearchTransaction searchTransaction, 
        String extraSearchName, 
        SearchQuestion extraSearchQuestion,
        SearchUser user,
        ExecutorHelper executorHelper) {
        
        // Don't run the same extra search twice.
        if(hasExtraSearchBeenSubmitted(searchTransaction, extraSearchName)) {
            return;
        }
        
        extraSearchQuestion.setQuestionType(SearchQuestionType.EXTRA_SEARCH);
        
        Callable<SearchTransaction> callable = makeCallable(searchTransaction, 
                extraSearchName, 
                extraSearchQuestion, 
                user, 
                Log4j2ThreadContextInterceptor.threadContextValuesSetByLog4j2Interceptor());
        
        log.trace("Submitting extra search '" + extraSearchName + "'");
        
        // We will wait no longer than the total amount of time left for extra searches. This is probably not required
        // however as long as the timeout is set sensibly if something goes wrong we will eventually stop waiting.
        executorHelper.submit(callable, extraSearchName, getTimeToWaitForExtraSearch(searchTransaction))
            .ifPresent(task -> searchTransaction.getExtraSearchesTasks().put(extraSearchName, task));
        
    }
    
    Callable<SearchTransaction> makeCallable(SearchTransaction searchTransaction, 
            String extraSearchName, 
            SearchQuestion extraSearchQuestion, 
            SearchUser user,
            Map<String, String> threadContextValues) {
        return new Callable<SearchTransaction>() {
            @Override
            public SearchTransaction call() throws Exception {
                try (WithThreadContextValues threadContext = new WithThreadContextValues(threadContextValues)){
                    StopWatch sw = new StopWatch();
                    try {
                        sw.start();
                        return transactionProcessor.process(extraSearchQuestion, user);
                    } finally {
                        sw.stop();
                        // We know exactly how much time was spent.
                        searchTransaction.getExtraSearchesAproxTimeSpent().addAndGet(sw.getTime());
                        log.debug("Extra search '" + extraSearchName + "' took " + sw.toString());
                    }
                }
                
            }
        };
    }
    
    private class WithThreadContextValues implements AutoCloseable {
        private final Map<String, String> values;
        
        public WithThreadContextValues(Map<String, String> values) {
            this.values = values;
            values.forEach(ThreadContext::put);
        }
        
        @Override
        public void close() {
            values.keySet().forEach(ThreadContext::remove);
        }
    }

    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (searchTransaction != null && searchTransaction.getExtraSearchesTasks().size() > 0) {
            // Wait for all pending extra searches task to complete,
            // and fill {@link #extraSearches}.
            for (final Entry<String, FutureTask<SearchTransaction>> entry : searchTransaction.getExtraSearchesTasks().entrySet()) {
                waitForExtraSearch(searchTransaction, entry.getKey(), entry.getValue());
            }
        }
    }
    
    void waitForExtraSearch(SearchTransaction searchTransaction, 
            String extraSearchName,
            FutureTask<SearchTransaction> extraSearchFuture) {
        
        
        long extraSearchesWaitTimeout = getTimeToWaitForExtraSearch(searchTransaction);
        
        SearchTransaction extraSearchSt = null;
        try {
            extraSearchSt = extraSearchFuture.get(extraSearchesWaitTimeout, TimeUnit.MILLISECONDS);
            searchTransaction.getExtraSearches().put(extraSearchName, extraSearchSt);
        } catch (TimeoutException te) {
            // Try to kill the extra search, this should kill padre.
            extraSearchFuture.cancel(true);
            // Tell the user that not all extra searches ran.
            searchTransaction.setAnyExtraSearchesIncomplete(true);
            if(extraSearchesWaitTimeout > 0L) {
                // Only log this if it we gave the extra search some time to finish.
                log.error("Timeout waiting " + extraSearchesWaitTimeout + "ms for extra search '" + extraSearchName + "'."
                        + "Consider raising 'extra.searches.timeout'.", te);
            }
        } catch (Exception e) {
            searchTransaction.setAnyExtraSearchesIncomplete(true);
            log.error("Unable to wait results for extra search '" + extraSearchName + "'", e);
        } finally {
            
            Optional.ofNullable(extraSearchSt)
                .map(SearchTransaction::getError)
                .ifPresent(e -> searchTransaction.setAnyExtraSearchesIncomplete(true));
            
        }
    }
    
    long getTimeToWaitForExtraSearch(SearchTransaction searchTransaction) {
        long extraSearchesWaitTimeout = searchTransaction.getQuestion().getCollection().getConfiguration()
            .valueAsLong(EXTRA_SEARCH_TIMEOUT, EXTRA_SEARCH_TIMEOUT_MS);
        long extraSearchTimeLeft = searchTransaction.getQuestion().getCurrentProfileConfig()
                .get(Keys.FrontEndKeys.ModernUI.EXTRA_SEARCH_TOTAL_TIMEOUT)
            - searchTransaction.getExtraSearchesAproxTimeSpent().get();
        
        if(extraSearchTimeLeft <= 0L) {
            log.warn("Time spent in extra searches exceeded.");
            return 0L;
        }
        return Long.min(extraSearchesWaitTimeout, extraSearchTimeLeft);
        
    }
    
    public void checkIfAllExtraSearchTimeHasBeenUsed(SearchTransaction searchTransaction) {
        
    }
    
    public Optional<SearchTransaction> getAndMaybeWaitForExtraSearch(SearchTransaction mainSearchTransaction, String extraSearchName) {

        // Ensure the search has been submitted
        if(mainSearchTransaction.getExtraSearchesQuestions().containsKey(extraSearchName)) {
            submitExtraSearchTaskIfNotAlreadySubmitted(mainSearchTransaction, extraSearchName, 
                mainSearchTransaction.getExtraSearchesQuestions().get(extraSearchName), getSearchUser(mainSearchTransaction),
                new PassThroughExecutorHelper(executor));
        }
        
        // If the search has not completed yet, wait for it.
        if(mainSearchTransaction.getExtraSearchesTasks().containsKey(extraSearchName)) {
            waitForExtraSearch(mainSearchTransaction, extraSearchName, mainSearchTransaction.getExtraSearchesTasks().get(extraSearchName));
        }
        
        // Now return the resulting extra search if any.
        return Optional.ofNullable(mainSearchTransaction.getExtraSearches().get(extraSearchName));
    }

    @Override
    public String getId() {
        return this.getClass().getSimpleName();
    }

}
