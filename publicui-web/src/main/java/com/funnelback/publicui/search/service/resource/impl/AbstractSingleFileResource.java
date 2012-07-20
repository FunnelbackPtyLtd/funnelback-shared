package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.IOException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import com.funnelback.publicui.search.service.resource.ParseableResource;
import com.funnelback.publicui.search.service.resource.ResourceManager;

/**
 * <p>Base class for resources based on a single file.</p>
 * 
 * <p>It will use the file system for implementing the various
 * {@link ResourceManager} methods:
 * <ul>
 * 	<li>{@link #getCacheKey()} will return the absolute path of the file.</li>
 *  <li>{@link #isStale(long)} will check the file last modified date.</li>
 *  <li>{@link #exists()} will check if the file exists.</li>
 * </ul>
 * </p>
 * 
 * @param <T> Type of the resource returned
 */
@RequiredArgsConstructor
@Log4j
public abstract class AbstractSingleFileResource<T> implements ParseableResource<T> {

	protected final File file;
	
	@Override
	public abstract T parse() throws IOException;
	
	@Override
	public boolean isStale(long timestamp) {
		boolean stale = file.lastModified() > timestamp;
		log.debug("Stale check for file '"+file.getAbsolutePath()+"' returned '"+stale+"' (lastModified="+file.lastModified()+",timestamp="+timestamp+")");
		return stale;
	}
	
	@Override
	public Object getCacheKey() {
		return file.getAbsolutePath();
	}
	
	@Override
	public boolean exists() {
		return file.exists();
	}
}
