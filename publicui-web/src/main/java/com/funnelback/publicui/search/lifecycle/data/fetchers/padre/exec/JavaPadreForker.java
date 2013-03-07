package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import com.funnelback.publicui.i18n.I18n;

/**
 * Forks PADRE using Java API (Apache Commons Exec)
 */
@Log4j
@RequiredArgsConstructor
public class JavaPadreForker implements PadreForker {

    /** Avg. size of a PADRE result packet */
    private final static int AVG_PADRE_PACKET_SIZE = 8*1024;
    
    /** Avg. size of PADRE error messages */
    private final static int AVG_PADRE_ERR_SIZE = 256;
    
    private final I18n i18n;

    protected final long padreWaitTimeout;
    
    @Override
    public PadreExecutionReturn execute(String commandLine, Map<String, String> environmnent) throws PadreForkingException {
        
        CommandLine padreCmdLine = CommandLine.parse(commandLine);
        
        ByteArrayOutputStream padreOutput = new ByteArrayOutputStream(AVG_PADRE_PACKET_SIZE);
        ByteArrayOutputStream padreError = new ByteArrayOutputStream(AVG_PADRE_ERR_SIZE);
        
        log.debug("Executing '" + padreCmdLine + "' with environment " + environmnent);
        
        PadreExecutor executor = new PadreExecutor();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(padreWaitTimeout);
        executor.setWatchdog(watchdog);

        PumpStreamHandler streamHandler = new PumpStreamHandler(padreOutput, padreError, null);
        
        executor.setStreamHandler(streamHandler);
        
        try {
            int rc = executor.execute(padreCmdLine, environmnent);
            return new PadreExecutionReturn(rc, padreOutput.toString());
        } catch (ExecuteException ee) {
            throw new PadreForkingException(i18n.tr("padre.forking.java.failed", padreCmdLine.toString()), ee);
        } catch (IOException ioe) {
            throw new PadreForkingException(i18n.tr("padre.forking.java.failed", padreCmdLine.toString()), ioe);
        } finally {
            if (watchdog.killedProcess()) {
                log.error("Query processor exceeded timeout of " + padreWaitTimeout + "ms and was killed."
                        + " Command line was '"+padreCmdLine.toString()+"', environment was '"+environmnent.toString());
            }
        }
    }

}
