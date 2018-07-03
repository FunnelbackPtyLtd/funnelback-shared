package com.funnelback.publicui.search.lifecycle;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchError;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.springmvc.utils.web.RequestParameterValueUtils;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Default {@link SearchTransactionProcessor} implementations
 * that run the configured {@link InputProcessor}s, {@link DataFetcher}s
 * and {@link OutputProcessor}s.
 */
@Log4j2
public class DefaultSearchTransactionProcessor implements SearchTransactionProcessor {

    private static final String INPUT = "input";
    private static final String OUTPUT = "output";
    private static final String DATAFETCH = "datafetch";
    private static final String RENDER = "render";
    
    // Can't use @Autowired for those 3 one otherwise
    // Spring will automatically construct a Set with all existing
    // implementations of InputProcessor, DataFetcher, OutputProcessor
    // ----
    @Resource(name="inputFlow")
    @Setter private List<InputProcessor> inputFlow;
    
    @Resource(name="dataFetchers")
    @Setter private List<DataFetcher> dataFetchers;

    @Resource(name="outputFlow")
    @Setter private List<OutputProcessor> outputFlow;
    // ----
    
    @Autowired @Setter
    private I18n i18n;

    public SearchTransaction process(SearchQuestion question, SearchUser user, Optional<String> extraSearchName) {
        SearchTransaction transaction = new SearchTransaction(question, new SearchResponse());
        transaction.setExtraSearchName(extraSearchName);
        
        if (user != null) {
            transaction.setSession(new SearchSession(user));
        }
        
        try {
            // This may be run in a separate thread if it's an extra search. Make sure the logging
            // context contain the right information
            ThreadContext.put(RequestParameterValueUtils.COLLECTION, question.getCollection().getId());
            ThreadContext.put(RequestParameterValueUtils.PROFILE, question.getProfile());
            
            // Record the time taken by each processor
            StopWatch sw = new StopWatch();
            
            for (InputProcessor processor : inputFlow) {
                sw.start(INPUT+":"+processor.getId());
                processor.processInput(transaction);
                sw.stop();
            }

            for (DataFetcher fetcher : dataFetchers) {
                sw.start(DATAFETCH+":"+fetcher.getId());
                fetcher.fetchData(transaction);
                sw.stop();
            }
            
            transaction.getResponse().setPerformanceMetrics(sw);

            for (OutputProcessor processor : outputFlow) {
                sw.start(OUTPUT+":"+processor.getId());
                processor.processOutput(transaction);
                sw.stop();
            }
            //We should not make changes to the transaction as the will be changes that can not be seen within the
            //post process hook script.
            
            // Start a counter that will be stopped in
            // the FreeMarker side
            sw.start(OUTPUT+":"+RENDER);
            
            
            
        } catch (InputProcessorException ipe) {
            recordException(transaction, ipe, SearchError.Reason.InputProcessorError, extraSearchName);
        } catch (DataFetchException dfe) {
            recordException(transaction, dfe, SearchError.Reason.DataFetchError, extraSearchName);
            transaction.setError(new SearchError(SearchError.Reason.DataFetchError, dfe));
        } catch (OutputProcessorException ope) {
            recordException(transaction, ope, SearchError.Reason.OutputProcessorError, extraSearchName);
            transaction.setError(new SearchError(SearchError.Reason.OutputProcessorError, ope));
        } catch (Exception e) {
            recordException(transaction, e, SearchError.Reason.Unknown, extraSearchName);
        }
        
        return transaction;
    }
    
    public void recordException(SearchTransaction transaction, Exception exception, SearchError.Reason reason, Optional<String> extraSearchName) {
        transaction.setError(new SearchError(reason, exception));
        SearchQuestionType questionType = Optional.ofNullable(transaction).map(SearchTransaction::getQuestion).map(SearchQuestion::getQuestionType)
            .orElse(null);
        String questionTypeAsString = Optional.ofNullable(questionType).map(t -> t.toString()).orElse("UNKNOWN");
        if(extraSearchName.isPresent()) {
            if(SearchQuestionType.FACETED_NAVIGATION_EXTRA_SEARCH == questionType) {
                log.error(i18n.tr("transaction-lifecycle.extra-search.internal.error", 
                                        extraSearchName.get(), 
                                        questionTypeAsString, 
                                        FrontEndKeys.ModernUi.REMOVE_INTERNAL_EXTRA_SEARCHES.getKey()), 
                           exception);
            } else {
                log.error(i18n.tr("transaction-lifecycle.extra-search.error", extraSearchName.get(), questionTypeAsString), exception);
            }
        } else {
            log.error(i18n.tr("transaction-lifecycle.main-search.error", questionTypeAsString), exception);
        }
        
    }
    
}
