package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import lombok.Setter;
import lombok.extern.apachecommons.Log;

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
import com.funnelback.common.utils.cgirunner.DefaultCgiRunner;
import com.funnelback.contentoptimiser.fetchers.DocFromCache;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.IndexRepository;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

@Log
@Component
public class DefaultDocFromCache implements DocFromCache {
	@Autowired
	@Setter
	private File searchHome;
	
	@Autowired
	private I18n i18n;
	
	@Autowired @Setter
	private ConfigRepository configRepository;
	
	@Autowired
	@Setter private IndexRepository indexRepository;
	
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
		sb.append(comparison.getImportantOne().getLiveUrl());
		sb.append("\">");
		sb.append(System.getProperty("line.separator"));
		sb.append("</DOCHDR>");
		
		PrintWriter printWriter = new PrintWriter(fos);
		printWriter.write(sb.toString());
		printWriter.flush();

		File perlBin = new File(configRepository.getExecutablePath(Keys.Executables.PERL));
		CgiRunner runner = new DefaultCgiRunner(
				new File(searchHome, DefaultValues.FOLDER_WEB + File.separator
						+ DefaultValues.FOLDER_PUBLIC + File.separator + config.value(Keys.UI_CACHE_LINK)),
				perlBin);
		try {
			runner.setRequestUrl(cacheUrl).setEnvironmentVariable("SEARCH_HOME", searchHome.getAbsolutePath()).run(fos);
		} catch (CgiRunnerException e1) {
			log.error("Failed to get document from cache",e1);
			comparison.getMessages().add(i18n.tr("error.callingCacheCgi"));
		}
		IOUtils.closeQuietly(printWriter);
		IOUtils.closeQuietly(fos);
		
		log.debug("....done");
		Executor indexDocument = new DefaultExecutor();
		CommandLine clIndexDocument = new CommandLine(new File(searchHome,  DefaultValues.FOLDER_BIN+ File.separator +  config.value(Keys.INDEXER)));
		String[] args = getArgsFromBldinfo(collectionId);
		
		log.debug("....done");
		try {
			log.debug("Indexing document");
			clIndexDocument.addArgument("-f");
			clIndexDocument.addArgument(cacheFile.getPath());
			clIndexDocument.addArgument(tempDir + File.separator + "index-single");
			clIndexDocument.addArguments(getArgsForSingleDocument(args));
			indexDocument.setStreamHandler(new PumpStreamHandler(null, null)); // ignore all indexer output
			indexDocument.execute(clIndexDocument);
		} catch (IOException e) {
			log.error("Failed to index document with command line " + clIndexDocument.toString(),e);
			comparison.getMessages().add(i18n.tr("error.callingIndexer"));
			return null;
		}
		log.debug("....done");
		String wordsInDoc;
		
		try {
			log.debug("Reading words in doc");
			wordsInDoc = Files.toString(new File(tempDir, "index-single.words_in_docs"), Charsets.UTF_8);	
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
