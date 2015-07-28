package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.utils.ExecutionReturn;

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
                log.debug("PADRE returned a non-zero exit code: " + rc);
            }
            ExecutionReturn er = new ExecutionReturn(rc, padreOutput.toString(), padreError.toString());
            if(er.getErr().trim().equals("")) {
                log.warn(er.getErr());
            }
        } catch (ExecuteException ee) {
            throw new PadreForkingException(i18n.tr("padre.forking.java.failed", padreCmdLine.toString()), ee);
        } catch (IOException ioe) {
            throw new PadreForkingException(i18n.tr("padre.forking.java.failed", padreCmdLine.toString()), ioe);
        } finally {
            if (watchdog.killedProcess()) {
                log.error("Query processor exceeded timeout of " + padreWaitTimeout + "ms and was killed."
                        + " Command line was '"+padreCmdLine.toString()+"', environment was '"+environment.toString());
            }
        }
    }

}
