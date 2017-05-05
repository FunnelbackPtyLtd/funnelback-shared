package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.exec.OS;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.AbstractPadreForking.EnvironmentKeys;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.JavaPadreForker;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.web.controllers.session.SessionController;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.utils.web.CGIEnvironment;

/**
 * Generic template of a controller to run a PADRE binary.
 * Assumes the binary resides in SEARCH_HOME/bin. The {@link HttpServletRequest}
 * will be converted to a CGI environment, and the output from PADRE will be
 * written to the {@link HttpServletResponse}.
 * 
 * Controllers can subclass it to easily map an URL to a PADRE binary.
 */
@Log4j2
public abstract class AbstractRunPadreBinaryController extends SessionController {

    protected abstract File getSearchHome();
    
    private static final Pattern HEADER_CONTENT_PATTERN = Pattern.compile("^(.*?)\r?\n\r?\n(.*)$", Pattern.DOTALL);
    private static final String HEADER_NAME_SEPARATOR = ": ";

    @Autowired
    private I18n i18n;

    /**
     * Runs a PADRE binary.
     * @param padreBinary Name of the binary (excluding extension if on Windows)
     * @param options Command line options, or null
     * @param request
     * @param response
     * @param detectHeaders Whereas to try to detect headers in the output and send them
     * @throws IOException
     * @throws PadreForkingException
     */
    protected final void runPadreBinary(String padreBinary,
            List<String> options,
            HttpServletRequest request, HttpServletResponse response,
            boolean detectHeaders,
            int sizeLimit) throws IOException, PadreForkingException {
        CGIEnvironment cgi = new CGIEnvironment(request);

        Map<String, String> env = cgi.getEnvironment();
        env.put(EnvironmentKeys.SEARCH_HOME.toString(), getSearchHome().getAbsolutePath());

        List<String> commandLine = new ArrayList<String>();
        commandLine.add(new File(getSearchHome(), DefaultValues.FOLDER_BIN + File.separator + padreBinary
            + ((OS.isFamilyWindows()) ? ".exe" : "")).getAbsolutePath());
        
        if (options != null) {
            commandLine.addAll(options);
        }

        try {
            ExecutionReturn out = new JavaPadreForker(i18n, DefaultValues.ModernUI.PADRE_FORK_TIMEOUT_MS).execute(commandLine, env, sizeLimit);

            String output = new String(IOUtils.toByteArray(out.getOutBytes()), out.getCharset());
            if (detectHeaders) {
                Matcher m = HEADER_CONTENT_PATTERN.matcher(output);
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
            } else {
                response.getWriter().write(output);                
            }
        } catch (PadreForkingException pfe) {
            log.error("Unable to run PADRE binary '" + padreBinary + "'", pfe);
            throw pfe;
        }
    }
}
