package com.funnelback.publicui.search.lifecycle.input.processors;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Maps config metadata aliases to corresponding metadata class.
 * For example we can map filetype to f, so if we see in the a query
 * 'filetype:pdf' we can change that to 'f:pdf'.
 */
@Component("metadataAliasesInputProcessor")
@Log4j2
public class MetadataAliases extends AbstractInputProcessor {

    private static final String SEPARATOR = ":";
    
    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (SearchTransactionUtils.hasQuery(searchTransaction)) {
            Config config = searchTransaction.getQuestion().getCollection().getConfiguration();
            boolean updateQuery = false;
            String[] terms = searchTransaction.getQuestion().getQuery().split("\\s");
            //Look for query terms that have a SEPARATOR, then see if we have a metadata
            //alias defined in Config, if so replace the aliase with the real metadata name.
            for(int i=0;i<terms.length; i++) {
                if(terms[i].contains(SEPARATOR)){
                    String alias = terms[i].split(SEPARATOR)[0];
                    if("".equals(alias)) continue;
                    String key = Keys.ModernUI.metadataAlias(alias);
                    String metadata = config.value(key);
                    if(metadata != null && !"".equals(metadata)){
                        terms[i] = metadata + terms[i].substring(alias.length());
                        updateQuery = true;
                    }
                }
            }
            
            if (updateQuery) {
                log.debug("Update query from '" + searchTransaction.getQuestion().getQuery() + "' to '" + StringUtils.join(terms, " ") + "'");
                searchTransaction.getQuestion().setQuery(StringUtils.join(terms, " "));
            }
        }
    }

}
