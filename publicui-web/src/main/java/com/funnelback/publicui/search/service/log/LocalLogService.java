package com.funnelback.publicui.search.service.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import au.com.bytecode.opencsv.CSVWriter;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.log.PublicUIWarningLog;
import com.funnelback.publicui.utils.web.LocalHostnameHolder;

/**
 * Writes log files locally in the LIVE folder of each collection
 */
@Service
@Log4j
public class LocalLogService implements LogService {
	
	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String XML_ROOT_START = "<log>";
	private static final String XML_ROOT_END = "</log>";
	
	@Autowired
	@Setter private File searchHome;
	
	@Autowired
	@Setter private LocalHostnameHolder localHostnameHolder;
	
	@Autowired
	@Setter private ClickLogWriterHolder clickLogWriterHolder;
	
	@Override
	public void logClick(ClickLog cl) {
		try {
			String shortHostname = localHostnameHolder.getShortHostname();
			CSVWriter csvWriter;
			if(shortHostname != null) {
				csvWriter = new CSVWriter(
						clickLogWriterHolder.getWriter(cl.getCollection().getConfiguration().getLogDir(DefaultValues.VIEW_LIVE),
						Files.Log.CLICKS_LOG_PREFIX + Files.Log.CLICKS_LOG_SEPARATOR + shortHostname+ Files.Log.CLICKS_LOG_EXT));
			} else {
				csvWriter = new CSVWriter(
						clickLogWriterHolder.getWriter(cl.getCollection().getConfiguration().getLogDir(DefaultValues.VIEW_LIVE),
						Files.Log.CLICKS_LOG_PREFIX + Files.Log.CLICKS_LOG_EXT));
			}
			
			String[] entry = new String[6];
			
			if(cl.getDate() != null) entry[0] = ClickLog.DATE_FORMAT.format(cl.getDate());
			entry[1] = cl.getRequestIp();
			if(cl.getReferer() != null) entry[2] = cl.getReferer().toString();
			entry[3] = "" + cl.getRank();
			if(cl.getTarget() != null) entry[4] = cl.getTarget().toString();
			if(cl.getType() != null) entry[5] = cl.getType().toString();
						
			csvWriter.writeNext(entry);
			csvWriter.close();
		} catch (IOException e) {
			log.error("Unable to open clicks.log", e);
		}
	}

	@Override
	@Async
	public void logContextualNavigation(ContextualNavigationLog cnl) {
		if (cnl.getCollection() != null) {
			logLiveXmlData(cnl.getCollection(),
					Files.Log.CONTEXTUAL_NAVIGATION_LOG_PREFIX,
					Files.Log.CONTEXTUAL_NAVIGATION_LOG_EXT,
					cnl.toXml());
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
	 * @param fileName Relative to the live folder, without the extension
	 * @param extension File extension, with the leading dot
	 * @param xmlData
	 */
	private void logLiveXmlData(Collection c, String fileName, String extension, String xmlData) {

		File targetFolder = new File(c.getConfiguration().getCollectionRoot()
				+ File.separator + DefaultValues.VIEW_LIVE
				+ File.separator + DefaultValues.FOLDER_LOG);
		
		File targetFile = new File(targetFolder, fileName + extension);
		
		if (localHostnameHolder.getHostname() != null
				&& ! localHostnameHolder.isLocalhost()
				&& c.getConfiguration().valueAsBoolean(
						Keys.Logging.HOSTNAME_IN_FILENAME,
						DefaultValues.Logging.HOSTNAME_IN_FILENAME)) {
			// Use hostname in filename
			targetFile = new File(targetFolder, fileName + "-" + localHostnameHolder.getShortHostname() + extension);
		}
		
		try {
			if (targetFile.exists()) {
				logXmlDataInExistingFile(targetFile, xmlData);
			} else {
				logXmlDataInNewFile(targetFile, xmlData);
			}
			
		} catch (IOException ioe) {
			log.error("Error while writing to log file '" + targetFile.getAbsolutePath() + "'", ioe);
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
			
		FileUtils.writeStringToFile(file, out.toString());
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
			try {
				targetFile.close();
			} catch (IOException ioe) {}			
		}
	}

}
