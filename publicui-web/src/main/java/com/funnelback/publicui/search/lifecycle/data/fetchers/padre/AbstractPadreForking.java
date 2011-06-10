package com.funnelback.publicui.search.lifecycle.data.fetchers.padre;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.aop.Profiled;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.JavaPadreForker;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForker.PadreExecutionReturn;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.WindowsNativePadreForker;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.PadreXmlParser;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.xml.XmlParsingException;

/**
 * Forks PADRE and communicate with it using stdin/out/err
 * 
 * Will choose either to fork using a Java API or Native Windows API
 * depending of the "impersonation" status of the transaction.
 */
@lombok.extern.apachecommons.Log
public abstract class AbstractPadreForking implements DataFetcher {

	public enum EnvironmentKeys {
		SEARCH_HOME, QUERY_STRING, SystemRoot;
	}
	
	private static final String OPT_USER_KEYS = "-userkeys";
	
	@Autowired
	@Setter
	protected File searchHome;
	
	@Autowired
	@Setter
	protected PadreXmlParser padreXmlParser;
	
	@Value("#{appProperties['padre.fork.native.timeout']}")
	@Setter
	protected int padreWaitTimeout;

	@Autowired
	protected I18n i18n;
	
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
			env.put(EnvironmentKeys.QUERY_STRING.toString(), getQueryString(searchTransaction));
	
			// SystemRoot environment variable is MANDATORY for TRIM DLS checks
			// The TRIM SDK uses WinSock to connect to the remote server, and 
			// WinSock needs SystemRoot to initialise itself.
			if (System.getenv(EnvironmentKeys.SystemRoot.toString()) != null) {
				env.put(EnvironmentKeys.SystemRoot.toString(), System.getenv(EnvironmentKeys.SystemRoot.toString()));
			}
	
			PadreExecutionReturn padreOutput = null;
			try {
				if (searchTransaction.getQuestion().isImpersonated()) {
					padreOutput = new WindowsNativePadreForker(i18n, padreWaitTimeout).execute(commandLine, env);
				} else {
					padreOutput = new JavaPadreForker(i18n).execute(commandLine, env);
				}
				if (log.isTraceEnabled()) {
					log.trace("\n---- RAW result packet BEGIN ----:\n\n"+padreOutput.getOutput()+"\n---- RAW result packet END ----");
				}

				updateTransaction(searchTransaction, padreOutput);
			} catch (PadreForkingException pfe) {
				log.error("PADRE forking failed", pfe);
				throw new DataFetchException(i18n.tr("padre.forking.failed"), pfe);	
			} catch (XmlParsingException pxpe) {
				log.error("Unable to parse PADRE response", pxpe);
				if (padreOutput != null && padreOutput.getOutput() != null && padreOutput.getOutput().length() > 0) {
					log.error("PADRE response was: \n" + padreOutput.getOutput());
				}
				throw new DataFetchException(i18n.tr("padre.response.parsing.failed"), pxpe);
			}
		}
	}
	
	protected abstract String getQueryString(SearchTransaction transaction);
	
	protected abstract void updateTransaction(SearchTransaction transaction, PadreExecutionReturn padreOutput) throws XmlParsingException;
}
