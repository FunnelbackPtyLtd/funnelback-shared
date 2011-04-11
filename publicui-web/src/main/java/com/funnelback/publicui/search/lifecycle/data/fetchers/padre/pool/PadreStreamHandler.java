package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.pool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import lombok.Getter;
import lombok.extern.apachecommons.Log;

import org.apache.commons.exec.ExecuteStreamHandler;

import com.funnelback.publicui.search.model.padre.Details;

/**
 * Handles process streams for a resident PADRE instance.
 */
@Log
public class PadreStreamHandler implements ExecuteStreamHandler {

	private InputStream processErrorStream;
	@Getter private OutputStream processInputStream;
	private InputStream processOutputStream;
	
	@Getter private BufferedReader outputStreamReader;
	
	/**
	 * Header read when PADRE starts (Collection details)
	 */
	@Getter private String header;
	
	/**
	 * Whenever the underlying PADRE instance is ready to be used.
	 * This is false until the PADRE header has been read.
	 */
	private boolean ready = false;
	
	@Override
	public void setProcessErrorStream(InputStream es) throws IOException {
		this.processErrorStream = es;
	}

	@Override
	public void setProcessInputStream(OutputStream is) throws IOException {
		this.processInputStream = is;
	}

	@Override
	public void setProcessOutputStream(InputStream os) throws IOException {
		this.processOutputStream = os;
		outputStreamReader = new BufferedReader(new InputStreamReader(processOutputStream));
	}

	@Override
	public void start() throws IOException {
		// Read the beginning of the packet (<details> tag)
		log.debug("Starting stream handling");

		String line = null;
		StringBuffer out = new StringBuffer();
		while ( (line = outputStreamReader.readLine()) != null) {
			out.append(line).append("\n");
			if (line.contains("</" + Details.Schema.DETAILS + ">")) {
				break;
			}
		}
		
		header = out.toString();
		ready = true;
	}

	@Override
	public void stop() {
		log.debug("Stopping stream handling");
		try {
			processInputStream.write(0x03);
		
			processInputStream.close();
			processErrorStream.close();
			processOutputStream.close();
		} catch (IOException ioe) {
			log.error("Unable to close streams", ioe);
		}

	}

	public boolean isReady() {
		return ready;
	}
	
}
