package com.funnelback.publicui.web.binding;

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
		setValue(configRepository.getCollection(text));
	}
	
	@Override
	public String getAsText() {
		return ((Collection) getValue()).getId();
	}
	
}
