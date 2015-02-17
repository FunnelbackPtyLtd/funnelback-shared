package com.funnelback.publicui.search.service.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

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
import com.funnelback.common.views.View;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.CartClickLog;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.log.FacetedNavigationLog;
import com.funnelback.publicui.search.model.log.InteractionLog;
import com.funnelback.publicui.search.model.log.Log;
import com.funnelback.publicui.search.model.log.PublicUIWarningLog;
import com.funnelback.springmvc.utils.web.LocalHostnameHolder;

/**
 * Writes log files locally in the LIVE folder of each collection
 */
@Service
@Log4j
public class LocalLogService implements LogService {

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String XML_ROOT_START = "<log>";
    private static final String XML_ROOT_END = "</log>";

    /**
     * Line separator. Always use <code>\n</code> for now
     * as we need to be able to do trickery with the XML logs
     */
    private static final char LINE_SEP = '\n';

    @Autowired
    @Setter private File searchHome;

    @Autowired
    @Setter private LocalHostnameHolder localHostnameHolder;

    @Override
    public void logClick(ClickLog cl) {
        try {

            CSVWriter csvWriter= new CSVWriter(
            				new FileWriter(
	            				new File(cl.getCollection().getConfiguration().getLogDir(View.live),
	            					getLogName(cl.getCollection(),
	            						Files.Log.CLICKS_LOG_PREFIX,
	            						Files.Log.CLICKS_LOG_SEPARATOR,
	            						Files.Log.CLICKS_LOG_EXT)),
	            				true),
            				CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

            String[] entry = new String[Files.Log.CLICK_LOGS_COLUMNS];

            if(cl.getDate() != null) {
                entry[Files.Log.CLICK_LOG_COL_DATE] = Files.Log.DATE_FORMAT.format(cl.getDate());
            }

            entry[Files.Log.CLICK_LOG_COL_IP] = cl.getRequestId();
            if(cl.getReferer() != null) {
                entry[Files.Log.CLICK_LOG_COL_REFERRER] = cl.getReferer().toString();
            }
            entry[Files.Log.CLICK_LOG_COL_RANK] = Integer.toString(cl.getRank());
            if(cl.getTarget() != null) {
                entry[Files.Log.CLICK_LOG_COL_TARGET] = cl.getTarget().toString();
            }
            if(cl.getType() != null) {
                entry[Files.Log.CLICK_LOG_COL_TYPE] = cl.getType().toString();
            }
            if (cl.getUserId() != null) {
                entry[Files.Log.CLICK_LOG_COL_USER_ID] = cl.getUserId();
            } else {
                entry[Files.Log.CLICK_LOG_COL_USER_ID] = Log.USER_ID_NOTHING;
            }

            csvWriter.writeNext(entry);
            csvWriter.close();
        } catch (IOException e) {
            log.error("Error while writing to click log", e);
        }
    }

    @Override
    public void logCart(CartClickLog cl) {
        try {

            CSVWriter csvWriter= new CSVWriter(
            				new FileWriter(
	            				new File(cl.getCollection().getConfiguration().getLogDir(View.live),
	            					getLogName(cl.getCollection(),
	            						Files.Log.CART_CLICKS_LOG_PREFIX,
	            						Files.Log.CART_CLICKS_LOG_SEPARATOR,
	            						Files.Log.CART_CLICKS_LOG_EXT)),
	            				true),
            				CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

            String[] entry = new String[Files.Log.CART_CLICKS_LOGS_COLUMNS];

            if(cl.getDate() != null) {
                entry[Files.Log.CART_CLICKS_LOG_COL_DATE] = Files.Log.DATE_FORMAT.format(cl.getDate());
            }

            entry[Files.Log.CART_CLICKS_LOG_COL_IP] = cl.getRequestId();
            if(cl.getReferer() != null) {
                entry[Files.Log.CART_CLICKS_LOG_COL_REFERRER] = cl.getReferer().toString();
            }
            if(cl.getTarget() != null) {
                entry[Files.Log.CART_CLICKS_LOG_COL_TARGET] = cl.getTarget().toString();
            }
            if(cl.getType() != null) {
                entry[Files.Log.CART_CLICKS_LOG_COL_TYPE] = cl.getType().toString();
            }
            if (cl.getUserId() != null) {
                entry[Files.Log.CART_CLICKS_LOG_COL_USER_ID] = cl.getUserId();
            } else {
                entry[Files.Log.CART_CLICKS_LOG_COL_USER_ID] = Log.USER_ID_NOTHING;
            }

            csvWriter.writeNext(entry);
            csvWriter.close();
        } catch (IOException e) {
            log.error("Error while writing to cart clicks log", e);
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

    @Async
    @Override
    public void logFacetedNavigation(FacetedNavigationLog fnl) {
        if (fnl != null) {
            logLiveXmlData(fnl.getCollection(),
                Files.Log.FACETED_NAVIGATION_LOG_PREFIX,
                Files.Log.FACETED_NAVIGATION_LOG_EXT,
                fnl.toXml());
        }

    }

    @Override
    @Async
    public synchronized void logPublicUIWarning(PublicUIWarningLog warning) {
        File target = new File(searchHome + File.separator + DefaultValues.FOLDER_LOG,
            Files.Log.PUBLIC_UI_WARNINGS_FILENAME);

        FileWriter fw = null;
        try {
            fw = new FileWriter(target, true);
            fw.append(warning.toString() + LINE_SEP);
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
                + File.separator + View.live
                + File.separator + DefaultValues.FOLDER_LOG);

        File targetFile = new File(targetFolder, getLogName(c, fileName, "-", extension));

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
        StringBuffer out = new StringBuffer(XML_HEADER).append(LINE_SEP)
            .append(XML_ROOT_START).append(LINE_SEP)
            .append(xmlData).append(LINE_SEP)
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
            if (LINE_SEP == b) {
                i--;
                targetFile.seek(i);
            }

            // Try to find the previous line end
            for (; i>0; i--) {
                targetFile.seek(i);
                b = targetFile.readByte();
                if (LINE_SEP == b) {
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

    /**
     * Saves an interaction log in the relevant log directory in the relevant
     * collection.
     *
     * @param il
     *            the {@link InteractionLog} to log
     */
    @Async
    @Override
    public synchronized void logInteraction(InteractionLog il) {
        try {
            CSVWriter csvWriter = new CSVWriter(
            		new FileWriter(
            			new File(il.getCollection().getConfiguration().getLogDir(View.live),
            					getLogName(il.getCollection(),
            						Files.Log.INTERACTION_LOG_PREFIX,
            						Files.Log.INTERACTION_LOG_SEPARATOR,
            						Files.Log.INTERACTION_LOG_EXT))
            		, true));

            ArrayList<String> logToWrite = new ArrayList<String>();

            if (il.getDate() != null) {
                logToWrite.add(InteractionLog.DATE_FORMAT.format(il.getDate()));
            } else {
                logToWrite.add(null);
            }

            logToWrite.add(il.getRequestId());

            if (il.getReferer() != null) {
                logToWrite.add(il.getReferer().toString());
            } else {
                logToWrite.add(null);
            }

            logToWrite.add(il.getLogType());

            SortedSet<String> sortedKeys = new TreeSet<String>(il.getParameters().keySet());

            for (String key : sortedKeys) {
                String[] entry = il.getParameters().get(key);
                for (String element : entry) {
                    logToWrite.add(key + ":" + element);
                }
            }

            if (il.getUserId() != null) {
                logToWrite.add(il.getUserId());
            } else {
                logToWrite.add(Log.USER_ID_NOTHING);
            }

            csvWriter.writeNext(logToWrite.toArray(new String[0]));
            csvWriter.close();

        } catch (IOException e) {
            log.error("Error while writing to user interaction log", e);
        }
    }

    private String getLogName(Collection c, String prefix, String seperator, String extension){
    	String shortHostname = localHostnameHolder.getShortHostname();
    	if (shortHostname != null
                && ! localHostnameHolder.isLocalhost()
                && c.getConfiguration().valueAsBoolean(
                        Keys.Logging.HOSTNAME_IN_FILENAME,
                        DefaultValues.Logging.HOSTNAME_IN_FILENAME)) {
            // Use hostname in filename
            return prefix + seperator + shortHostname + extension;
        } else {
        	return prefix + extension;
        }
    }

}
