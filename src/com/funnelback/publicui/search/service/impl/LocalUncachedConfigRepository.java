package com.funnelback.publicui.search.service.impl;

import com.funnelback.publicui.search.model.collection.Collection;

public class LocalUncachedConfigRepository extends LocalConfigRepository {

	@Override
	public Collection getCollection(String collectionId) {
		return loadCollection(collectionId);
	}
	
}
