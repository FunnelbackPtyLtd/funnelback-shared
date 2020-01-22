package com.funnelback.publicui.xml.padre;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.vibur.objectpool.ConcurrentPool;
import org.vibur.objectpool.PoolObjectFactory;
import org.vibur.objectpool.PoolService;
import org.vibur.objectpool.util.ConcurrentLinkedQueueCollection;

public class XMLStreamReaderCreator {

    private PoolService<XMLInputFactory> pool;
    
    public XMLStreamReaderCreator() {
        this.pool 
            = new ConcurrentPool<>(
                        new ConcurrentLinkedQueueCollection<>(), new PoolObjectFactory<XMLInputFactory>() {

                            @Override
                            public XMLInputFactory create() {
                                return XMLInputFactory.newInstance();
                            }

                            @Override
                            public void destroy(XMLInputFactory arg0) {
                            }

                            @Override
                            public boolean readyToRestore(XMLInputFactory arg0) {
                                return true;
                            }

                            @Override
                            public boolean readyToTake(XMLInputFactory arg0) {
                                return true;
                            }
                            
                        }, 1, // always have 1 
                        1, // at most have one, the operation on it is super fast so this is probably sufficient.
                        // I think we can generate about 1m per s, which is probably enough.
                        false);
                        
    }
    
    public XMLStreamReader createXMLStreamReader(InputStream is, String charset) 
            throws XMLStreamException {
        XMLInputFactory factory = this.pool.tryTake();
        if(factory != null) {
            try {
                return createXMLStreamReaderFrom(factory, is, charset);
            } finally {
                this.pool.restore(factory);
            }
        }
        // Couldn't get it immediately? Make a new one on the fly like we did before.
        return createXMLStreamReaderFrom(XMLInputFactory.newInstance(), is, charset);
    }
    
    private XMLStreamReader createXMLStreamReaderFrom(XMLInputFactory xmlInputFactory, InputStream is, String charset) throws XMLStreamException {
        return xmlInputFactory.createXMLStreamReader(is, charset);
    }
}
