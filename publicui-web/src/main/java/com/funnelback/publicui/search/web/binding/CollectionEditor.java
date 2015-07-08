package com.funnelback.publicui.search.web.binding;

import java.beans.PropertyEditorSupport;

import com.funnelback.common.config.CollectionId;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

public class CollectionEditor extends PropertyEditorSupport {
    
    private ConfigRepository configRepository;
    
    public CollectionEditor(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        // We attempt to parse the text as a CollectionId to validate it
        // and throw a binding exception if it's invalid
        if (text != null && text.contains(",")) {
            // FUN-4279: Account for more than one values of 'collection'
            setValue(configRepository.getCollection(new CollectionId(text.split(",")[0]).getId()));
        } else {
            setValue(configRepository.getCollection(new CollectionId(text).getId()));
        }
    }
    
    @Override
    public String getAsText() {
        return ((Collection) getValue()).getId();
    }
    
}
