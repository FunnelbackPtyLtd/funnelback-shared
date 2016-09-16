package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.utils.ExecutionReturn;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Forks PADRE using Java API (Apache Commons Exec)
 */
@Log4j2
@RequiredArgsConstructor
public class JavaPadreForker implements PadreForker {

    /** Avg. size of a PADRE result packet */
    private final static int AVG_PADRE_PACKET_SIZE = 8*1024;
    
    /** Avg. size of PADRE error messages */
    private final static int AVG_PADRE_ERR_SIZE = 256;
    
    private final I18n i18n;

    protected final long padreWaitTimeout;
    
    @Override
    public ExecutionReturn execute(List<String> commandLine, Map<String, String> environment) throws PadreForkingException {
        
        if (commandLine == null || commandLine.size() < 1) {
            throw new PadreForkingException(i18n.tr("padre.forking.java.failed", ""), new IllegalArgumentException("No commandLine specified"));
        }
        
        CommandLine padreCmdLine = new CommandLine(commandLine.get(0));
        if (commandLine.size() > 1) {
            padreCmdLine.addArguments(commandLine.subList(1, commandLine.size()).toArray(new String[]{}), false);
        }
        
        ByteArrayOutputStream padreOutput = new ByteArrayOutputStream(AVG_PADRE_PACKET_SIZE);
        ByteArrayOutputStream padreError = new ByteArrayOutputStream(AVG_PADRE_ERR_SIZE);
        
        log.debug("Executing '" + padreCmdLine + "' with environment " + environment);
        
        PadreExecutor executor = new PadreExecutor();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(padreWaitTimeout);
        executor.setWatchdog(watchdog);

        PumpStreamHandler streamHandler = new PumpStreamHandler(padreOutput, padreError, null);
        
        executor.setStreamHandler(streamHandler);
        
        try {
            int rc = executor.execute(padreCmdLine, environment);
            if (rc != 0) {
                log.warn("PADRE returned a non-zero exit code: " + rc + getExecutionDetails(padreCmdLine, environment));
            }
            ExecutionReturn er = new ExecutionReturn(rc, padreOutput.toString(), padreError.toString());
            if(!er.getErr().trim().isEmpty()) {
                log.debug("PADRE printed the following to STDERR: " + er.getErr() + getExecutionDetails(padreCmdLine, environment));
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
