package com.funnelback.publicui.search.lifecycle.input.processors;

import java.security.Principal;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.funnelback.common.system.Security;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingOptionsHelper;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.extern.log4j.Log4j2;
 
@Log4j2
@Component("increasePadreTimeOutForServiceUsers")
public class IncreasePadreTimeOutForServiceUsers extends AbstractInputProcessor {
    
    private static final long SERVICE_USER_PADRE_TIMEOUT = 1200000; //20mins
    
    @Override
    public void processInput(SearchTransaction searchTransaction) {
        Optional<String> userName = Optional.ofNullable(searchTransaction).map(SearchTransaction::getQuestion).map(SearchQuestion::getPrincipal)
                                            .map(Principal::getName);
        if(!userName.isPresent()) {
            log.trace("Search timeout will not be increased because the user name is unknown.");
            return;
        }
        if(!Security.isServiceUser(userName.get())) {
            log.trace("Search timeout will not be increased because the user '{}' is not a service user.", userName.get());
            return;
        }
        
        long currentValue = getPadreForkingOptionsHelper(searchTransaction).getPadreForkingTimeout();
        
        if(SERVICE_USER_PADRE_TIMEOUT > currentValue) {
            searchTransaction.getQuestion().setPadreTimeout(Optional.of(SERVICE_USER_PADRE_TIMEOUT));
            log.debug("Increasing timeout for service user '{}' from {} to {}", 
                    userName.get(), 
                    currentValue, 
                    searchTransaction.getQuestion().getPadreTimeout().get());
        } else {
            log.debug("NOT increasing timeout for service user '{}' as {} is larger than {}", 
                userName.get(), 
                currentValue, 
                SERVICE_USER_PADRE_TIMEOUT);
        }
    }
    
    protected PadreForkingOptionsHelper getPadreForkingOptionsHelper(SearchTransaction st) {
        return new PadreForkingOptionsHelper(st);
    }

    
}
