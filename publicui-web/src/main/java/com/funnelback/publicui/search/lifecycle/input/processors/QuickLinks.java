package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Map;

import com.funnelback.config.keys.Keys;
import com.funnelback.config.keys.collection.QuickLinkKeys;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Checks is quick links are enabled, and sets the related
 * query processor options, falling back to default values
 * if nothing is specified in the config files.
 * 
 * If the query_processor_option already contains quicklinks related
 * options their value takes precedence.
 */
@Log4j2
@Component("quickLinksInputProcessor")
public class QuickLinks extends AbstractInputProcessor {

    /**
     * Name of the query processor option for quick links depth
     */
    private static final String QL_OPT_DEPTH = "-QL";
    
    /**
     * Name of the query processor option for quick links rank
     */
    private static final String QL_OPT_RANK = "-QL_rank";
    
    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)) {
            
            if (searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.CollectionKeys.QuickLinkKeys.QUICKLINKS)) {
            
                String qpOptions = searchTransaction.getQuestion().getCurrentProfileConfig().get(Keys.FrontEndKeys.QueryProcessor.QUERY_PROCESSOR_OPTIONS); 
    
                if (qpOptions == null || ! qpOptions.matches(".*($|\\s)" + QL_OPT_DEPTH + "=\\d+.*")) {
                    // No depth specified on the qp options
                    Integer depth = searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.CollectionKeys.QuickLinkKeys.DEPTH);
                    String opt = QL_OPT_DEPTH + "=" + Integer.toString(depth); 
                     
                    searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(opt);
                    log.debug("Added query processor option '" + opt + "'");
                }
                
                if (qpOptions == null || ! qpOptions.matches(".*($|\\s)" + QL_OPT_RANK + "=(\\d+|all).*")) {
                    // No rank specified on the qp options
                    Integer rank = searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.CollectionKeys.QuickLinkKeys.RANK);
                    String opt = QL_OPT_RANK + "=" + Integer.toString(rank);
                    searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(opt);
                    log.debug("Added query processor option '" + opt + "'");
                }
                
            }
        }
    }
}
