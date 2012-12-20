package com.funnelback.publicui.search.service.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import org.springframework.stereotype.Repository;

import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.Store.View;
import com.funnelback.common.io.store.StoreType;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.DataRepository;

@Repository
@Log4j
public class LocalDataRepository implements DataRepository {
	
	/** Name of the parameter containing the record id for database collections */
	private final static String RECORD_ID = "record_id";
	
	public RecordAndMetadata<? extends Record<?>> getCachedDocument(
			Collection collection, View view, String url) {
		
		try (Store<? extends Record<?>> store = StoreType.getStore(collection.getConfiguration(), view)) {
			store.open();
			return store.getRecordAndMetadata(extractPrimaryKey(collection, url));		
		} catch (ClassNotFoundException cnfe) {
			log.error("Error while getting store for collection '"+collection.getId()+"'", cnfe);
		} catch (IOException ioe) {
			log.error("Couldn't access stored content on collection '"+collection.getId()+"' for URL '"+url+"'", ioe);
		}
	
		return new RecordAndMetadata<Record<?>>(null, null);

	}

	/**
	 * Resolves the primary key used to store the document from its URL,
	 * depending on the collection type. For example database collections use
	 * the database ID as primary key (12), but the actual document URL will be
	 * something like <code>local://serve-db-document?...&amp;record_id=12</code>
	 * 
	 * @param url URL of the document
	 * @return Corresponding primary key
	 */
	@SneakyThrows(UnsupportedEncodingException.class)
	private String extractPrimaryKey(Collection collection, String url) {
		switch (collection.getType()) {
		case database:
		case directory:
			return URLDecoder.decode(url.replaceFirst(".*[&?;]"+RECORD_ID+"=([^&]+).*", "$1"), "UTF-8");
		case trimpush:
			return URLDecoder.decode(url, "UTF-8");
		case meta:
		case unknown:
			throw new IllegalArgumentException("'"+collection.getType()+"' collections don't support cached copies.");
		default:
			return url;
		}
	}

}
