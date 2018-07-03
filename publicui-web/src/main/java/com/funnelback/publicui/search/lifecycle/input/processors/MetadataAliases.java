package com.funnelback.publicui.search.lifecycle.input.processors;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

import lombok.extern.log4j.Log4j2;

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
            ServiceConfigReadOnly serviceConfig = searchTransaction.getQuestion().getCurrentProfileConfig();
            String[] terms = searchTransaction.getQuestion().getQuery().split("\\s");
            boolean updateQuery = false;

            //Look for query terms that have a SEPARATOR, then see if we have a metadata
            //alias defined in Config, if so replace the aliase with the real metadata name.
            for (int i=0; i < terms.length; i++) {
                // The seperator can not be the first character
                if (terms[i].indexOf(SEPARATOR) > 0) {
                    String alias = terms[i].split(SEPARATOR)[0];
                    if (alias.trim().isEmpty()) continue;

                    Optional<String> metadata = serviceConfig.get(FrontEndKeys.ModernUi.getMetadataAlias(alias));
                    if (metadata.isPresent() && !metadata.get().trim().isEmpty()) {
                        terms[i] = metadata.get() + terms[i].substring(alias.length());
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
