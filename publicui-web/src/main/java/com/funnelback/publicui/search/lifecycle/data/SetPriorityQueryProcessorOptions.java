package com.funnelback.publicui.search.lifecycle.data;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SetPriorityQueryProcessorOptions extends AbstractDataFetcher {

    @Override
    public void fetchData(SearchTransaction searchTransaction)
        throws DataFetchException {
        searchTransaction.getQuestion().getPriorityQueryProcessorOptions()
            .getOptions()
            .keySet()
            .forEach(option -> {
                if(searchTransaction.getQuestion().getAdditionalParameters().remove(option) != null) {
                    log.debug("Overwriting '{}' in addtional parameters", option);
                }
                
                if(searchTransaction.getQuestion().getRawInputParameters().remove(option) != null) {
                    log.debug("Overwriting '{}' in raw input parameters", option);
                }
                
                if(searchTransaction.getQuestion().getDynamicQueryProcessorOptions()
                    .removeIf(s -> s.startsWith("-" + option + "="))) {
                    log.debug("Overwriting '{}' in dynamic query processor options", option);
                }
            });
        
        searchTransaction.getQuestion().getPriorityQueryProcessorOptions()
            .getOptions()
            .entrySet()
            .stream()
            .map(option -> "-" + option.getKey() + "=" + option.getValue())
            .forEach(searchTransaction.getQuestion().getDynamicQueryProcessorOptions()::add);
        
    }
}
