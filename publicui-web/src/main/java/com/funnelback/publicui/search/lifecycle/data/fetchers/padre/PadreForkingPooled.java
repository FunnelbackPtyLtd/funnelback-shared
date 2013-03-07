package com.funnelback.publicui.search.lifecycle.data.fetchers.padre;

import java.io.IOException;

import lombok.extern.log4j.Log4j;

import org.apache.commons.pool.KeyedObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.AbstractDataFetcher;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.pool.PadreConnection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.PadreXmlParser;

/**
 * Uses a pool of forked PADRE communicate with them using stdin/out/err
 */
@Log4j
@Component("padreForkingPooled")
public class PadreForkingPooled extends AbstractDataFetcher {

    @Autowired
    private KeyedObjectPool padrePool;

    @Autowired
    private PadreXmlParser padreXmlParser;
    
    @Autowired
    private I18n i18n;

    @Override
    public void fetchData(SearchTransaction searchTransaction) throws DataFetchException {
        PadreConnection c = null;
        String padreOutput = null;
        try {
            c  = (PadreConnection) padrePool.borrowObject(searchTransaction.getQuestion().getCollection().getId());
                        
            padreOutput = c.inputCmd(new PadreQueryStringBuilder(searchTransaction.getQuestion(), true).buildCompleteQuery());
            
            searchTransaction.getResponse().setRawPacket(padreOutput.toString());
            searchTransaction.getResponse().setResultPacket(padreXmlParser.parse(padreOutput));
            searchTransaction.getResponse().setReturnCode(-1);
        } catch (IOException ioe) {
            log.error("Unable to communicate with PADRE", ioe);
            throw new DataFetchException(i18n.tr("padre.forking.pooled.communicate.failed"), ioe);
        } catch (XmlParsingException xpe) {
            log.error("Unable to parse PADRE response", xpe);
            log.error("PADRE response was: \n" + padreOutput);
            throw new DataFetchException(i18n.tr("padre.response.parsing.failed"), xpe);
        } catch (Exception e) {
            log.error("Unable get a PADRE connection", e);
            throw new DataFetchException(i18n.tr("padre.forking.pooled.connection.failed"), e);            
        } finally {
            if (c != null) {
                try {
                    padrePool.returnObject(searchTransaction.getQuestion().getCollection().getId(), c);
                } catch (Exception e) {
                    log.error("Unable to return PADRE connection to the pool for collection '" + searchTransaction.getQuestion().getCollection().getId() + "'", e);
                }
            }
            log.debug("Pool status: " + padrePool.getNumActive() + " active, " + padrePool.getNumIdle() + " idle");
        }

    }

}
