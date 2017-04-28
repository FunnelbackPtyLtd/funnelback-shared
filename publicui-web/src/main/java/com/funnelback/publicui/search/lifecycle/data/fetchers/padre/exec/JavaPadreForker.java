package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.utils.BoundedByteArrayOutputStream;
import com.funnelback.publicui.utils.ExecutionReturn;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Forks PADRE using Java API (Apache Commons Exec)
 */
@Log4j2
@RequiredArgsConstructor
public class JavaPadreForker implements PadreForker {

    private final I18n i18n;

    protected final long padreWaitTimeout;
    
    @Override
    public ExecutionReturn execute(List<String> commandLine, Map<String, String> environment, int sizeLimit) throws PadreForkingException {
        
        if (commandLine == null || commandLine.size() < 1) {
            throw new PadreForkingException(i18n.tr("padre.forking.java.failed", ""), new IllegalArgumentException("No commandLine specified"));
        }
        
        CommandLine padreCmdLine = new CommandLine(commandLine.get(0));
        try {
            //Try to place anything that will reference padre output in this try, so that when a OOM is caught
            //no references to the output are held allowing the gc to ckean up objects.
            if (commandLine.size() > 1) {
                padreCmdLine.addArguments(commandLine.subList(1, commandLine.size()).toArray(new String[]{}), false);
            }
            
            BoundedByteArrayOutputStream padreOutput = new BoundedByteArrayOutputStream(AVG_PADRE_PACKET_SIZE, sizeLimit);
            BoundedByteArrayOutputStream padreError = new BoundedByteArrayOutputStream(AVG_PADRE_ERR_SIZE, sizeLimit);
            
            log.debug("Executing '" + padreCmdLine + "' with environment " + environment);
            
            PadreExecutor executor = new PadreExecutor();
    
            ExecuteWatchdog watchdog = new ExecuteWatchdog(padreWaitTimeout);
            executor.setWatchdog(watchdog);
    
            PumpStreamHandler streamHandler = new PumpStreamHandler(padreOutput, padreError, null);
            
            executor.setStreamHandler(streamHandler);
            
            try {
                int rc = executor.execute(padreCmdLine, environment);
                
                if (padreOutput.isTruncated()) {
                    throw new PadreForkingException(i18n.tr("padre.forking.failed.sizelimit", padreCmdLine.toString()));
                }
                
                ExecutionReturn er = new ExecutionReturn(rc, padreOutput.toByteArray(), padreError.toByteArray(), StandardCharsets.UTF_8);
                
                //Ideally padre should never be writting to STDERR unless something is wrong with the collection.
                if(er.getErrBytes().length > 0) {
                    String error = new String(er.getErrBytes()).trim();
                    if(error.length() > 0) {
                        log.debug("PADRE printed the following to STDERR: '" + 
                            error + "' " + getExecutionDetails(padreCmdLine, environment));
                    }
                }
                
                //Log non zero exit codes. Sometimes non zero exit codes will be in a valid XML
                //Which will have some error messages displayed to the user.
                if(rc != 0 ) {
                    log.debug("Output for non zero exit code (code: {}) when running: {}\nSTDOUT:\n{}\nSTDERR\n{}",
                        rc,
                        getExecutionDetails(padreCmdLine, environment),
                            new String(er.getOutBytes(), StandardCharsets.UTF_8),
                            new String(er.getErrBytes(), StandardCharsets.UTF_8));    
                }
                
                if(rc == 139) {
                    //Seg faults are common to avoid support spending too long wondering what exit code 139 is
                    //just log it is a seg fault. If that is put into a Jira ticket any padre/c dev will pick it
                    //up immediately.
                    throw new PadreForkingException(i18n.tr("padre.forking.java.failed.seg.fault", padreCmdLine.toString(), rc));
                }
                
                return er;
            } catch (ExecuteException ee) {
                throw new PadreForkingException(i18n.tr("padre.forking.java.failed", padreCmdLine.toString()), ee);
            } catch (IOException ioe) {
                throw new PadreForkingException(i18n.tr("padre.forking.java.failed", padreCmdLine.toString()), ioe);
            } finally {
                if (watchdog.killedProcess()) {
                    log.error("Query processor exceeded timeout of " + padreWaitTimeout + "ms and was killed."
                        + getExecutionDetails(padreCmdLine, environment));
                }
            }
        } catch (OutOfMemoryError oome) {
            // Not sure if these are actually a good thing to do
            System.gc();
            throw new PadreForkingException(i18n.tr("padre.forking.java.oom", padreCmdLine.toString()), oome);
        }
    }
    
    /**
     * Get details about the PADRE execution environment as a string
     * @param cmdLine PADRE command line
     * @param environment Environment map
     * @return String containing the command line and the details of the environment map
     */
    private String getExecutionDetails(CommandLine cmdLine, Map<String, String> environment) {
        return " Command line was: "+cmdLine.toString()
            + System.getProperty("line.separator") + "Environment was: "+Arrays.asList(environment.entrySet().toArray());
    }

}
