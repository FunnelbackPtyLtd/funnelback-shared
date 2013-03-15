package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.AbstractPadreForking.EnvironmentKeys;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Fetch the user keys from TRIM by running an external tool
 * that returns the keys through STDOUT.
 * 
 * @since 13.0
 */
@Log4j
public class TrimMapper implements UserKeysMapper {

    @Autowired
    private File searchHome;
    
    /**
     * Folder containing the binary to get the user keys,
     * relative to SEARCH_HOME
     */
    private final String GET_USER_KEYS_BINARY_PATH =
        DefaultValues.FOLDER_WINDOWS_BIN + File.separator + DefaultValues.FOLDER_TRIM;
    
    /** File name of the program to get the user keys */
    private final String GET_USER_KEYS_BINARY = "Funnelback.TRIM.GetUserKeys.exe";
    
    @Override
    public List<String> getUserKeys(SearchTransaction st) {
        List<String> out = new ArrayList<String>();
        
        if (SearchTransactionUtils.hasQuestion(st)) {
            if (st.getQuestion().getPrincipal() != null) {
                File getUserKeysBinary = new File(searchHome
                    + File.separator + GET_USER_KEYS_BINARY_PATH,
                    GET_USER_KEYS_BINARY);
                
                Map<String, String> env = new HashMap<String, String>();
                env.put(EnvironmentKeys.SEARCH_HOME.toString(), searchHome.getAbsolutePath());
                // SystemRoot environment variable is MANDATORY.
                // The TRIM SDK uses WinSock to connect to the remote server, and 
                // WinSock needs SystemRoot to initialise itself.
                if (System.getenv(EnvironmentKeys.SystemRoot.toString()) != null) {
                    env.put(EnvironmentKeys.SystemRoot.toString(),
                        System.getenv(EnvironmentKeys.SystemRoot.toString()));
                }
                
                ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                ByteArrayOutputStream stderr = new ByteArrayOutputStream();
                PumpStreamHandler streamHandler = new PumpStreamHandler(stdout, stderr, null);
                
                CommandLine cmdLine = CommandLine.parse(
                    getUserKeysBinary.getAbsolutePath()
                    + " -u " + st.getQuestion().getPrincipal().getName()
                    + " " + st.getQuestion().getCollection().getId());
                
                DefaultExecutor executor = new DefaultExecutor();
                executor.setWorkingDirectory(getUserKeysBinary.getParentFile());
                executor.setStreamHandler(streamHandler);
                
                try {
                    log.debug("Running user keys collector on collection '"
                        + st.getQuestion().getCollection().getId()+ "' for user '"
                        + st.getQuestion().getPrincipal().getName()
                        + "' with command line '" + cmdLine + "'");
                    int rc = executor.execute(cmdLine, env);
                    
                    String keys = stdout.toString();
                    out.add(keys);
                    
                    log.debug("Collected keys '"+keys+"' for user '"+st.getQuestion().getPrincipal().getName()+"'");
                    
                } catch (IOException ioe) {
                    log.error("Error while running user keys collector with command line '"
                        + cmdLine + "'. STDOUT was '"+stdout.toString()+"', STDERR was '"+stderr.toString()
                        + "'", ioe);
                }
                
                
            } else {
                log.warn(TrimMapper.class.getSimpleName() + " is enabled on collection '"+
                    st.getQuestion().getCollection().getId() + "' but the request is not impersonated."
                    + " Ensure "+Keys.ModernUI.AUTHENTICATION
                    +" is enabled and that Windows authentication is working.");
                
            }
        }
        
        return out;
    }

}
