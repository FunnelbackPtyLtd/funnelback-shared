package com.funnelback.publicui.search.lifecycle.inputoutput;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

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

    @Autowired
    private SearchTransactionProcessor transactionProcessor;

    @Autowired
    private TaskExecutor executor;

    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (searchTransaction != null && searchTransaction.getExtraSearchesQuestions().size() > 0) {

            final SearchUser user = getSearchUser(searchTransaction);
            
            for (final Entry<String, SearchQuestion> entry : searchTransaction.getExtraSearchesQuestions().entrySet()) {
                submitExtraSearchTaskIfNotAlreadySubmitted(searchTransaction, entry.getKey(), entry.getValue(), user);
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
        SearchUser user) {
        
        // Don't run the same extra search twice.
        if(hasExtraSearchBeenSubmitted(searchTransaction, extraSearchName)) {
            return;
        }
        
        extraSearchQuestion.setQuestionType(SearchQuestionType.EXTRA_SEARCH);
        FutureTask<SearchTransaction> task = new FutureTask<SearchTransaction>(
                new Callable<SearchTransaction>() {
                    @Override
                    public SearchTransaction call() throws Exception {
                        StopWatch sw = new StopWatch();
                        try {
                            sw.start();
                            return transactionProcessor.process(extraSearchQuestion, user);
                        } finally {
                            sw.stop();
                            log.debug("Extra search '" + extraSearchName + "' took " + sw.toString());
                        }
                    }
                });

        searchTransaction.getExtraSearchesTasks().put(extraSearchName, task);
        log.trace("Submitting extra search '" + extraSearchName + "'");
        executor.execute(task);
    }

    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (searchTransaction != null && searchTransaction.getExtraSearchesTasks().size() > 0) {
            for (final Entry<String, FutureTask<SearchTransaction>> entry : searchTransaction.getExtraSearchesTasks().entrySet()) {
                waitForExtraSearch(searchTransaction, entry.getKey(), entry.getValue());
            }
        }
    }
    
    private void waitForExtraSearch(SearchTransaction searchTransaction, 
            String extraSearchName,
            FutureTask<SearchTransaction> extraSearchFuture) {
        // Wait for all pending extra searches task to complete,
        // and fill {@link #extraSearches}.
        long extraSearchesWaitTimeout = searchTransaction.getQuestion().getCollection().getConfiguration()
            .valueAsLong(Keys.ModernUI.EXTRA_SEARCH_TIMEOUT, DefaultValues.ModernUI.EXTRA_SEARCH_TIMEOUT_MS);
        
        try {
            //TODO revert to MS
            searchTransaction.getExtraSearches().put(extraSearchName, extraSearchFuture.get(extraSearchesWaitTimeout, TimeUnit.HOURS));
        } catch (TimeoutException te) {
            log.error("Timeout waiting " + extraSearchesWaitTimeout + "ms for extra search '" + extraSearchName + "'."
                    + "Consider raising 'extra.searches.timeout'.", te);
        } catch (Exception e) {
            log.error("Unable to wait results for extra search '" + extraSearchName + "'", e);
        }
        
    }
    
    public Optional<SearchTransaction> getAndMaybeWaitForExtraSearch(SearchTransaction mainSearchTransaction, String extraSearchName) {

        // Ensure the search has been submitted
        if(mainSearchTransaction.getExtraSearchesQuestions().containsKey(extraSearchName)) {
            submitExtraSearchTaskIfNotAlreadySubmitted(mainSearchTransaction, extraSearchName, 
                mainSearchTransaction.getExtraSearchesQuestions().get(extraSearchName), getSearchUser(mainSearchTransaction));
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
