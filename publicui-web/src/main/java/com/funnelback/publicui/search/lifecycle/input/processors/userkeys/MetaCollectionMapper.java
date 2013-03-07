package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.UserKeys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Reads the {@link UserKeysMapper} for each component collection
 * and call them in turn.
 * 
 * @since 11.4
 */
@Log4j
public class MetaCollectionMapper implements UserKeysMapper {

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private I18n i18n;
    
    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    
    @Override
    public List<String> getUserKeys(SearchTransaction transaction) {
        Collection c = transaction.getQuestion().getCollection();
        List<String> out = new ArrayList<String>();
        if (c.getType().equals(Type.meta)) {
            for (String component: c.getMetaComponents()) {
                Collection componentCollection = configRepository.getCollection(component);
                if (componentCollection != null) {
                    String securityPlugin = componentCollection.getConfiguration().value(
                            Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER);
                    if (securityPlugin != null && ! "".equals(securityPlugin)) {
                        try {
                            out.addAll(UserKeys.getUserKeys(securityPlugin, transaction, i18n, beanFactory));
                        } catch (InputProcessorException ipe) {
                            throw new IllegalStateException("Unable to secure transaction for collection '"
                                    + componentCollection + "' with plugin '"+securityPlugin+"'", ipe);
                        }
                    }
                }
            }
        } else {
            log.warn("Collection '"+c.getId()+"' is not a meta collection");
        }
        return out;
    }

}
