package com.funnelback.publicui.search.lifecycle.data.fetchers.padre;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.aop.Profiled;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.JavaPadreForker;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForker.PadreExecutionReturn;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.WindowsNativePadreForker;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.PadreXmlParser;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.PadreXmlParsingException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Forks PADRE and communicate with it using stdin/out/err
 * 
 * Will choose either to fork using a Java API or Native Windows API
 * depending of the "impersonation" status of the transaction.
 */
@Component
@lombok.extern.apachecommons.Log
public class PadreForking implements DataFetcher {

	private enum EnvironmentKeys {
		SEARCH_HOME, QUERY_STRING, SystemRoot;
	}
	
	private static final String OPT_USER_KEYS = "-userkeys";
	
	/** PADRE return code for success */
	public static final int RC_SUCCESS = 0;
	
	@Autowired
	@Setter
	private File searchHome;
	
	@Autowired
	@Setter
	private PadreXmlParser padreXmlParser;
	
	@Value("#{appProperties['padre.fork.native.timeout']}")
	@Setter
	private int padreWaitTimeout;
	
	@Override
	@Profiled
	public void fetchData(SearchTransaction searchTransaction) throws DataFetchException {
		if (SearchTransactionUtils.hasQueryAndCollection(searchTransaction)) {
			
			String commandLine = new File(searchHome,
					DefaultValues.FOLDER_BIN 				
					+ File.separator
					+ searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.QUERY_PROCESSOR)).getAbsolutePath()
					+ " " + StringUtils.join(searchTransaction.getQuestion().getDynamicQueryProcessorOptions().toArray(new String[0]), " ");
			
			if (searchTransaction.getQuestion().getUserKeys().size() > 0) {
				commandLine += " " + OPT_USER_KEYS + "=\""
				+ StringUtils.join(searchTransaction.getQuestion().getUserKeys().toArray(new String[0])) + "\"";
			}
	
			Map<String, String> env = new HashMap<String, String>(searchTransaction.getQuestion().getEnvironmentVariables());
			env.put(EnvironmentKeys.SEARCH_HOME.toString(), searchHome.getAbsolutePath());
			env.put(EnvironmentKeys.QUERY_STRING.toString(), PadreQueryStringBuilder.buildQueryString(searchTransaction));
	
			// SystemRoot environment variable is MANDATORY for TRIM DLS checks
			// The TRIM SDK uses WinSock to connect to the remote server, and 
			// WinSock needs SystemRoot to initialise itself.
			if (System.getenv(EnvironmentKeys.SystemRoot.toString()) != null) {
				env.put(EnvironmentKeys.SystemRoot.toString(), System.getenv(EnvironmentKeys.SystemRoot.toString()));
			}
	
			PadreExecutionReturn padreOutput = null;
			try {
				if (searchTransaction.getQuestion().isImpersonated()) {
					padreOutput = new WindowsNativePadreForker(padreWaitTimeout).execute(commandLine, env);
				} else {
					padreOutput = new JavaPadreForker().execute(commandLine, env);
				}
				
				searchTransaction.getResponse().setRawPacket(padreOutput.getOutput().toString());
				searchTransaction.getResponse().setResultPacket(padreXmlParser.parse(padreOutput.getOutput().toString()));
				searchTransaction.getResponse().setReturnCode(padreOutput.getReturnCode());
			} catch (PadreForkingException pfe) {
				log.error("PADRE forking failed", pfe);
				throw new DataFetchException("PADRE forking failed", pfe);				
	
			} catch (PadreXmlParsingException pxpe) {
				log.error("Unable to parse PADRE output", pxpe);
				if (padreOutput != null && padreOutput.getOutput() != null && padreOutput.getOutput().length() > 0) {
					log.error("PADRE output was: \n" + padreOutput.getOutput());
				}
				throw new DataFetchException("Unable to parse PADRE output", pxpe);
			}
		}
	}
	
}
