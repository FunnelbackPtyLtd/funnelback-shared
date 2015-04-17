package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.AbstractPadreForking.EnvironmentKeys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor.ExecutionException;

/**
 * Fetch the user keys from TRIM by running an external tool
 * that returns the keys through STDOUT.
 * 
 * @since 13.0
 */
@Log4j2
public abstract class AbstractTrimMapper implements UserKeysMapper {

    /** How much time to wait for the user keys collector to return */
    private static final int WAIT_TIMEOUT = 1000*60;
    
    /**
     * Possible format for key strings, depending of the locks / keys
     * plugin configured in padre-sw
     */
    protected static enum KeyStringFormat { v1, v2 };
    
    /**
     * Folder containing the binary to get the user keys,
     * relative to SEARCH_HOME
     */
    private final static String GET_USER_KEYS_BINARY_PATH =
        DefaultValues.FOLDER_WINDOWS_BIN + File.separator + DefaultValues.FOLDER_TRIM;
    
    /** File name of the program to get the user keys */
    private final static String GET_USER_KEYS_BINARY = "Funnelback.TRIM.GetUserKeys.exe";

    /** Exit code when retrieving user keys was successful */
    private static final int GET_USER_KEYS_SUCCESS = 0;

    @Autowired
    private File searchHome;
    
    @Autowired
    private I18n i18n;

    /**
     * @return The key string format to get
     */
    protected abstract KeyStringFormat getKeyStringFormat();

    
    @Override
    public List<String> getUserKeys(Collection collection, SearchTransaction st) {
        List<String> out = new ArrayList<String>();
        
        if (SearchTransactionUtils.hasQuestion(st)) {
            if (st.getQuestion().getPrincipal() != null) {
                File getUserKeysBinary = new File(searchHome
                    + File.separator + GET_USER_KEYS_BINARY_PATH,
                    GET_USER_KEYS_BINARY);
                
                // Copy ALL the environment here. The TRIM SDK requires some environment
                // variables to be set, such as "SystemRoot" and "CommonProgramFiles"
                Map<String, String> env = new HashMap<>(System.getenv());
                // Force SEARCH_HOME, discarding any existing value in the environment
                env.put(EnvironmentKeys.SEARCH_HOME.toString(), searchHome.getAbsolutePath());

                List<String> cmdLine = new ArrayList<String>(Arrays.asList(new String[] {
                                getUserKeysBinary.getAbsolutePath(), "-f", getKeyStringFormat().name(),
                                collection.getId() }));
                
                try {
                    log.debug("Running user keys collector on collection '"
                        + collection.getId()+ "' for user '"
                        + st.getQuestion().getPrincipal().getName()
                        + "' with command line '" + cmdLine + "'");
                    
                    ExecutionReturn er = new WindowsNativeExecutor(i18n, WAIT_TIMEOUT)
                        .execute(cmdLine, env, getUserKeysBinary.getParentFile());
                    
                    String outStr = er.getOutput().trim();
                    
                    if (er.getReturnCode() != GET_USER_KEYS_SUCCESS) {
                        log.error("User keys collector returned a non-zero status ("
                            + er.getReturnCode()+") with command line '"
                            + cmdLine + "'. Output was '"+er.getOutput() + "'");
                    } else {
                        // This splits the strings on commas, but only those followed by
                        // the collection's id (to reduce the risk of commas in the trim group names
                        // being caught - I'm not sure if they'd be permitted by Trim)
                        String[] keys = outStr.split(",(?=" + Pattern.quote(collection.getId() + ":") + ")");
                        out.addAll(Arrays.asList(keys));
                        log.debug("Collected keys '"+out+"' for user '"
                            +st.getQuestion().getPrincipal().getName()+"'");
                    }
                    
                } catch (ExecutionException ee) {
                    log.error("Error while running user keys collector with command line '"
                        + cmdLine + "'", ee);
                }
                
                
            } else {
                log.warn(AbstractTrimMapper.class.getSimpleName() + " is enabled on collection '"+
                    st.getQuestion().getCollection().getId() + "' but the request is not impersonated."
                    + " Ensure "+Keys.ModernUI.AUTHENTICATION
                    +" is enabled and that Windows authentication is working.");
                
            }
        }
        
        return out;
    }
    
}
