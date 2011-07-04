package com.funnelback.contentoptimiser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

@Log
@Component
public class DefaultDocFromCache implements DocFromCache {
	@Autowired
	@Setter
	File searchHome;
	
	@Autowired
	I18n i18n;
	
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
	public String getDocument(UrlComparison comparison, String cacheUrl,Config config) {
		// Create a temp directory to store the cache copy and the index
		File tempDir = Files.createTempDir();
		log.info("Created tempdir");

		Executor getCache = new DefaultExecutor();			
		CommandLine clGetCache = new CommandLine(new File(searchHome, DefaultValues.FOLDER_WEB + File.separator + DefaultValues.FOLDER_PUBLIC + File.separator + config.value(Keys.UI_CACHE_LINK)));
		File cacheFile = new File(tempDir,"cachefile");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(cacheFile);
			// Get this document from cache, and put it in <tempDir>/cachefile
			log.info("Obtaining doc from cache");
			clGetCache.addArgument(cacheUrl);
			getCache.setStreamHandler(new PumpStreamHandler(fos, null));
			getCache.execute(clGetCache);
		} catch (FileNotFoundException e1) {
			log.error("File not found " + tempDir + File.pathSeparator + "cachefile");
			comparison.getMessages().add(i18n.tr("error.creatingCacheFile"));
			return null;
		} catch (IOException e) {
			log.error("Failed to get document from cache with command line " + clGetCache.toString());
			comparison.getMessages().add(i18n.tr("error.callingCacheCgi"));
			return null;
		} finally {
			IOUtils.closeQuietly(fos);
		}
		log.info("....done");
		Executor indexDocument = new DefaultExecutor();
		CommandLine clIndexDocument = new CommandLine(new File(searchHome,  DefaultValues.FOLDER_BIN+ File.separator +  config.value(Keys.INDEXER)));
		File bldinfo = null;
		try {
			log.info("reading bldinfo");
			bldinfo = new File(config.getCollectionRoot(), DefaultValues.VIEW_LIVE + File.separator + DefaultValues.FOLDER_IDX + File.separator + DefaultValues.INDEXFILES_PREFIX + ".bldinfo");
		} catch (FileNotFoundException e) {
			comparison.getMessages().add(i18n.tr("error.configFileNotFound"));
		}

		String[] args = new String[0];
		try {
			args = getArgsFromBldinfo(bldinfo);
		} catch (IOException e1) {
			log.error("Failed to read bldinfo file '" + bldinfo.toString() + "': " + e1.getMessage());
			comparison.getMessages().add(i18n.tr("error.readingBldinfo"));
			return null;
		}
		log.info("....done");
		try {
			log.info("Indexing");
			clIndexDocument.addArgument("-f");
			clIndexDocument.addArgument(cacheFile.getPath());
			clIndexDocument.addArgument(tempDir + File.separator + "index-single");
			clIndexDocument.addArguments(getArgsForSingleDocument(args));
			log.info(clIndexDocument.toString());
			indexDocument.setStreamHandler(new PumpStreamHandler(null, null)); // ignore all indexer output
			indexDocument.execute(clIndexDocument);
		} catch (IOException e) {
			log.error("Failed to index document with command line " + clIndexDocument.toString());
			comparison.getMessages().add(i18n.tr("error.callingIndexer"));
			return null;
		}
		log.info("....done");
		String wordsInDoc;
		try {
			log.info("Reading words in doc");
			wordsInDoc = Files.toString(new File(tempDir, "index-single.words_in_docs"), Charsets.UTF_8);
			FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {		
			log.error("Failed to open words in doc file after indexing: " + e);
			comparison.getMessages().add(i18n.tr("error.readingIndexedFile"));
			return null;
		}
		log.info("Done");
		return wordsInDoc;
	}
	
	/**
	 * This function obtains the previous indexer options from the given bldinfo file.
	 * @param bldinfo - the bldinfo file
	 * @return the indexer options in the bldinfo file
	 * @throws IOException
	 */
	private String[] getArgsFromBldinfo(File bldinfo) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(bldinfo));
		String line;
		List<String> args = new ArrayList<String>();
		// The first 5 or 6 lines are typically something like this:
	    //   version FUNNELBACK_PADRE_9.0.0.30 64MDPLFS-VEC3-DNAMS2 (Web/Enterprise)
	    //   index_format: Linux-8.2
	    //   /home/dave/bin/padre-iw
	    //   -f
	    //   secondary_index.bundle
	    //   junk                                           
		// and can be safely ignored:
		for(int i = 0; i < 5;i++) in.readLine();
		
		// now read in the arguments
		while((line = in.readLine()) != null) {
			if (line.startsWith("idxch_classes: ")) {
				// This indicates the end of the args section
				break;
			}
		    if (line.length() > 0 && line.charAt(0) == '-') {
		    	// this is an argument, so we should remember it
			   args.add(line); 	
		    }
		}
		return args.toArray(new String[0]);
		
	}


}
