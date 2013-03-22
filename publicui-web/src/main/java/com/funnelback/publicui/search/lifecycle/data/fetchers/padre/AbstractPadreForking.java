package com.funnelback.publicui.search.lifecycle.data.fetchers.padre;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashMap;
import java.util.Map;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.AbstractDataFetcher;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.JavaPadreForker;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.WindowsNativePadreForker;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.PadreXmlParser;

/**
 * Forks PADRE and communicate with it using stdin/out/err
 * 
 * Will choose either to fork using a Java API or Native Windows API
 * depending of the "impersonation" status of the transaction.
 */
@Log4j
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
    
    @Value("#{appProperties['padre.fork.timeout']?:30000}")
    @Setter
    protected int padreWaitTimeout;

    @Autowired
    protected I18n i18n;
    
    @Override
    public void fetchData(SearchTransaction searchTransaction) throws DataFetchException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && new PadreQueryStringBuilder(searchTransaction.getQuestion(), true).hasQuery()) {
            
            String commandLine = new File(searchHome,
                    DefaultValues.FOLDER_BIN                 
                    + File.separator
                    + searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.QUERY_PROCESSOR)).getAbsolutePath()
                    + " " + StringUtils.join(searchTransaction.getQuestion().getDynamicQueryProcessorOptions().toArray(new String[0]), " ");
            
            if (absoluteQueryProcessorPath) {
                commandLine = searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.QUERY_PROCESSOR)
                    + " " + StringUtils.join(searchTransaction.getQuestion().getDynamicQueryProcessorOptions().toArray(new String[0]), " ");
            }
            
            if (searchTransaction.getQuestion().getUserKeys().size() > 0) {
                commandLine += " " + OPT_USER_KEYS + "=\""
                + StringUtils.join(searchTransaction.getQuestion().getUserKeys().toArray(new String[0]), ",") + "\"";
            }
    
            Map<String, String> env = new HashMap<String, String>(searchTransaction.getQuestion().getEnvironmentVariables());
            env.put(EnvironmentKeys.SEARCH_HOME.toString(), searchHome.getAbsolutePath());
            env.put(EnvironmentKeys.QUERY_STRING.toString(), getQueryString(searchTransaction));
            
            // Give Padre the originating IP address, accounting for X-Forwarded-For
            String remoteAddress = SearchQuestionBinder.getRequestIp(searchTransaction.getQuestion());
            if (remoteAddress != null) {
                env.put(PassThroughEnvironmentVariables.Keys.REMOTE_ADDR.toString(),remoteAddress);
            }
    
            // SystemRoot environment variable is MANDATORY for TRIM DLS checks
            // The TRIM SDK uses WinSock to connect to the remote server, and 
            // WinSock needs SystemRoot to initialise itself.
            if (System.getenv(EnvironmentKeys.SystemRoot.toString()) != null) {
                env.put(EnvironmentKeys.SystemRoot.toString(), System.getenv(EnvironmentKeys.SystemRoot.toString()));
            }
    
            ExecutionReturn padreOutput = null;
            File indexUpdateLockFile = new File(searchTransaction.getQuestion().getCollection().getConfiguration().getCollectionRoot()
                    + File.separator + DefaultValues.VIEW_LIVE + File.separator + DefaultValues.FOLDER_IDX, Files.Index.UPDATE_LOCK);
            RandomAccessFile indexUpdateLockRandomFile = null;
            FileLock indexUpdateLock = null;
            
            try {
                indexUpdateLockRandomFile = new RandomAccessFile(indexUpdateLockFile, "rw");
                // Ask for a shared lock as multiple queries can happen at the same time
                indexUpdateLock = indexUpdateLockRandomFile.getChannel().lock(0, Long.MAX_VALUE, true);
                
                if (indexUpdateLock == null || ! indexUpdateLock.isValid()) {
                    log.error("Unable to obtain lock '"+indexUpdateLockFile.getAbsolutePath()+"'");
                    indexUpdateLockRandomFile.close();
                    throw new DataFetchException(i18n.tr("padre.forking.lock.error"), null);
                }
            } catch (IOException ioe) {
                log.error("Unable to obtain lock '"+indexUpdateLockFile.getAbsolutePath()+"'", ioe);
                throw new DataFetchException(i18n.tr("padre.forking.lock.error"), ioe);
            } catch (OverlappingFileLockException ofle) {
                // This can happen if the lock for this collection has already been acquired
                // by another thread or an extra search
                log.trace("Unable to obtain lock '"+indexUpdateLockFile.getAbsolutePath()+"' because it's already acquired", ofle);
            }
            
            try {
                if (searchTransaction.getQuestion().isImpersonated()) {
                    padreOutput = new WindowsNativePadreForker(i18n, padreWaitTimeout).execute(commandLine, env);
                } else {
                    padreOutput = new JavaPadreForker(i18n, padreWaitTimeout).execute(commandLine, env);
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
            } finally {
                // Close locks and associated resources.
                
                // indexUpdateLock might be null if we encountered an
                // OverlappingFileLockException earlier
                if (indexUpdateLock != null) {
                    try { indexUpdateLock.release(); }
                    catch (IOException ioe) { }
                }
                
                // But indexUpdateLockRandomFile is not supposed to be 
                try { indexUpdateLockRandomFile.close(); }
                catch (IOException ioe) { }
            }
        }
    }
    
    protected abstract String getQueryString(SearchTransaction transaction);
    
    protected abstract void updateTransaction(SearchTransaction transaction, ExecutionReturn padreOutput) throws XmlParsingException;
    
}
