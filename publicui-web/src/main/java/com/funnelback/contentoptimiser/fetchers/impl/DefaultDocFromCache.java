package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import com.funnelback.common.config.indexer.BuildInfoUtils;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.utils.cgirunner.CgiRunner;
import com.funnelback.common.utils.cgirunner.CgiRunnerException;
import com.funnelback.common.views.StoreView;
import com.funnelback.common.views.View;
import com.funnelback.contentoptimiser.fetchers.DocFromCache;
import com.funnelback.contentoptimiser.utils.CgiRunnerFactory;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.service.IndexRepository;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

@Log4j
@Component
public class DefaultDocFromCache implements DocFromCache {
    private static final String CACHE_CGI = "cache.cgi";

    @Autowired
    @Setter
    private File searchHome;
    
    @Autowired
    private I18n i18n;
    
    @Autowired @Setter
    private ConfigRepository configRepository;
    
    @Autowired
    private DataRepository dataRepository;

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
    public String getDocument(ContentOptimiserModel comparison, String cacheUrl,Config config,String collectionId) {
        // Create a temp directory to store the cache copy and the index
        File tempDir = Files.createTempDir();
        log.debug("Created tempdir");
        File cacheFile = new File(tempDir,"cachefile");
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

            RecordAndMetadata<RawBytesRecord> cached =
                (RecordAndMetadata<RawBytesRecord>)
                    dataRepository.getCachedDocument(
                        new Collection(collectionId, config),
                        StoreView.live,
                        comparison.getSelectedDocument().getIndexUrl());

            fos.write(cached.record.getContent());
        } catch (UnsupportedEncodingException e0) {
            log.error("Failed to decode url ", e0);
        } catch (IOException e1) {
            log.error("Failed to get document from cache",e1);
        }
        IOUtils.closeQuietly(printWriter);
        IOUtils.closeQuietly(fos);
        
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
        String indexerOptions = indexRepository.getBuildInfoValue(collectionId, BuildInfoUtils.BuildInfoKeys.indexer_arguments.toString());

        // We're only interested in options starting with a dash
        List<String> args = new ArrayList<String>();
        for (String line: indexerOptions.split(BuildInfoUtils.INDEXER_OPTIONS_SEPARATOR)) {
            if (line.length() > 0 && line.charAt(0) == '-') {
               args.add(line);     
            }
        }
        
        return args.toArray(new String[0]);
        
    }


}
