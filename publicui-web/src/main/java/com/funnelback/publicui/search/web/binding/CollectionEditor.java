package com.funnelback.publicui.search.web.binding;

import java.beans.PropertyEditorSupport;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

public class CollectionEditor extends PropertyEditorSupport {
	
	private ConfigRepository configRepository;
	
	public CollectionEditor(ConfigRepository configRepository) {
		this.configRepository = configRepository;
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		// FUN-4279: Account for more than one values of 'collection'
		if (text.contains(",")) {
			setValue(configRepository.getCollection(text.split(",")[0]));
		} else {
			setValue(configRepository.getCollection(text));
		}
	}
	
	@Override
	public String getAsText() {
		return ((Collection) getValue()).getId();
	}
	
}
