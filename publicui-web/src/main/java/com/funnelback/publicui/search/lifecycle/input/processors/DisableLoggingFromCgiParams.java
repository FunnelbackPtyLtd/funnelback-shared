package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Optional;
import java.util.Set;

import java.security.Principal;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.ImmutableSet;

import lombok.extern.log4j.Log4j2;

@Component("disableLoggingFromCgiParams")
@Log4j2
public class DisableLoggingFromCgiParams extends AbstractInputProcessor {
    
    // follow what padre considers to be off as the log option follows padre
    private Set<String> VALUES_FOR_OFF = ImmutableSet.of("off", "false", "0");

    @Override
    public void processInput(SearchTransaction searchTransaction) {
        Optional<Principal> principle = Optional.ofNullable(searchTransaction)
                .map(SearchTransaction::getQuestion)
                .map(SearchQuestion::getPrincipal);
        // if present then we are authenticated
        if(principle.isPresent()) {
            String logValue = searchTransaction.getQuestion().getInputParameterMap().get("log");
            if(logValue != null && VALUES_FOR_OFF.contains(logValue.toLowerCase())) {
                searchTransaction.getQuestion().setLogQuery(false);
            }
        } else {
            log.trace("no authenticated user logging can not be disabled");
        }
        
    }
}
