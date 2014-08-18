package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.utils.cgirunner.CgiRunner;
import com.funnelback.common.utils.cgirunner.CgiRunnerException;
import com.funnelback.contentoptimiser.fetchers.DocFromCache;
import com.funnelback.contentoptimiser.utils.CgiRunnerFactory;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.IndexRepository;
import com.funnelback.warc.CompressionMode;
import com.funnelback.warc.io.indexed.Fix;
import com.funnelback.warc.io.indexed.ReadOnlyMapDbWarcFile;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

@Log4j
@Component
public class DefaultDocFromCache implements DocFromCache {
    private static final String CACHE_CGI = "cache.cgi";

    private static final String SEP = System.getProperty("file.separator");

    @Autowired
    @Setter
    private File searchHome;
    
    @Autowired
    private I18n i18n;
    
    @Autowired @Setter
    private ConfigRepository configRepository;
    
    @Autowired
    @Setter private IndexRepository indexRepository;
    
    @Autowired @Setter
    private CgiRunnerFactory cgiRunnerFactory;
    
    private final String ignoreIndexerOptionPrefixes[] = {
        "-forcexml",
        "-F",
        "-ifb",
        "-W",
        "-bigweb",
        "-big",
        "-annie",
        "-speller",
    }; 
    
    private final String provideOptionstoIndexer[] = {
        "-small",
        "-show_each_word_to_file",
        "-noank_record",
    };  
    
    /**
     * This method takes the options for a whole collection index, then:
     *   - removes the ones that don't make sense for a single document index
     *   - adds options that we should always use for a single document index 
     */
    @Override
    public String[] getArgsForSingleDocument(String[] wholeCollectionArgs) {
        List<String> orig = new ArrayList<String>(Arrays.asList(wholeCollectionArgs));
        
        Iterator<String> it = orig.iterator();
        while(it.hasNext()) {
            String s = it.next();
            for(String ignore : ignoreIndexerOptionPrefixes) {
                if(s.startsWith(ignore)) {
                    it.remove();
                    break;
                }
            }
        }
        orig.addAll(Arrays.asList(provideOptionstoIndexer));
        
        return orig.toArray(new String[0]);
    }
    
    /**
     * Extracts a document from the cache, and indexes it in a temporary location to find out the words in the document. 
     * 
     *  @return a string containing all the words in the document in the order that they appear.
     */
    @Override
    public String getDocument(ContentOptimiserModel comparison, String cacheUrl, Config config, String collectionId) {
        // Create a temp directory to store the cache copy and the index
        File tempDir = Files.createTempDir();
        log.debug("Created tempdir");
        File cacheFile = new File(tempDir,"cachefile");

        //Extract local copy into temp directory
        createLocalTempCopy(
            tempDir,
            cacheFile,
            cacheUrl,
            comparison);

        //Index locally and return document words
        String docWords = indexLocalTempCopyGetWords(tempDir, collectionId, cacheFile, config, comparison); 

        return docWords;
    }

    /** Gets a cached copy (identified by cacheUrl) out of the store,
     * and writes it to cacheFile in tempDir.
     */
    private void createLocalTempCopy(File tempDir, File cacheFile, String cacheUrl, ContentOptimiserModel comparison) {

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(cacheFile);
        } catch (FileNotFoundException e1) {        
            log.error("Error creating cachefile in tempdir: " + tempDir + File.pathSeparator + "cachefile",e1);
            comparison.getMessages().add(i18n.tr("error.creatingCacheFile"));
        }        
        log.debug("Obtaining doc from cache");

        StringBuilder sb = new StringBuilder();
        sb.append("<DOCHDR>");
        sb.append(System.getProperty("line.separator"));
        sb.append("  <BASE HREF=\"");
        sb.append(comparison.getSelectedDocument().getLiveUrl());
        sb.append("\">");
        sb.append(System.getProperty("line.separator"));
        sb.append("</DOCHDR>");
        
        PrintWriter printWriter = new PrintWriter(fos);
        printWriter.write(sb.toString());
        printWriter.flush();

        try {

            //If we're using mocks :(
            if(cacheUrl.equals("cache-url")) {
                finishCacheOldWarcStore(cacheUrl, fos);
            } else {

                Map<String, String> urlMap = getUrlMap(cacheUrl);
                String collectionId = urlMap.get("collection");
    
                Config conf = configRepository.getCollection(collectionId).getConfiguration();
    
                //If we're using the old warc store path
                if(conf.getConfigData()
                       .get("crawler.classes.URLStore")
                       .equals("com.funnelback.common.io.WARCStore")) {
                    finishCacheOldWarcStore(cacheUrl, fos);
                } else {
                    String targetUrl = URLDecoder.decode(urlMap.get("url"), "UTF-8"); 
                    File warcFileStem = new File(conf.getCollectionRoot().toString() 
                            + SEP + "live" + SEP + "data" + SEP + "funnelback-web-crawl");
                    
                    finishCacheNewWarcStore(collectionId, targetUrl, warcFileStem, fos);
                }
            }

        } catch (Exception e1) {
            log.error("Failed to get document from cache",e1);
            comparison.getMessages().add(i18n.tr("error.callingCacheCgi"));
        }

        IOUtils.closeQuietly(printWriter);
        IOUtils.closeQuietly(fos);
    }

    private void finishCacheNewWarcStore(String collectionId, String targetUrl, File warcFileStem, FileOutputStream fos) throws IOException {

        try (ReadOnlyMapDbWarcFile warc = new ReadOnlyMapDbWarcFile(warcFileStem, CompressionMode.COMPRESSED)) {
            warc.open(Fix.NO_AUTOFIX);

            String strContents =
                new String (warc
                    .get(targetUrl)
                    .decompressInPlace()
                    .getContents());

            fos.write(strContents.split("\r\n", 2)[1].getBytes());
        }
    }

    private void finishCacheOldWarcStore(String cacheUrl, FileOutputStream fos) throws CgiRunnerException {
        
        File perlBin = new File(configRepository.getExecutablePath(Keys.Executables.PERL));
        CgiRunner runner = cgiRunnerFactory.create(
                new File(searchHome, DefaultValues.FOLDER_WEB + File.separator
                        + DefaultValues.FOLDER_PUBLIC + File.separator + CACHE_CGI),
                perlBin);
        
        runner.setRequestUrl(cacheUrl).setEnvironmentVariable("SEARCH_HOME", searchHome.getAbsolutePath()).run(fos);
    }

    private static Map<String, String> getUrlMap(String url) {
        String[] urlParts = url.split("[?&]");
        HashMap<String,String> urlMap = new HashMap<String, String>();
        for(String s : urlParts) {
            String keyValues[] = s.split("=", 2);
            if(keyValues.length == 2) {
                urlMap.put(keyValues[0], keyValues[1]);
            }
        }
        return urlMap;
    }

    /** Indexes the entry in cacheFile, to pull out the words in the document.
     * Returns those words in a string */
    private String indexLocalTempCopyGetWords(File tempDir, String collectionId, File cacheFile, Config config, ContentOptimiserModel comparison) {
        File wordsInDocFile = new File(tempDir, "index-single.words_in_docs");
        log.debug("....done");
        Executor indexDocument = new DefaultExecutor();
        indexDocument.setWorkingDirectory(tempDir);
        CommandLine clIndexDocument = new CommandLine(new File(searchHome,  DefaultValues.FOLDER_BIN+ File.separator +  config.value(Keys.INDEXER)));
        String[] args = getArgsFromBldinfo(collectionId);
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        ByteArrayOutputStream errstream = new ByteArrayOutputStream();
        log.debug("....done");
        try {
            log.debug("Indexing document");
            clIndexDocument.addArgument("-f");            
            clIndexDocument.addArgument(cacheFile.getPath());
            clIndexDocument.addArgument(tempDir + File.separator + "index-single");
            clIndexDocument.addArguments(getArgsForSingleDocument(args));
            indexDocument.setStreamHandler(new PumpStreamHandler(outstream, errstream)); // record indexer output for debugging
            Map<String,String> env = new HashMap<String,String>();
            env.put("SEARCH_HOME",searchHome.getAbsolutePath());
            indexDocument.execute(clIndexDocument,env);
        } catch (IOException e) {
            log.error("Failed to index document with command line " + clIndexDocument.toString(),e);
            log.error("Failed indexer standard output was: " + outstream.toString());
            log.error("Failed indexer standard error was: " + errstream.toString());
        
            if(! wordsInDocFile.exists()) {
                comparison.getMessages().add(i18n.tr("error.callingIndexer"));
                return null;
            }
        }
        log.debug("....done");
        String wordsInDoc;
        
        try {
            log.debug("Reading words in doc");
            wordsInDoc = Files.toString(wordsInDocFile, Charsets.UTF_8);    
            FileUtils.deleteDirectory(tempDir);
        } catch (IOException e) {        
            log.error("Failed to open words in doc file after indexing",e);
            comparison.getMessages().add(i18n.tr("error.readingIndexedFile"));
            return null;
        }
        log.debug("Done");
        return wordsInDoc;
    }
    
    /**
     * This function obtains the previous indexer options from the given bldinfo file.
     * @param collectionId
     * @return the indexer options in the bldinfo file
     * @throws IOException
     */
    private String[] getArgsFromBldinfo(String collectionId) {
        String indexerOptions = indexRepository.getBuildInfoValue(collectionId, IndexRepository.BuildInfoKeys.indexer_arguments.toString());

        // We're only interested in options starting with a dash
        List<String> args = new ArrayList<String>();
        for (String line: indexerOptions.split(IndexRepository.INDEXER_OPTIONS_SEPARATOR)) {
            if (line.length() > 0 && line.charAt(0) == '-') {
               args.add(line);     
            }
        }
        
        return args.toArray(new String[0]);
        
    }


}
