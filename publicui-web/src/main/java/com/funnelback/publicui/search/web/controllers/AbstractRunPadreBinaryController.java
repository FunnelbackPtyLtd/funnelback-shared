package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.Log;

import org.apache.commons.exec.OS;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.AbstractPadreForking.EnvironmentKeys;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.JavaPadreForker;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForker.PadreExecutionReturn;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.utils.web.CGIEnvironment;

/**
 * Generic template of a controller to run a PADRE binary.
 * Assumes the binary resides in SEARCH_HOME/bin. The {@link HttpServletRequest}
 * will be converted to a CGI environment, and the output from PADRE will be
 * written to the {@link HttpServletResponse}.
 * 
 * Controllers can subclass it to easily map an URL to a PADRE binary.
 */
@Log
public abstract class AbstractRunPadreBinaryController {

	protected abstract File getSearchHome();
	
	private static final Pattern HEADER_CONTENT_PATTERN = Pattern.compile("^(.*?)\r?\n\r?\n(.*)$", Pattern.DOTALL);
	private static final String HEADER_NAME_SEPARATOR = ": ";

	/**
	 * Runs a PADRE binary.
	 * @param padreBinary Name of the binary (excluding extension if on Windows)
	 * @param options Command line options, or null
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws PadreForkingException
	 */
	protected final void runPadreBinary(String padreBinary,
			String options,
			HttpServletRequest request, HttpServletResponse response) throws IOException, PadreForkingException {
		CGIEnvironment cgi = new CGIEnvironment(request);

		Map<String, String> env = cgi.getEnvironment();
		env.put(EnvironmentKeys.SEARCH_HOME.toString(), getSearchHome().getAbsolutePath());

		String commandLine = new File(getSearchHome(),
				DefaultValues.FOLDER_BIN + File.separator
				+ padreBinary + ((OS.isFamilyWindows()) ? ".exe" : "")).getAbsolutePath();
		
		if (options != null) {
			commandLine += " " + options;
		}

		try {
			PadreExecutionReturn out = new JavaPadreForker().execute(commandLine, env);

			Matcher m = HEADER_CONTENT_PATTERN.matcher(out.getOutput());
			if (m.matches()) {

				// Output headers
				String[] headers = m.group(1).split("\r?\n");
				for (String header : headers) {
					String[] kv = header.split(HEADER_NAME_SEPARATOR);
					response.setHeader(kv[0], kv[1]);
				}

				// Output content
				response.getWriter().write(m.group(2));
			}
		} catch (PadreForkingException pfe) {
			log.error("Unable to run PADRE binary '" + padreBinary + "'", pfe);
			throw pfe;
		}
	}
}
