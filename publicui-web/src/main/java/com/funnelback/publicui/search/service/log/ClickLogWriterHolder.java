package com.funnelback.publicui.search.service.log;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * This class exists to abstract the writer from the click logging code for testing purposes
 * 
 * @author tjones
 *
 */
public interface ClickLogWriterHolder {

	/**
	 * get the Writer for the given log file
	 * 
	 * @param logDir The directory to log
	 * @param fileName The filename to log to
	 * @return a buffered writer for logging
	 * @throws IOException If there is a problem creating the writer for the given file
	 */
	Writer getWriter(File logDir, String fileName) throws IOException;

}
