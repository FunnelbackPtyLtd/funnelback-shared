package com.funnelback.publicui.search.service.index.result;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.AbstractPadreForking;
import com.funnelback.publicui.search.model.padre.Result;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches result from an index using <code>padre-di</code>
 */
@Component
@Log4j
public class PadreDIResultFetcher implements ResultFetcher {

    /** Name of the <code>padre-di</code> executable */
    private static final String PADRE_DI = "padre-di";

    /**
     * Avg. size of the stdout response of padre-di, for the
     * initial size of the byte array holding the output
     */
    private static final int AVG_PADRE_DI_OUTPUT_SIZE = 2048;

    /**
     * Pattern to use to extract metadata lines out of <code>padre-di</code> output
     */
    private static final Pattern MD_PATTERN = Pattern.compile("^([a-zA-Z0-9]):\\s(.+)$");

    /** Separator between multiple metadata values */
    private static final String MD_SEPARATOR = Pattern.quote("|");

    /** Label used by PADRE to sum up Field values */
    private static final String FIELD = "Field";

    /** Metadata class for the title */
    private static final String MD_TITLE = "t";

    /** Metadata class for the summary */
    private static final String MD_SUMMARY = "c";

    /** Location of the padre-di binary, platform specific */
    @Setter
    private File padreDiExecutable;

    @Autowired
    @Setter
    private File searchHome;

    @Override
    public Result fetchResult(File indexStem, URI resultUri) {
        String str = getPadreDIOutput(indexStem, resultUri);
        if (str != null) {
            log.debug("padre-di output is: '"+str+"'");
            String[] lines = str.split("\r?\n");

            // FIXME If no result were found, there will be a line
            // containing the word "Field"...
            if (! lines[0].startsWith(FIELD)) {

                Result r = new Result();
                r.setSummary("");
                r.setIndexUrl(resultUri.toString());

                // line 0: DOCNUM + URL, then always <md_class>:<space><value>
                for (int i=1; i<lines.length;i++) {
                    Matcher m = MD_PATTERN.matcher(lines[i]);
                    if (m.find() && m.group(2) != null) {
                        String md = m.group(1);
                        String content = m.group(2);

                        switch (md) {
                            case MD_TITLE:
                                r.setTitle(content.split(MD_SEPARATOR)[0]);
                                break;
                            case MD_SUMMARY:
                                r.setSummary(content);
                                break;
                            default:
                                r.getMetaData().put(md, content);
                        }
                    } else {
                        // No more metadata
                        break;
                    }
                }

                return r;
            }
        }

        return null;
    }

    private String getPadreDIOutput(File indexStem, URI resultUri) {
        Map<String, String> env = new HashMap<>();
        env.put(AbstractPadreForking.EnvironmentKeys.SEARCH_HOME.toString(), searchHome.getAbsolutePath());

        CommandLine cmd = new CommandLine(padreDiExecutable)
            .addArgument(indexStem.getAbsolutePath())
            .addArgument("-meta")
            .addArgument(resultUri.toString().replaceAll("^http://", "")); // Strip off http prefix for padre

        ByteArrayOutputStream padreOutput = new ByteArrayOutputStream(AVG_PADRE_DI_OUTPUT_SIZE);
        PumpStreamHandler streamHandler = new PumpStreamHandler(padreOutput, null);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);

        log.debug("Running padre-di with command line '"+cmd+"' and environment '"+env+"'");

        try {
            int rc = executor.execute(cmd, env);
            if (rc != 0) {
                log.warn(PADRE_DI + " exited with a non-zero exit code: "+rc);
                return null;
            } else {
                return padreOutput.toString("UTF-8");
            }
        } catch (Exception e) {
            log.error("Error while running padre-di with command line '"+cmd+"' and environment '"+env+"'", e);
            return null;
        }
    }

    @PostConstruct
    public void postConstruct() {
        padreDiExecutable = new File(searchHome
                + File.separator + DefaultValues.FOLDER_BIN,
                PADRE_DI + ((OS.isFamilyWindows()) ? ".exe" : ""));
    }
}
