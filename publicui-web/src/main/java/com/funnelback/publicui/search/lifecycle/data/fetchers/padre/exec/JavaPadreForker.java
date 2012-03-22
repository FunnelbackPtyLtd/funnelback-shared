package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.PumpStreamHandler;

import com.funnelback.publicui.i18n.I18n;

/**
 * Forks PADRE using Java API (Apache Commons Exec)
 */
@Log4j
@RequiredArgsConstructor
public class JavaPadreForker implements PadreForker {

	private final I18n i18n;
	
	@Override
	public PadreExecutionReturn execute(String commandLine, Map<String, String> environmnent) throws PadreForkingException {
		
		CommandLine padreCmdLine = CommandLine.parse(commandLine);
		
		ByteArrayOutputStream padreOutput = new ByteArrayOutputStream();
		ByteArrayOutputStream padreError = new ByteArrayOutputStream();
		
		log.debug("Executing '" + padreCmdLine + "' with environment " + environmnent);
		
		PadreExecutor executor = new PadreExecutor();
		executor.setStreamHandler(new PumpStreamHandler(padreOutput, padreError, null));
		
		try {
			int rc = executor.execute(padreCmdLine, environmnent);
			return new PadreExecutionReturn(rc, padreOutput.toString());
		} catch (IOException ioe) {
			throw new PadreForkingException(i18n.tr("padre.forking.java.failed", padreCmdLine.toString()), ioe);
		}
	}

}
