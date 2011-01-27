package com.funnelback.publicui.test.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.funnelback.publicui.search.model.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

public class MockConfigRepository implements ConfigRepository {

	private Map<String, Collection> collections = new HashMap<String, Collection>();
		
	@Override
	public Collection getCollection(String collectionId) {
		return collections.get(collectionId);
	}

	@Override
	public List<Collection> getAllCollections() {
		return new ArrayList<Collection>(collections.values());
	}

	@Override
	public List<String> getAllCollectionIds() {
		return new ArrayList<String>(collections.keySet());
	}
	
	public void addCollection(Collection c) {
		collections.put(c.getId(), c);
	}
	
	public void removeCollection(String collectionId) {
		collections.remove(collectionId);
	}
	
	public void removeAllCollections() {
		collections.clear();
	}

}

