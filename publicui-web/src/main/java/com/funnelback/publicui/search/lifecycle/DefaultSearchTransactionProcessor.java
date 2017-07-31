package com.funnelback.publicui.search.lifecycle;

import java.util.List;

import javax.annotation.Resource;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.util.StopWatch;

import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchError;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
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

    public SearchTransaction process(SearchQuestion question, SearchUser user) {
        SearchTransaction transaction = new SearchTransaction(question, new SearchResponse());
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
            log.catching(ipe);
            transaction.setError(new SearchError(SearchError.Reason.InputProcessorError, ipe));
        } catch (DataFetchException dfe) {
            log.catching(dfe);
            transaction.setError(new SearchError(SearchError.Reason.DataFetchError, dfe));
        } catch (OutputProcessorException ope) {
            log.catching(ope);
            transaction.setError(new SearchError(SearchError.Reason.OutputProcessorError, ope));
        } catch (Exception e) {
            e.printStackTrace();
            log.catching(e);
            transaction.setError(new SearchError(SearchError.Reason.Unknown, e));
        }
        
        return transaction;
    }
    
}
