package com.funnelback.publicui.search.lifecycle.data.fetchers.padre;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.WindowsNativePadreForker;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.PadreXmlParser;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.PadreXmlParsingException;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchError;
import com.funnelback.publicui.search.model.transaction.SearchError.Reason;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

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
	private File searchHome;
	
	@Autowired
	private PadreXmlParser padreXmlParser;
	
	@Value("#{appProperties['padre.fork.native.timeout']}")
	private int padreWaitTimeout;
	
	@Override
	@Profiled
	public void fetchData(SearchTransaction searchTransaction) throws DataFetchException {

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
		env.put(EnvironmentKeys.SystemRoot.toString(), System.getenv(EnvironmentKeys.SystemRoot.toString()));


		String padreOutput = null;
		try {
			if (searchTransaction.getQuestion().isImpersonated()) {
				padreOutput = new WindowsNativePadreForker(padreWaitTimeout).execute(commandLine, env);
			} else {
				padreOutput = new JavaPadreForker().execute(commandLine, env);
			}
			
			searchTransaction.getResponse().setRawPacket(padreOutput.toString());
			searchTransaction.getResponse().setResultPacket(padreXmlParser.parse(padreOutput.toString()));
		} catch (PadreForkingException pfe) {
			log.error("PADRE forking failed", pfe);
			SearchError se = new SearchError(Reason.DataFetchError, pfe);

			// Try to parse the XML packet
			if (pfe.getOutput() != null) {
				try {			
					ResultPacket packet = padreXmlParser.parse(pfe.getOutput());					
					se.setAdditionalData(packet.getError());
				} catch (PadreXmlParsingException pxpe) {
					log.debug("Unable to parse erroneous XML packet", pxpe);
				}
			}
			searchTransaction.setError(se);				

		} catch (PadreXmlParsingException pxpe) {
			log.error("Unable to parse PADRE output", pxpe);
			if (padreOutput != null && padreOutput.length() > 0) {
				log.error("PADRE output was: \n" + padreOutput);
			}
			throw new DataFetchException("Unable to parse PADRE output", pxpe);
		}

	}
	
}
