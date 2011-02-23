package com.funnelback.publicui.search.service.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import lombok.Setter;
import lombok.extern.apachecommons.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.log.PublicUIWarningLog;

/**
 * Writes log files locally in the LIVE folder of each collection
 */
@Service
@Log
public class LocalLogService implements LogService {
	
	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String XML_ROOT_START = "<log>";
	private static final String XML_ROOT_END = "</log>";
	
	@Autowired
	@Setter private File searchHome;
	
	@Override
	public void logClick(ClickLog cl) {
		log.debug("NOT YET IMPLEMENTED");
	}

	@Override
	@Async
	public void logContextualNavigation(ContextualNavigationLog cnl) {
		if (cnl.getCollection() != null) {
			logLiveXmlData(cnl.getCollection(), Files.Log.CONTEXTUAL_NAVIGATION_LOG_FILENAME, cnl.toXml());
		}
	}
	
	@Override
	@Async
	public synchronized void logPublicUIWarning(PublicUIWarningLog warning) {
		File target = new File(searchHome + File.separator + DefaultValues.FOLDER_LOG, Files.Log.PUBLIC_UI_WARNINGS_FILENAME);
		FileWriter fw = null;
		try {
			fw = new FileWriter(target, true);
			fw.append(warning.toString() + "\n");
		} catch (IOException ioe) {
			log.warn("Error while writing to '" + target.getAbsolutePath() + "'", ioe);
		} finally {
			IOUtils.closeQuietly(fw);
		}
	}
	
	/**
	 * Update an XML log for the specified collection
	 * @param c
	 * @param fileName Relative to the live folder
	 * @param xmlData
	 */
	private void logLiveXmlData(Collection c, String fileName, String xmlData) {		
		File targetFile = null;
		try {
			targetFile = new File(c.getConfiguration().getCollectionRoot()
					+ File.separator + DefaultValues.VIEW_LIVE
					+ File.separator + DefaultValues.FOLDER_LOG,
					fileName);
			
			if (targetFile.exists()) {
				logXmlDataInExistingFile(targetFile, xmlData);
			} else {
				logXmlDataInNewFile(targetFile, xmlData);
			}
			
		} catch (IOException ioe) {
			if (targetFile != null) {
				log.error("Error while writing to log file '" + targetFile.getAbsolutePath() + "'", ioe);
			} else {
				log.error(ioe);
			}
		}
	}
	
	/**
	 * Creates a new log file and write the data in it.
	 * @param file
	 * @param xmlData
	 * @throws IOException
	 */
	private synchronized void logXmlDataInNewFile(File file, String xmlData) throws IOException {
		StringBuffer out = new StringBuffer(XML_HEADER).append("\n")
			.append(XML_ROOT_START).append("\n")
			.append(xmlData).append("\n")
			.append(XML_ROOT_END);
			
		FileUtils.write(file, out.toString());
	}
	
	/**
	 * Updates an existing XML log.
	 * As it's XML we have to keep it valid with a opening and closing root tag,
	 * so we need to seek at the end of the file, find the last line before the root tag
	 * and write our data.
	 * @param file
	 * @param xmlData
	 * @throws IOException
	 */
	private synchronized void logXmlDataInExistingFile(File file, String xmlData) throws IOException {
		RandomAccessFile targetFile = null;
		try {
			targetFile = new RandomAccessFile(file, "rw");
			
			// Skip any trailing \n in the file
			long i = targetFile.length()-1;
			targetFile.seek(i);
			byte b = targetFile.readByte();
			if (0x0a == b) {
				i--;
				targetFile.seek(i);
			}
			
			// Try to find the previous line end
			for (; i>0; i--) {
				targetFile.seek(i);
				b = targetFile.readByte();
				log.debug("Read '" + b + "'");
				if (0x0a == b) {
					break;
				}
			}
			
			if (i <= 0) {
				throw new IOException("Unable to find the closing XML tag in file '" + file.getAbsolutePath() + "'");
			}
			
			targetFile.writeBytes(xmlData + "\n");
			targetFile.writeBytes(XML_ROOT_END);
		} finally {
			IOUtils.closeQuietly(targetFile);
		}
	}

}
