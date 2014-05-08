package com.funnelback.publicui.search.lifecycle.input.processors;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import lombok.extern.log4j.Log4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.MetaMapper;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.UserKeysMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Fetches user keys for early binding DLS. Will cache them in memory
 * if a cache-ttl is set in <tt>collection.cfg</tt>
 * 
 */
@Component("userKeysInputProcessor")
@Log4j
public class UserKeys extends AbstractInputProcessor {
    
    /** Suffix of classes implementing the user key mapper interface */
    private static final String MAPPER_SUFFIX = "Mapper";
    
    /** Name of the EHCache used to cache user keys */
    private static final String CACHE = "userKeyMapper";
    
    /**
     * Character to use in the cache key to separate the collection ID
     * from the user name
     */
    private static final String COLLECTION_NAME_SEPARATOR = ":";
    
    @Autowired
    @Setter private I18n i18n;
    
    @Autowired
    @Setter private AutowireCapableBeanFactory beanFactory;
    
    @Autowired
    @Setter
    private CacheManager appCacheManager;
    
    @SuppressWarnings("unchecked")
    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)) {
            String securityPlugin = searchTransaction.getQuestion().getCollection().getConfiguration().value(
                    Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER);
            if (securityPlugin != null && ! "".equals(securityPlugin)) {
                
                Cache cache = appCacheManager.getCache(CACHE);

                // Do not cache by default (0s TTL)
                int cacheTtl = searchTransaction.getQuestion().getCollection().getConfiguration().valueAsInt(
                    Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER_CACHE_SECONDS, 0);
                
                if (cacheTtl > 0) {
                    // Try to read from cache
                    Element elt = cache.get(getCacheKey(searchTransaction));
                    if (elt != null) {
                        searchTransaction.getQuestion().getUserKeys().addAll((List<? extends String>) elt.getValue());
                        return;
                    }
                }
                
                // No cache or element not found in cache
                searchTransaction.getQuestion().getUserKeys().addAll(
                        getUserKeys(securityPlugin, searchTransaction.getQuestion().getCollection(),
                            searchTransaction, i18n, beanFactory));
                
                if (cacheTtl > 0) {
                    // Save in cache
                    Element elt = new Element(getCacheKey(searchTransaction),
                        searchTransaction.getQuestion().getUserKeys());
                    elt.setTimeToLive(cacheTtl);
                    cache.put(elt);
                }
            }
        }
    }

    private String getCacheKey(SearchTransaction transaction) {
        Principal principal = transaction.getQuestion().getPrincipal();
        String remoteUser = transaction.getQuestion().getInputParameterMap().get(
            PassThroughEnvironmentVariables.Keys.REMOTE_USER.toString());
        
        if (principal != null
            && principal.getName() != null
            && ! "".equals(principal.getName())) {
            return transaction.getQuestion().getCollection().getId()
                + COLLECTION_NAME_SEPARATOR
                + transaction.getQuestion().getPrincipal().getName();
        } else if (remoteUser != null
            && ! "".equals(remoteUser)) {
            return transaction.getQuestion().getCollection().getId()
                + COLLECTION_NAME_SEPARATOR
                + remoteUser;
        } else {
            log.warn("Neither the Security Principal or the REMOTE_USER are" +
                " available in the search transaction. User keys will not be cached");
            return null;
        }
    }
    
    /**
     * "Secures" a search transaction by applying the given security plugin
     * @param securityPlugin Class name of the security plugin ({@link UserKeysMapper})
     * @param collection to consider for reading config values. This is for meta collections
     *  where the component should be used instead of the collection of the search transaction.
     * @param st {@link SearchTransaction} to secure
     * @param i18n for error messages
     * @param beanFactory Factory used to create the security plugin instance
     * @return A list of user keys. Might be empty or null depending of the actual
     * {@link UserKeysMapper} implementation
     * @throws InputProcessorException 
     */
    public static List<String> getUserKeys(String securityPlugin,
        Collection collection, SearchTransaction st,
        I18n i18n, AutowireCapableBeanFactory beanFactory) throws InputProcessorException {
        
        String className = UserKeysMapper.class.getPackage().getName()
            + "." + securityPlugin
            + MAPPER_SUFFIX;
        
        if (securityPlugin.contains(".")) {
            // Use fully qualified class name instead of injecting the package name
            className = securityPlugin;
        }
        
        log.debug("Will use '" + className + "' security plugin");
        
        try {
            Class<?> clazz = Class.forName(className);
            UserKeysMapper mapper = (UserKeysMapper) beanFactory.createBean(clazz);
            
            List<String> rawKeys = mapper.getUserKeys(collection, st);
            
            // Prefix every userKey with "collection_name;" (so padre can direct them appropriately for meta collections)
            // But not for MetaMapper (for which they will already have been added for the sub-component
            List<String> result = new ArrayList<String>();
            if (!clazz.equals(MetaMapper.class)) {
                for (String rawKey : rawKeys) {
                    result.add(collection.getId() + ";" + rawKey);
                }
            } else {
                result.addAll(rawKeys);
            }
            
            return result;
        } catch (ClassNotFoundException cnfe) {
            throw new InputProcessorException(i18n.tr("inputprocessor.userkeys.plugin.invalid", securityPlugin), cnfe);
        }
    }

}
