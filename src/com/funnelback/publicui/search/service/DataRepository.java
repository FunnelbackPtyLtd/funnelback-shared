package com.funnelback.publicui.search.service;

import com.funnelback.publicui.search.model.Collection;

/**
 * Interface to access collection data, such as files in
 * the live/offline view, index files, etc. *
 */
public interface DataRepository {

	/**
	 * Gets a cached document.
	 * @param collection
	 * @param relativeUrl relative URL of the document such as
	 * http/server/folder/doc.html.pan.txt, or 7/w/3/0/7w3043...html.pan.txt
	 * @return Content of the cache document
	 */
	public String getCachedDocument(Collection collection, String relativeUrl);
	
}
