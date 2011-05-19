package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import lombok.extern.apachecommons.Log;

import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.ChangeCollectionQuestionFactory;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.ExtraSearchQuestionFactory;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Prepares any extra search and starts their execution.
 */
@Component("extraSearchesSubmitInputProcessor")
@Log
public class ExtraSearchesSubmit implements InputProcessor, ApplicationContextAware {

	/**
	 * Key containing the class of the {@link ExtraSearchQuestionFactory}
	 * implementation.
	 */
	private static final String KEY_CLASS = "class";
	
	private static final Class<? extends ExtraSearchQuestionFactory> DEFAULT_CLASS = ChangeCollectionQuestionFactory.class;
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Autowired
	private SearchTransactionProcessor processor;
	
	@Autowired
	private TaskExecutor executor;
	
	private ApplicationContext applicationContext;
	
	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		if (SearchTransactionUtils.hasQueryAndCollection(searchTransaction)
				&& searchTransaction.getQuestion().getCollection().getConfiguration().hasValue(Keys.PublicUI.EXTRA_SEARCHES)) {
			
			String[] extraSearches = searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.PublicUI.EXTRA_SEARCHES).split(",");
			
			// Configure and submit extra searches
			for (final String extraSearch: extraSearches) {
				log.trace("Configuring extra search '" + extraSearch + "'");
				
				Map<String, String> extraSearchConfiguration = configRepository.getExtraSearchConfiguration(
						searchTransaction.getQuestion().getCollection(),
						extraSearch);
				if (extraSearchConfiguration != null) {
					Class<? extends ExtraSearchQuestionFactory> clazz = DEFAULT_CLASS;
					
					try {
						
						
						if (extraSearchConfiguration.get(KEY_CLASS) != null) {
							// Try to use user defined class
							clazz = (Class<? extends ExtraSearchQuestionFactory>) Class.forName(extraSearchConfiguration.get(KEY_CLASS));
						}
						ExtraSearchQuestionFactory factory = applicationContext.getAutowireCapableBeanFactory().createBean(clazz);
						final SearchQuestion q = factory.buildQuestion(searchTransaction.getQuestion(), extraSearchConfiguration);
						
						log.trace("Submitting extra search '" + extraSearch
								+ "' on collection '" + q.getCollection().getId() + "'"
								+ " with query '" + q.getQuery() + "'");

						FutureTask<SearchTransaction> task = new FutureTask<SearchTransaction>(
								new Callable<SearchTransaction>() {
									@Override
									public SearchTransaction call() throws Exception {
										StopWatch sw = new StopWatch();
										try {
											sw.start();
											return processor.process(q);
										} finally {
											sw.stop();
											log.debug("Extra search '" + extraSearch + "' took " + sw.toString());
										}
									}
								});

						searchTransaction.addExtraSearch(extraSearch, task);
						executor.execute(task);
					} catch (Exception e) {
						log.error("Unable to process extra search '" + extraSearch + "'", e);
					}
				} else {
					log.error("Extra search configuration '" + extraSearch + "' for collection '"
							+ searchTransaction.getQuestion().getCollection().getId() + "' is not available");
				}
			}			
		}
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;		
	}
	
}
