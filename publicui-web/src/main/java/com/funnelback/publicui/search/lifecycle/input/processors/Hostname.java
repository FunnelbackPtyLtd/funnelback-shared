package com.funnelback.publicui.search.lifecycle.input.processors;

import com.funnelback.common.views.View;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.springmvc.utils.web.LocalHostnameHolder;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Sets the relevant query processor option if the server
 * host name must be used in the queries log filename.
 * Sets the hostname in the question section if it 
 * is configured for display.
 * 
 * @see "FUN-3306"
 * @see "FUN-5071"
 * @see "FUN-6100"
 * @since v12.2
 */
@Log4j2
@Component("hostname")
public class Hostname extends AbstractInputProcessor {

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
                    + File.separator + View.live
                    + File.separator + DefaultValues.FOLDER_LOG,
                    "queries-"+localHostnameHolder.getShortHostname()+".log");
            String opt = QP_OPT_LOGFILE + "=" + logFile.getAbsolutePath();
            
            searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(opt);
            log.debug("Added QP option '"+opt+"'");
            
        }
        
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && searchTransaction.getQuestion().getCollection().getConfiguration()
                .valueAsBoolean(Keys.ModernUI.SHOW_HOSTNAME, DefaultValues.ModernUI.SHOW_HOSTNAME)) {
        	searchTransaction.getQuestion().setHostname(localHostnameHolder.getHostname());
        }
    }

}
