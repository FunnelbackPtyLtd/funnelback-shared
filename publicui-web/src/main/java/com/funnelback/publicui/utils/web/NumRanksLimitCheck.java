package com.funnelback.publicui.utils.web;

import java.util.Collections;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.funnelback.common.config.Config;
import com.funnelback.common.padre.NumRanks;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.data.service.ServiceConfigDataReadOnly;
import com.funnelback.config.keys.Keys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.exception.NumRanksExceededException;
import com.google.common.base.CharMatcher;

public class NumRanksLimitCheck {

    public boolean numRanksExceeded(String numRanksParam, NumRanks numRanksLimit) {
        if(numRanksParam.isEmpty()) {
            return false;
        }
        
        // Only check numeric num_ranks padre will ignore it if it is non numeric.
        if(!CharMatcher.inRange('0', '9').matchesAllOf(numRanksParam)) {
            return false;
        }
        
        // Some huge numeric number.
        if(numRanksParam.length() > Integer.toString(NumRanks.MAX_SIZE).length()) {
            return true;
        }
        
        if(Long.parseLong(numRanksParam) > (long) numRanksLimit.getValue()) {
            return true;
        }
        
        return false;
    }
    
    public void verifyNumRanksLimitIsNotExceeded(HttpServletRequest request, ServiceConfigReadOnly serviceConfig, I18n i18n) 
            throws NumRanksExceededException {
        NumRanks numRanksLimit = serviceConfig.get(Keys.FrontEndKeys.ModernUI.NUM_RAMKS_LIMIT);
        Collections.list(request.getHeaderNames()).forEach(System.out::println);
        for(String numRanksParam : Optional.ofNullable(request.getParameterMap().get(RequestParameters.NUM_RANKS)).orElse(new String[0])) {
            if(numRanksExceeded(numRanksParam, numRanksLimit)) {
                throw new NumRanksExceededException(numRanksLimit, i18n);
            }
        }
    }
}
