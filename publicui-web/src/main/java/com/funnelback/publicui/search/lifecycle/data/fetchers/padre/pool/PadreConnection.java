package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.pool;

import java.io.IOException;

import lombok.extern.apachecommons.Log;

import com.funnelback.common.padre.ResultPacket;
import com.funnelback.common.utils.Wait;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreExecutor;

/**
 * A pseudo connection to a resident PADRE binary.
 */
@Log
public class PadreConnection {

	/**
	 * Is this connection closed ?
	 */
	private boolean closed = true;
	
	private PadreExecutor executor;
	
	public PadreConnection(PadreExecutor executor) {
		this.executor = executor;
		closed = false;
	}
	
	/**
	 * Sends the givent command to the STDIN of PADRE
	 * @param cmd
	 * @return
	 * @throws IOException
	 */
	public String inputCmd(String cmd) throws IOException {
		if (closed) {
			throw new IllegalStateException("This connection is closed");
		}
		
		final PadreStreamHandler handler = (PadreStreamHandler)executor.getStreamHandler();
		new Wait() {
			
			@Override
			public boolean until() {
				return handler.isReady();
			}
		}.wait(1000, 1, "Readiness of PADRE stream handler");
				
		handler.getProcessInputStream().write((cmd+"\n").getBytes());
		handler.getProcessInputStream().flush();
		
		log.debug("Reading stdout until end results tag");
		StringBuffer out = new StringBuffer();
		String line;
		while ((line = handler.getOutputStreamReader().readLine()) != null) {
			out.append(line);
			if (line.contains("</" + com.funnelback.publicui.search.model.padre.ResultPacket.Schema.RESULTS + ">")) {
				break;
			}
		}

		return handler.getHeader()
			+ out.toString()
			+ "</" + ResultPacket.Schema.RESULT_PACKET + ">";

	}
	
	public void close() {
		log.debug("Closing connection");
		executor.getStreamHandler().stop();
	}
	
}
