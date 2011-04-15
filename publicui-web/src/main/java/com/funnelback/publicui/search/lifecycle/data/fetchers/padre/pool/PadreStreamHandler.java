package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.pool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import lombok.Getter;
import lombok.extern.apachecommons.Log;

import org.apache.commons.exec.ExecuteStreamHandler;

/**
 * Handles process streams for a resident PADRE instance.
 */
@Log
public class PadreStreamHandler implements ExecuteStreamHandler {

	private InputStream processErrorStream;
	@Getter private OutputStream processInputStream;
	private InputStream processOutputStream;
	
	@Getter private BufferedReader outputStreamReader;
	
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
	public void start() throws IOException { }

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
	
}
