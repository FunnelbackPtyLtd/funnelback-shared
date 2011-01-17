package com.funnelback.publicui.search.lifecycle.data.fetcher.padre.exec;

import java.util.Map;

/**
 * Executes a PADRE binary and get the results
 * @author Administrator
 *
 */
public interface PadreForker {

	/**
	 * Executes PADRE
	 * @param commandLine PADRE command line, with program path and arguments
	 * @param environmnent Environment variables to set
	 * @return Output of the command.
	 */
	public String execute(String commandLine, Map<String, String> environmnent) throws PadreForkingException;
	
}
