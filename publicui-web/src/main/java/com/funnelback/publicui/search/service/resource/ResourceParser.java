package com.funnelback.publicui.search.service.resource;

import java.io.File;
import java.io.IOException;

/**
 * <p>Parses a File and return a Java Object.</p>
 *
 */
public interface ResourceParser<T> {

	public T parse(File f) throws IOException;
	
}
