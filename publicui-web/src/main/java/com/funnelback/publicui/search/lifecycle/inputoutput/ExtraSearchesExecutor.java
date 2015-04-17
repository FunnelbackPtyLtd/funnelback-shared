package com.funnelback.publicui.search.lifecycle.inputoutput;

import java.util.Map.Entry;
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

            final SearchUser user = (searchTransaction.getSession() != null)
                ? searchTransaction.getSession().getSearchUser()
                : null;
            
            for (final Entry<String, SearchQuestion> entry : searchTransaction.getExtraSearchesQuestions().entrySet()) {
                
                entry.getValue().setQuestionType(SearchQuestionType.EXTRA_SEARCH);
                FutureTask<SearchTransaction> task = new FutureTask<SearchTransaction>(
                        new Callable<SearchTransaction>() {
                            @Override
                            public SearchTransaction call() throws Exception {
                                StopWatch sw = new StopWatch();
                                try {
                                    sw.start();
                                    return transactionProcessor.process(entry.getValue(), user);
                                } finally {
                                    sw.stop();
                                    log.debug("Extra search '" + entry.getKey() + "' took " + sw.toString());
                                }
                            }
                        });

                searchTransaction.getExtraSearchesTasks().put(entry.getKey(), task);
                log.trace("Submitting extra search '" + entry.getKey() + "'");
                executor.execute(task);
            }
        }
    }

    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (searchTransaction != null && searchTransaction.getExtraSearchesTasks().size() > 0) {
            // Wait for all pending extra searches task to complete,
            // and fill {@link #extraSearches}.
            long extraSearchesWaitTimeout = searchTransaction.getQuestion().getCollection().getConfiguration()
                .valueAsLong(Keys.ModernUI.EXTRA_SEARCH_TIMEOUT, DefaultValues.ModernUI.EXTRA_SEARCH_TIMEOUT_MS);

            for (final Entry<String, FutureTask<SearchTransaction>> entry : searchTransaction.getExtraSearchesTasks().entrySet()) {
                try {
                    searchTransaction.getExtraSearches().put(entry.getKey(), entry.getValue().get(extraSearchesWaitTimeout, TimeUnit.MILLISECONDS));
                } catch (TimeoutException te) {
                    log.error("Timeout waiting " + extraSearchesWaitTimeout + "ms for extra search '" + entry.getKey() + "'."
                            + "Consider raising 'extra.searches.timeout'.", te);
                } catch (Exception e) {
                    log.error("Unable to wait results for extra search '" + entry.getKey() + "'", e);
                }
            }
        }
    }

    @Override
    public String getId() {
        return this.getClass().getSimpleName();
    }

}
