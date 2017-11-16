package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.util.List;
import java.util.Map;

import com.funnelback.publicui.utils.ExecutionReturn;

/**
 * Executes a PADRE binary and get the results
 */
public interface PadreForker {

    /** Avg. size of a PADRE result packet in 2013 - See FUN-5364 */
    public final static int AVG_PADRE_PACKET_SIZE = 8*1024;
    
    /** Avg. size of PADRE error messages in 2013 - See FUN-5364 */
    public final static int AVG_PADRE_ERR_SIZE = 256;
    
    /**
     * Executes PADRE
     * @param commandLine PADRE command line, with program path and arguments
     * @param environment Environment variables to set
     * @param sizeLimit Maximum response size (to ensure memory isn't exhausted)
     * @return Output of the command.
     * @throws PadreForkingException if something goes wrong
     */
    public ExecutionReturn execute(List<String> commandLine, Map<String, String> environment, int sizeLimit) throws PadreForkingException;  
    
}
