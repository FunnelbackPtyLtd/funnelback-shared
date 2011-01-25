package com.funnelback.publicui.test.mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

public class MockConfigRepository implements ConfigRepository {

	private final Collection collection1;
	private final Collection collection2;
	
	// private final String searchHome;
	
	public MockConfigRepository(String searchHome) {
		try {
			collection1 = new Collection("collection1", new NoOptionsConfig(new File(searchHome), "collection1"));
			collection2 = new Collection("collection2", new NoOptionsConfig(new File(searchHome), "collection2"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Collection getCollection(String collectionId) {
		if (collection1.getId().equals(collectionId)) {
			return collection1;
		} else if (collection2.getId().equals(collectionId)) {
			return collection2;
		} else {
			return null;
		}
	}

	@Override
	public List<Collection> getAllCollections() {
		List<Collection> out = new ArrayList<Collection>();
		out.add(collection1);
		out.add(collection2);
		return out;
	}

	@Override
	public List<String> getAllCollectionIds() {
		List<String> out = new ArrayList<String>();
		out.add(collection1.getId());
		out.add(collection2.getId());
		return out;
	}

}

