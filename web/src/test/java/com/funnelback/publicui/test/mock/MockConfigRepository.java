package com.funnelback.publicui.test.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

public class MockConfigRepository implements ConfigRepository {

	private Map<String, Collection> collections = new HashMap<String, Collection>();
	@Getter private Map<GlobalConfiguration, Map<String, String>> globalConfigs = new HashMap<GlobalConfiguration, Map<String, String>>();
		
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
	
	@Override
	public Map<String, String> getGlobalConfiguration(GlobalConfiguration conf) {
		return globalConfigs.get(conf);
	}

}

