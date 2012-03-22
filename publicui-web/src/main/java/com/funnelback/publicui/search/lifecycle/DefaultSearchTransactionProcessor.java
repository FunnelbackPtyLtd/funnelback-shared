package com.funnelback.publicui.search.lifecycle;

import java.util.List;

import javax.annotation.Resource;

import lombok.extern.log4j.Log4j;

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

/**
 * Default {@link SearchTransactionProcessor} implementations
 * that run the configured {@link InputProcessor}s, {@link DataFetcher}s
 * and {@link OutputProcessor}s.
 */
@Log4j
public class DefaultSearchTransactionProcessor implements SearchTransactionProcessor {

	// Can't use @Autowired for those 3 one otherwise
	// Spring will automatically construct a Set with all existing
	// implementations of InputProcessor, DataFetcher, OutputProcessor
	// ----
	@Resource(name="inputFlow")
	private List<InputProcessor> inputFlow;
	
	@Resource(name="dataFetchers")
	private List<DataFetcher> dataFetchers;

	@Resource(name="outputFlow")
	private List<OutputProcessor> outputFlow;
	// ----

	public SearchTransaction process(SearchQuestion question) {
		SearchTransaction transaction = new SearchTransaction(question, new SearchResponse());
		try {
			for (InputProcessor processor : inputFlow) {
				processor.processInput(transaction);
			}

			for (DataFetcher fetcher : dataFetchers) {
				fetcher.fetchData(transaction);
			}

			for (OutputProcessor processor : outputFlow) {
				processor.processOutput(transaction);
			}

		} catch (InputProcessorException ipe) {
			log.error(ipe);
			transaction.setError(new SearchError(SearchError.Reason.InputProcessorError, ipe));
		} catch (DataFetchException dfe) {
			log.error(dfe);
			transaction.setError(new SearchError(SearchError.Reason.DataFetchError, dfe));
		} catch (OutputProcessorException ope) {
			log.error(ope);
			transaction.setError(new SearchError(SearchError.Reason.OutputProcessorError, ope));
		}
		
		return transaction;
	}
	
}
