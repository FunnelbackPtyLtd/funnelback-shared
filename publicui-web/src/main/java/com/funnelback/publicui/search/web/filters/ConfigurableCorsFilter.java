package com.funnelback.publicui.search.web.filters;

import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.springmvc.web.filter.CorsFilter;

/**
 * Filter to add a CORS allow origin header
 * 
 * @author nguillaumin@funnelback.com
 */
public class ConfigurableCorsFilter extends CorsFilter implements Filter {

    @Autowired
    @Setter
    private ConfigRepository configRepository;
    
    @Override
    public Optional<String> getCorsAllowOrigin(ServletRequest request, ServletResponse response) {
        Optional<String> opt = Optional.ofNullable(request.getParameter(RequestParameters.COLLECTION))
            .map(collectionName -> configRepository.getCollection(collectionName))
            .map(collection -> collection.getConfiguration())
            .map(config -> config.value(Keys.ModernUI.CORS_ALLOW_ORIGIN));
        if(opt.isPresent()){
            //Try reading from global
            return opt;
        }
        
        return super.getCorsAllowOrigin(request, response);
    }

}
