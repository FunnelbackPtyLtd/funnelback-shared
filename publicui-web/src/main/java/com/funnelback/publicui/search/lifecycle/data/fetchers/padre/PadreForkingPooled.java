package com.funnelback.publicui.search.lifecycle.data.fetchers.padre;

import java.io.IOException;

import lombok.extern.apachecommons.Log;

import org.apache.commons.pool.KeyedObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.pool.PadreConnection;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.PadreXmlParser;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.xml.XmlParsingException;

/**
 * Uses a pool of forked PADRE communicate with them using stdin/out/err
 */
@Log
@Component("padreForkingPooled")
public class PadreForkingPooled implements DataFetcher {

	@Autowired
	private KeyedObjectPool padrePool;

	@Autowired
	private PadreXmlParser padreXmlParser;

	@Override
	public void fetchData(SearchTransaction searchTransaction) throws DataFetchException {
		PadreConnection c = null;
		String padreOutput = null;
		try {
			c  = (PadreConnection) padrePool.borrowObject(searchTransaction.getQuestion().getCollection().getId());
			
			padreOutput = c.inputCmd(PadreQueryStringBuilder.buildQuery(searchTransaction));
			
			searchTransaction.getResponse().setRawPacket(padreOutput.toString());
			searchTransaction.getResponse().setResultPacket(padreXmlParser.parse(padreOutput));
			searchTransaction.getResponse().setReturnCode(-1);
		} catch (IOException ioe) {
			log.error("Unable to communicate with PADRE", ioe);
			throw new DataFetchException(I18n.i18n().tr("Unable to communicate with PADRE"), ioe);
		} catch (XmlParsingException xpe) {
			log.error("Unable to parse PADRE output", xpe);
			log.error("PADRE output was: \n" + padreOutput);
			throw new DataFetchException(I18n.i18n().tr("Unable to parse PADRE output"), xpe);
		} catch (Exception e) {
			log.error("Unable get a PADRE connection", e);
			throw new DataFetchException(I18n.i18n().tr("Unable get a PADRE connection"), e);			
		} finally {
			if (c != null) {
				try {
					padrePool.returnObject(searchTransaction.getQuestion().getCollection().getId(), c);
				} catch (Exception e) {
					log.error("Unable to return PADRE connection to the pool for collection '" + searchTransaction.getQuestion().getCollection().getId() + "'", e);
				}
			}
		}

	}

}
