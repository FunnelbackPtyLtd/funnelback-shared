package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Map;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.ChangeCollectionQuestionFactory;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.ExtraSearchQuestionFactory;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Prepares extra searches configured in <code>collection.cfg</code>.
 */
@Component("extraSearchesInputProcessor")
@Log4j
public class ExtraSearches extends AbstractInputProcessor implements ApplicationContextAware {

    /**
     * Key containing the class of the {@link ExtraSearchQuestionFactory}
     * implementation.
     */
    private static final String KEY_CLASS = "class";
    
    private static final Class<? extends ExtraSearchQuestionFactory> DEFAULT_CLASS =
        ChangeCollectionQuestionFactory.class;
    
    @Autowired
    @Setter
    private ConfigRepository configRepository;
    
    @Setter private ApplicationContext applicationContext;
    
    @SuppressWarnings("unchecked")
    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && searchTransaction.getQuestion().getCollection().getConfiguration()
                    .hasValue(Keys.ModernUI.EXTRA_SEARCHES)
                && searchTransaction.getQuestion().getQuestionType().equals(SearchQuestion.SearchQuestionType.SEARCH) ) {
            
            String[] extraSearches = searchTransaction.getQuestion().getCollection()
                .getConfiguration().value(Keys.ModernUI.EXTRA_SEARCHES).split(",");
            
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
                            clazz = (Class<? extends ExtraSearchQuestionFactory>) Class
                                .forName(extraSearchConfiguration.get(KEY_CLASS));
                        }
                        ExtraSearchQuestionFactory factory = applicationContext.getAutowireCapableBeanFactory()
                            .createBean(clazz);
                        final SearchQuestion q = factory.buildQuestion(searchTransaction.getQuestion(),
                            extraSearchConfiguration);
                        
                        log.trace("Adding extra search '" + extraSearch
                                + "' on collection '" + q.getCollection().getId() + "'"
                                + " with query '" + q.getQuery() + "'");

                        searchTransaction.addExtraSearch(extraSearch, q);
                    } catch (Exception e) {
                        log.error("Unable to configure extra search '" + extraSearch + "'", e);
                    }
                } else {
                    log.error("Extra search configuration '" + extraSearch + "' for collection '"
                            + searchTransaction.getQuestion().getCollection().getId() + "' is not available");
                }
            }            
        }
    }
    
}
