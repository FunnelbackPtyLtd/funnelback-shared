package com.funnelback.publicui.search.web.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;
import com.funnelback.publicui.search.model.collection.paramtransform.criteria.Criteria;
import com.funnelback.publicui.search.model.collection.paramtransform.criteria.ParameterMatchesValueCriteria;
import com.funnelback.publicui.search.model.collection.paramtransform.operation.AddParameterOperation;
import com.funnelback.publicui.search.model.collection.paramtransform.operation.Operation;
import com.funnelback.publicui.search.model.collection.paramtransform.operation.RemoveAllValuesOperation;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.ConfigRepository.GlobalConfiguration;
/**
 * Checks access restriction based on host names (dns_aliases.cfg)
 */
@Log4j2
public class HostnameRestrictionFilter implements Filter {

    
    @Autowired
    private ConfigRepository configRepository;

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        ServletRequest target = request;
        
        String collectionId = request.getParameter(RequestParameters.COLLECTION);
        if ( collectionId != null) {
            Map<String, String> dnsAliases = configRepository.getGlobalConfigurationFile(GlobalConfiguration.DNSAliases);
            if (dnsAliases != null) {
                String hostName = request.getServerName();
                if (dnsAliases.get(hostName) != null) {
                    String[] collections = dnsAliases.get(hostName).split(",");
                    log.debug("Authorized collections for hostname '" + hostName + "' : '" + dnsAliases.get(hostName) + "'");
                    if (collections != null && collections.length > 0 && ! "".equals(collections[0]) && ! ArrayUtils.contains(collections, collectionId)) {
                        // This collection is not authorized for this hostname
                        // Try to substitute the first collection of the list
                        log.debug("Unauthorized access on collection '" + collectionId + "' for hostname '" + hostName + "'. "
                                + "Collection '" + collections[0] + "' will be substituted.");
                        TransformRule rule = getTransformRule(collectionId, collections[0]);
                        target = new RequestParametersTransformWrapper((HttpServletRequest) request, Arrays.asList(new TransformRule[] {rule}));
                    } else {
                        log.debug("Access granted on collection '" + collectionId + "' for hostname '" + hostName + "'");
                    }
                } else {
                    log.trace("No hostname restriction for hostname '" + hostName + "'");
                }
            }
        }
        
        chain.doFilter(target, response);

    }

    @Override
    public void init(FilterConfig arg0) throws ServletException { }

    private TransformRule getTransformRule(String collectionFrom, String collectionTo) {
        Criteria c = new ParameterMatchesValueCriteria(RequestParameters.COLLECTION, collectionFrom);
        
        RemoveAllValuesOperation operation1 = new RemoveAllValuesOperation(RequestParameters.COLLECTION);
        AddParameterOperation operation2 = new AddParameterOperation(RequestParameters.COLLECTION, collectionTo);
        
        return new TransformRule(c, Arrays.asList(new Operation[] {operation1, operation2 }));
    }
    
}
