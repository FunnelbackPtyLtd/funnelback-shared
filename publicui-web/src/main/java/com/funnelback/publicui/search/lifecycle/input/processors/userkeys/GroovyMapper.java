package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import com.funnelback.common.config.Keys;
import com.funnelback.common.utils.ClassUtils;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.GenericHookScriptRunner;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import groovy.lang.Script;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * {@link UserKeysMapper} that delegates fetching the keys to
 * a Groovy script.
 *
 */
@Log4j2
public class GroovyMapper implements UserKeysMapper {

    @Autowired
    @Setter private I18n i18n;
    
    @Autowired
    @Setter private AutowireCapableBeanFactory beanFactory;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<String> getUserKeys(Collection collection, SearchTransaction transaction) {
        String className = transaction.getQuestion().getCollection()
            .getConfiguration().value(Keys.SecurityEarlyBinding.GROOVY_CLASS);
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException(i18n.tr("inputprocessor.userkeys.groovy.undefined",
                Keys.SecurityEarlyBinding.GROOVY_CLASS));
        }
        
        Class<?> scriptClass = (Class<?>) ClassUtils.forName(className, this.getClass());
        
        if(UserKeysMapper.class.isAssignableFrom(scriptClass)) {
            UserKeysMapper mapper = (UserKeysMapper) beanFactory.createBean(scriptClass);
            return mapper.getUserKeys(collection, transaction);
        }
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put(Hook.SEARCH_TRANSACTION_KEY, transaction);
            data.put(Hook.COLLECTION_KEY, collection);
            
            Object o = GenericHookScriptRunner.runScript((Class<Script>) scriptClass, data);
            
            if (o == null) {
                // No keys
                return new ArrayList<String>();
            } else if (o instanceof String) {
                // Single key
                List<String> out = new ArrayList<String>();
                out.add((String) o);
                return out;
            } else if (o instanceof List) {
                // Multiple keys
                return (List) o;
            } else {
                log.error("User to key mapper class returned an unsupported value type '"
                    + o.getClass().getName() + "'. Only "+String.class.getName()+" and "+List.class.getName()+"<"+
                    String.class.getName()+"> are supported.");
                return new ArrayList<String>();
            }
        } catch (Throwable t) { // Catch any error in the Groovy code
            log.error("Error while executing Groovy user to key mapper", t);
            return new ArrayList<String>();
        }
        
    }

}
