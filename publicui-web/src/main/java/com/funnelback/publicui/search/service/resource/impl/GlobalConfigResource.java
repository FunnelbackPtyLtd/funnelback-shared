package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.IOException;

import lombok.extern.log4j.Log4j;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.GlobalOnlyConfig;
import com.funnelback.publicui.search.service.resource.ParseableResource;

/**
 * Loads the <code>global.cfg(.default)</code>
 */
@Log4j
public class GlobalConfigResource implements ParseableResource<GlobalOnlyConfig> {

	private final File searchHome;
	private final File[] filesToCheck;

	public GlobalConfigResource(File searchHome) {
		this.searchHome = searchHome;
		filesToCheck = new File[] {
			new File(searchHome + File.separator + DefaultValues.FOLDER_CONF, Files.DEFAULT_GLOBAL_FILENAME),
			new File(searchHome + File.separator + DefaultValues.FOLDER_CONF, Files.LOCAL_GLOBAL_FILENAME)
		};
	}
	
	@Override
	public GlobalOnlyConfig parse() throws IOException {
		log.debug("Creating global config object");
		return new GlobalOnlyConfig(searchHome);
	}
	
	@Override
	public boolean isStale(long timestamp) {
		for (File f: filesToCheck) {
			if (f.lastModified() > timestamp) {
				log.debug("Stale check for file '"+f.getAbsolutePath()+"' returned true (lastModified="+f.lastModified()+",timestamp="+timestamp+")");
				return true;
			} else {
				log.debug("Stale check for file '"+f.getAbsolutePath()+"' returned false (lastModified="+f.lastModified()+",timestamp="+timestamp+")");
			}
		}
		
		return false;
	}

	@Override
	public Object getCacheKey() {
		return new File(searchHome + File.separator + DefaultValues.FOLDER_CONF, Files.LOCAL_GLOBAL_FILENAME);
	}
	
	@Override
	public boolean exists() {
		for (File f: filesToCheck) {
			if (f.exists()) {
				return true;
			}
		}
		
		return false;
	}
	
	
}
