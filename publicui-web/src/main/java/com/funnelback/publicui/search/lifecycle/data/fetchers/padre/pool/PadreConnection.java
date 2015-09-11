package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.pool;

import java.io.IOException;

import lombok.extern.log4j.Log4j2;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreExecutor;
import com.funnelback.publicui.search.model.padre.ResultPacket;

/**
 * A pseudo connection to a resident PADRE binary.
 */
@Log4j2
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
     * Sends the given command to the STDIN of PADRE
     * @param cmd
     * @return
     * @throws IOException
     */
    public String inputCmd(String cmd) throws IOException {
        if (closed) {
            throw new IllegalStateException("This connection is closed");
        }
        
        final PadreStreamHandler handler = (PadreStreamHandler)executor.getStreamHandler();                
        handler.getProcessInputStream().write((cmd+"\n").getBytes());
        handler.getProcessInputStream().flush();
        
        log.debug("Reading stdout until end results tag");
        StringBuffer out = new StringBuffer();
        String line;
        while ((line = handler.getOutputStreamReader().readLine()) != null) {
            out.append(line);
            if (line.contains("</" + ResultPacket.Schema.PADRE_RESULT_PACKET + ">")) {
                break;
            }
        }

        return out.toString();
    }
    
    public void close() throws IOException {
        log.debug("Closing connection");
        executor.getStreamHandler().stop();
    }
    
}
