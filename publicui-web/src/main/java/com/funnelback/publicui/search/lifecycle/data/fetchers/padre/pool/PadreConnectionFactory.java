package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.pool;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.apachecommons.Log;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.PadreForking.EnvironmentKeys;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreExecutor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Builds {@link PadreConnection} by forking new padre-sw processes.
 */
@Log
public class PadreConnectionFactory extends BaseKeyedPoolableObjectFactory {

	@Autowired
	private ConfigRepository configRepository;

	@Autowired
	private File searchHome;
	
	@Override
	public PadreConnection makeObject(Object key) throws Exception {
		String collectionId = (String) key;
		Collection c = configRepository.getCollection(collectionId);
		
		if (c == null) {
			throw new IllegalArgumentException("Invalid collection '" + collectionId + "'");
		}
		
		File indexStem = new File(c.getConfiguration().getCollectionRoot()
				+ File.separator + DefaultValues.VIEW_LIVE + File.separator + DefaultValues.FOLDER_IDX,
				DefaultValues.INDEXFILES_PREFIX);
		
		String commandLine = new File(searchHome
				+ File.separator + DefaultValues.FOLDER_BIN,								
				c.getConfiguration().value(Keys.QUERY_PROCESSOR)).getAbsolutePath()
				+ " " + indexStem.getAbsolutePath() + " -res xml";
		
		Map<String, String> env = new HashMap<String, String>();
		env.put(EnvironmentKeys.SEARCH_HOME.toString(), searchHome.getAbsolutePath());

		// SystemRoot environment variable is MANDATORY for TRIM DLS checks
		// The TRIM SDK uses WinSock to connect to the remote server, and 
		// WinSock needs SystemRoot to initialise itself.
		if (System.getenv(EnvironmentKeys.SystemRoot.toString()) != null) {
			env.put(EnvironmentKeys.SystemRoot.toString(), System.getenv(EnvironmentKeys.SystemRoot.toString()));
		}

		log.debug("Running new PADRE instance for collection '" + c.getId());
		PadreExecutor executor = new PadreExecutor();
		executor.setStreamHandler(new PadreStreamHandler());
		executor.execute(CommandLine.parse(commandLine), env, new PadreResultHandler());
		
		return new PadreConnection(executor);
	}
	
	@Override
	public void destroyObject(Object key, Object obj) throws Exception {
		PadreConnection c = (PadreConnection) obj;
		c.close();
	}
	
	private class PadreResultHandler implements ExecuteResultHandler {
		@Override
		public void onProcessFailed(ExecuteException ex) {
			log.error("PADRE failed", ex);				
		}
		
		@Override
		public void onProcessComplete(int rc) {
			log.info("PADRE completed with exit code: " + rc);				
		}
	}

}
