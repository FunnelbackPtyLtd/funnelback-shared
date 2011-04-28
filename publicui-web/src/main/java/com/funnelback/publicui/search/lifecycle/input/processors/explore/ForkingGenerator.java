package com.funnelback.publicui.search.lifecycle.input.processors.explore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Setter;
import lombok.extern.apachecommons.Log;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;

@Component
@Log
public class ForkingGenerator implements ExploreQueryGenerator {

	/**
	 * We take advantage of knowing that the XML response has proper newline chars.  
	 */
	private static final Pattern PADRE_RF_XML_PATTERN = Pattern.compile("^query=(.*?)$", Pattern.DOTALL | Pattern.MULTILINE);
	
	@Autowired
	@Setter private File searchHome;
	
	@Setter private String padreRfBinary = "padre-rf";
	
	@Override
	public String getExploreQuery(Collection c, String url, Integer nbOfTerms) {
		File padreRfBin = new File(searchHome + File.separator + DefaultValues.FOLDER_BIN, padreRfBinary);
		File idxStem = null;
		try {
			idxStem = new File(c.getConfiguration().getCollectionRoot()
					+ File.separator + DefaultValues.VIEW_LIVE
					+ File.separator + DefaultValues.FOLDER_IDX
					+ File.separator + DefaultValues.INDEXFILES_PREFIX);
		} catch (FileNotFoundException fnfe) {
			return null;
		}
		
		CommandLine cmdLine = CommandLine.parse(
				padreRfBin.getAbsolutePath() + " "
				+ "-idx_stem=" + idxStem.getAbsolutePath() + " "
				+ ((nbOfTerms != null) ? "-exp=" + nbOfTerms.toString() + " ": "")
				+ "-url=" + url				
				);
		
		ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
		ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
		
		log.debug("Executing '" + cmdLine + "'");
		DefaultExecutor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(stdOut, stdErr, null));
		
		try {
			executor.execute(cmdLine);
			return getQuery(stdOut.toString());
		} catch (IOException ioe) {
			log.error("Error while running '" + cmdLine.toString() + "'", ioe);
		}
		
		return null;
	}
	
	private String getQuery(String padreRfXml) {
		Matcher m = PADRE_RF_XML_PATTERN.matcher(padreRfXml);
		if (m.find()) {
			return m.group(1);
		} else {
			return null;
		}
	}

}
