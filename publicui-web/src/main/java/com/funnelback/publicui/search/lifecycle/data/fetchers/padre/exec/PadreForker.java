package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.util.List;
import java.util.Map;

import com.funnelback.publicui.utils.ExecutionReturn;

/**
 * Executes a PADRE binary and get the results
 */
public interface PadreForker {

    /**
     * Executes PADRE
     * @param commandLine PADRE command line, with program path and arguments
     * @param environment Environment variables to set
     * @return Output of the command.
     * @throws PadreForkingException if something goes wrong
     */
    public ExecutionReturn execute(List<String> commandLine, Map<String, String> environment) throws PadreForkingException;  
    
}
