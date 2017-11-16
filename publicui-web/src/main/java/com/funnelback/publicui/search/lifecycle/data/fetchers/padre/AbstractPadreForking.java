package com.funnelback.publicui.search.lifecycle.data.fetchers.padre;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.exec.OS;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.execute.ExecutablePicker;
import com.funnelback.common.lock.ThreadSharedFileLock.FileLockException;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.AbstractDataFetcher;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.JavaPadreForker;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingOptionsHelper;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.WindowsNativePadreForker;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.index.QueryReadLock;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.PadreXmlParser;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Forks PADRE and communicate with it using stdin/out/err
 * 
 * Will choose either to fork using a Java API or Native Windows API
 * depending of the "impersonation" status of the transaction.
 */
@Log4j2
public abstract class AbstractPadreForking extends AbstractDataFetcher {

    public enum EnvironmentKeys {
        SEARCH_HOME, QUERY_STRING, SystemRoot;
    }
    
    private static final String OPT_USER_KEYS = "-userkeys";

    /**
     * Whereas to use an absolute path when invoking the query
     * processor or a relative (from bin/) one. 
     */
    @Setter
    private boolean absoluteQueryProcessorPath = false;
    
    @Autowired
    @Setter
    protected File searchHome;
    
    @Autowired
    @Setter
    protected PadreXmlParser padreXmlParser;
    
    @Autowired
    protected I18n i18n;
    
    @Autowired
    @Setter @Getter
    protected QueryReadLock queryReadLock;
    
    private ExecutablePicker executablePicker = new ExecutablePicker();
    
    @Override
    public void fetchData(SearchTransaction searchTransaction) throws DataFetchException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && new PadreQueryStringBuilder(searchTransaction.getQuestion(), true).hasQuery()) {
            
            List<String> commandLine = new ArrayList<String>();
            
            
            
            if (absoluteQueryProcessorPath) {
                commandLine.add(executablePicker.pickAnExecutable(new File(searchTransaction.getQuestion().getCollection().getConfiguration()
                        .value(Keys.QUERY_PROCESSOR))).toString());
            } else {
                commandLine.add(executablePicker.pickAnExecutable(new File(searchHome, DefaultValues.FOLDER_BIN + File.separator
                        + searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.QUERY_PROCESSOR)))
                    .getAbsolutePath());
            }

            commandLine.addAll(searchTransaction.getQuestion().getDynamicQueryProcessorOptions());

            if (searchTransaction.getQuestion().getUserKeys().size() > 0) {
                if (OS.isFamilyWindows()) {
                    // On Windows the complete command-line option is quoted (including the dash + option name)
                    // so re-quoting it here is causing problems (FUN-6399)
                    commandLine.add(OPT_USER_KEYS + "="
                        + StringUtils.join(searchTransaction.getQuestion().getUserKeys().toArray(new String[0]), ","));
                } else {
                    commandLine.add(OPT_USER_KEYS + "=\""
                        + StringUtils.join(searchTransaction.getQuestion().getUserKeys().toArray(new String[0]), "\",\"")
                        + "\"");
                }                
            }
    
            Map<String, String> env = new HashMap<String, String>(
                    searchTransaction.getQuestion().getEnvironmentVariables());
            env.put(EnvironmentKeys.SEARCH_HOME.toString(), searchHome.getAbsolutePath());
            env.put(EnvironmentKeys.QUERY_STRING.toString(), getQueryString(searchTransaction));
            
            // Give Padre the originating IP address, accounting for X-Forwarded-For
            String remoteAddress = searchTransaction.getQuestion().getRequestId();
            if (remoteAddress != null) {
                env.put(PassThroughEnvironmentVariables.Keys.REMOTE_ADDR.toString(), remoteAddress);
            }
    
            // SystemRoot environment variable is MANDATORY for TRIM DLS checks
            // The TRIM SDK uses WinSock to connect to the remote server, and 
            // WinSock needs SystemRoot to initialise itself.
            if (System.getenv(EnvironmentKeys.SystemRoot.toString()) != null) {
                env.put(EnvironmentKeys.SystemRoot.toString(), System.getenv(EnvironmentKeys.SystemRoot.toString()));
            }
    
            PadreForkingOptionsHelper padreForkingOptions = new PadreForkingOptionsHelper(searchTransaction);

            ExecutionReturn padreOutput = null;
            
            try {
                padreOutput = runPadre(searchTransaction, commandLine, env, padreForkingOptions);
                
                if (log.isTraceEnabled()) {
                    try {
                        log.trace("Padre exit code is: " + padreOutput.getReturnCode() + "\n"
                            + "---- RAW result packet BEGIN ----:\n\n"
                                +new String(IOUtils.toByteArray(padreOutput.getOutBytes().get()), padreOutput.getCharset())
                                +"\n---- RAW result packet END ----");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    
                }

                updateTransaction(searchTransaction, padreOutput);
            } catch (PadreForkingException pfe) {
                log.error("PADRE forking failed with command line {}", getExecutionDetails(commandLine, env), pfe);
                throw new DataFetchException(i18n.tr("padre.forking.failed"), pfe);
            } catch (XmlParsingException pxpe) {
                log.error("Unable to parse PADRE response with command line {} {}", getExecutionDetails(commandLine, env),
                    Optional.ofNullable(padreOutput)
                            .map(o -> o.getReturnCode())
                            .map(rc -> "With exit code " + rc)
                            .orElse(""),
                    pxpe);
                if (padreOutput != null && padreOutput.getOutBytes() != null && padreOutput.getUntruncatedOutputSize() > 0) {
                    try {
                        log.error("PADRE response was: {}\n",
                                new String(IOUtils.toByteArray(padreOutput.getOutBytes().get()), padreOutput.getCharset()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    //This works around this issue created by: 
                    //https://issues.apache.org/jira/browse/LOG4J2-1434
                    //By writing this message after a large padre result packet,
                    //we drop the size of the cache from the thread local.
                    //
                    //We only bother to do this if the padre packet was big at least 1MB.
                    if(padreOutput.getUntruncatedOutputSize() > 1024 * 1024) {
                        log.error("Ignore this message, work around for LOG4J2-1434");
                    }
                }
                throw new DataFetchException(i18n.tr("padre.response.parsing.failed"), pxpe);
            }
        }
    }
    
    ExecutionReturn runPadre(SearchTransaction searchTransaction, 
                                List<String> commandLine,
                                Map<String, String> env,
                                PadreForkingOptionsHelper padreForkingOptions) throws DataFetchException, PadreForkingException {
        try {
            queryReadLock.lock(searchTransaction.getQuestion().getCollection());
        } catch (FileLockException e) {
            throw new DataFetchException(i18n.tr("padre.forking.lock.error"), e);
        }
        
        int padreWaitTimeout = (int) padreForkingOptions.getPadreForkingTimeout();
        try {
            if (searchTransaction.getQuestion().isImpersonated()) {
                return new WindowsNativePadreForker(i18n, (int) padreWaitTimeout).execute(commandLine, env, padreForkingOptions);
            } else {
                return new JavaPadreForker(i18n, padreWaitTimeout).execute(commandLine, env, padreForkingOptions);
            }
        } finally {
            queryReadLock.release(searchTransaction.getQuestion().getCollection());
        }
        
    }
    
    protected abstract String getQueryString(SearchTransaction transaction);
    
    protected abstract void updateTransaction(SearchTransaction transaction, ExecutionReturn padreOutput) throws XmlParsingException;
    
    private String getExecutionDetails(List cmdLine, Map<String, String> environment) {
        return " Command line was: "+cmdLine
            + System.getProperty("line.separator") + "Environment was: "+Arrays.asList(environment.entrySet().toArray());
    }
    
}
