package com.funnelback.publicui.search.service.data.filecopy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import com.funnelback.publicui.search.model.collection.Collection;

/**
 * Streams a filecopy document
 * 
 * @since 12.4
 */
public interface FilecopyDocumentStreamer {

	public void streamDocument(Collection collection, URI uri, OutputStream os) throws IOException;
	
	public void streamPartialDocument(Collection collection, URI uri, OutputStream os, int limit) throws IOException;
	
}
