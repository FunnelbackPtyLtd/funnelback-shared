package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Executes a PADRE binary and get the results
 */
public interface PadreForker {

	/**
	 * Executes PADRE
	 * @param commandLine PADRE command line, with program path and arguments
	 * @param environmnent Environment variables to set
	 * @return Output of the command.
	 */
	public PadreExecutionReturn execute(String commandLine, Map<String, String> environmnent) throws PadreForkingException;
	
	/**
	 * PADRE response: A return code, and the content.
	 */
	@RequiredArgsConstructor
	public class PadreExecutionReturn {
		@Getter private final int returnCode;
		@Getter private final String output;
	}
	
}
