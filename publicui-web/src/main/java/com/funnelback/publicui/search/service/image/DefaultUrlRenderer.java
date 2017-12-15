package com.funnelback.publicui.search.service.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Renders URL using PhantomJS
 */
@Log4j2
@Component
public class DefaultUrlRenderer implements UrlRenderer {

    /** Thumbnail cache */
    protected static final String CACHE = DefaultUrlRenderer.class.getSimpleName() + "Repository";
    
    protected static final String PATTERN_KEY = "default_url_renderer.permitted_url_pattern";
    
    /** Name of the directory containing the PhantomJS binary */
    private static final String PHANTOMJS = "phantomjs";
    
    /** Possible folders where a PhantomJS binary could be found */
    private static final String[] LINUX_DIRECTORIES_CANDIDATES = new String[] {"centos6", "centos5", "local"};

    @Autowired
    private CacheManager appCacheManager;

    @Autowired
    private File searchHome;

    @Autowired
    private ConfigRepository configRepository;
    
    private File phantomBinary;
    
    /**
     * Find out which PhantomJS binary we should use, at startup time.
     */
    @PostConstruct
    public void setupPhantomBinary() {
        if (OS.isFamilyMac()) {
            phantomBinary = new File(searchHome,
                DefaultValues.FOLDER_MAC_BIN + File.separator
                + PHANTOMJS + File.separator
                + DefaultValues.FOLDER_BIN + File.separator + PHANTOMJS);
        } else if (OS.isFamilyWindows()) {
            phantomBinary = new File(searchHome,
                DefaultValues.FOLDER_WINDOWS_BIN + File.separator
                + PHANTOMJS + File.separator + DefaultValues.FOLDER_BIN + 
                File.separator + PHANTOMJS + ".exe");
        } else {
            phantomBinary = new File(searchHome,
                DefaultValues.FOLDER_LINUX_BIN + File.separator
                + PHANTOMJS + File.separator
                + DefaultValues.FOLDER_BIN + File.separator + PHANTOMJS);
        }
    }

    private static boolean doesPhantomBinaryWork(File phantomBinaryToTest) {
        int result = -1;
        try {
            CommandLine cmdLine = new CommandLine(phantomBinaryToTest.getAbsolutePath());
            cmdLine.addArgument("--version");
            
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValues(null); // executor should not check exit codes itself

            // capture the command's output
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            executor.setStreamHandler(streamHandler);

            // We need to force the phantomBinary to check the right lib path on linux
            Map<String, String> environment = new HashMap<String, String>(System.getenv());
            File libraryDirectory = new File(phantomBinaryToTest.getParentFile().getParentFile(), "lib");
            environment.put("LD_LIBRARY_PATH", libraryDirectory.getAbsolutePath());
            
            try {
                result = executor.execute(cmdLine, environment);
            } catch (Exception e) {
                result = -2;
            }
            
            log.debug(cmdLine.toString() + " exited with code " + result);
            log.debug(cmdLine.toString() + " output " + outputStream.toString());

        } catch (Exception e) {
            log.error(phantomBinaryToTest + " does not work", e);
            // Assume any exception means it does not work
            return false;
        }

        return result == 0;
    }

    @Override
    public byte[] renderUrl(String url, int width, int height)
        throws IOException {
        
        if (phantomBinary != null) {
            
            String permittedUrlPattern = configRepository.getGlobalConfiguration().value(PATTERN_KEY);
            if (permittedUrlPattern != null && !permittedUrlPattern.isEmpty()) {
                // Compare the URL to the pattern - Fail if it doesn't match
                if (! url.matches(permittedUrlPattern)) {
                    throw new RuntimeException("URL is not permitted according to " + PATTERN_KEY);
                }
            }
            
            Cache cache = appCacheManager.getCache(CACHE);
    
            String key = url + "|" + width + "|" + height;
    
            if (! cache.isKeyInCache(key)) {
                log.trace("Rendering " + url + " to cache with key " + key);
                
                File phantomPreview = new File(searchHome, "bin" + File.separator + "phantom_preview.js");
    
                File tempFile = null;
                try {
                    tempFile = File.createTempFile("phantom_preview", ".png");
                    
                    CommandLine cmdLine = new CommandLine(phantomBinary.getAbsolutePath());
                    cmdLine.addArgument(phantomPreview.getAbsolutePath());
                    cmdLine.addArgument(url);
                    cmdLine.addArgument(Integer.toString(width));
                    cmdLine.addArgument(Integer.toString(height));
                    cmdLine.addArgument(tempFile.getCanonicalPath());
                    DefaultExecutor executor = new DefaultExecutor();
                    
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
                    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
                    executor.setStreamHandler(streamHandler);
                    
                    // We need to force the phantomBinary to check the right lib path on linux
                    Map<String, String> environment = new HashMap<String, String>(System.getenv());
                    File libraryDirectory = new File(phantomBinary.getParentFile().getParentFile(), "lib");
                    environment.put("LD_LIBRARY_PATH", libraryDirectory.getAbsolutePath());
                    
                    int result = executor.execute(cmdLine, environment);
                    if (result != 0) {
                        throw new RuntimeException(cmdLine.toString() + " failed with code " + result);
                    }
            
                    byte[] imageBytes = FileUtils.readFileToByteArray(tempFile);
                    
                    if (imageBytes.length < 1) {
                        // Something went wrong
                        log.error("Preview for '"+url+"' returned an empty content. "
                            + "Process stdout was: '"+outputStream.toString()+"' "
                            + "Process stderr was: '"+errorStream.toString()+"' ");
                    }
    
                    cache.put(new Element(key, imageBytes));
                } finally {
                    if (tempFile != null && tempFile.exists()) {
                        tempFile.delete();
                    }
                }
                
            }
            
            return (byte[]) cache.get(key).getValue();
        } else {
            log.warn("Unable to render URL '"+url+"' because PhantomJS is unavailable. "
                + "Please check the application startup logs.");
            return new byte[0];
        }
    }
}
