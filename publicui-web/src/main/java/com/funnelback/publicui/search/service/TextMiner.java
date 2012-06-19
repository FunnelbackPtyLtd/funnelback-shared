package com.funnelback.publicui.search.service;

import java.util.List;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.EntityDefinition;

public interface TextMiner {
    public EntityDefinition getEntityDefinition(String entity, Collection collection);
    
    public EntityDefinition getCustomDefinition(String entity, Collection collection);

    public List<String> getURLNounPhrases(String URL, Collection collection);
}
