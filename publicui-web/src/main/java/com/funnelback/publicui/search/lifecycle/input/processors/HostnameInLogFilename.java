package com.funnelback.publicui.search.lifecycle.input.processors;

import java.io.File;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.web.LocalHostnameHolder;

/**
 * Sets the relevant query processor option if the server
 * host name must be used in the queries log filename.
 * 
 * @see FUN-3306
 * @see FUN-5071
 * @since v12.2
 */
@Log4j
@Component("hostnameInLogFilename")
public class HostnameInLogFilename extends AbstractInputProcessor {

    /** Name of the QP option to specify the log file */
    public static final String QP_OPT_LOGFILE = "-qlog_file";
    
    @Autowired
    @Setter private LocalHostnameHolder localHostnameHolder;
    
    @Override
    public void processInput(SearchTransaction searchTransaction)
        throws InputProcessorException {
        if (localHostnameHolder.getHostname() != null
                && ! localHostnameHolder.isLocalhost()
                && SearchTransactionUtils.hasCollection(searchTransaction)
                && searchTransaction.getQuestion().getCollection().getConfiguration()
                .valueAsBoolean(Keys.Logging.HOSTNAME_IN_FILENAME, DefaultValues.Logging.HOSTNAME_IN_FILENAME)) {
            
            File logFile = new File(
                    searchTransaction.getQuestion().getCollection().getConfiguration().getCollectionRoot()
                    + File.separator + DefaultValues.VIEW_LIVE
                    + File.separator + DefaultValues.FOLDER_LOG,
                    "queries-"+localHostnameHolder.getShortHostname()+".log");
            String opt = QP_OPT_LOGFILE + "=" + logFile.getAbsolutePath();
            
            searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(opt);
            log.debug("Added QP option '"+opt+"'");            
        }

    }

}
