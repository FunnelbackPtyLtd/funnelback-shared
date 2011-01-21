package com.funnelback.publicui.search.lifecycle.data.fetcher.padre.xml.impl;

import java.io.StringReader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.data.fetcher.padre.xml.PadreXmlParser;
import com.funnelback.publicui.search.lifecycle.data.fetcher.padre.xml.PadreXmlParsingException;
import com.funnelback.publicui.search.model.padre.ContextualNavigation;
import com.funnelback.publicui.search.model.padre.Details;
import com.funnelback.publicui.search.model.padre.Error;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.search.model.padre.Spell;
import com.funnelback.publicui.search.model.padre.factories.ContextualNavigationFactory;
import com.funnelback.publicui.search.model.padre.factories.DetailsFactory;
import com.funnelback.publicui.search.model.padre.factories.ErrorFactory;
import com.funnelback.publicui.search.model.padre.factories.ResultFactory;
import com.funnelback.publicui.search.model.padre.factories.ResultsSummaryFactory;
import com.funnelback.publicui.search.model.padre.factories.SpellFactory;

@Component("padreXmlParser")
public class StaxStreamParser implements PadreXmlParser {

	@Override
	public ResultPacket parse(String xml) throws PadreXmlParsingException {

		ResultPacket packet = new ResultPacket();
		
		try {
			XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
		
			while(xmlStreamReader.hasNext()) {
				int type = xmlStreamReader.next();
				
				switch(type){
				case XMLStreamReader.START_ELEMENT:
					
					if (Details.Schema.DETAILS.equals(xmlStreamReader.getLocalName())) {
						packet.setDetails(DetailsFactory.fromXmlStreamReader(xmlStreamReader));
					} else if (ResultPacket.Schema.QUERY.equals(xmlStreamReader.getLocalName())) {
						packet.setQuery(xmlStreamReader.getElementText());
					} else if (ResultPacket.Schema.QUERY_AS_PROCESSED.equals(xmlStreamReader.getLocalName())) {
						packet.setQueryAsProcessed(xmlStreamReader.getElementText());
					} else if (ResultPacket.Schema.PADRE_ELAPSED_TIME.equals(xmlStreamReader.getLocalName())) {
						packet.setPadreElapsedTime(Integer.parseInt(xmlStreamReader.getElementText()));
					} else if (ResultsSummary.Schema.RESULTS_SUMMARY.equals(xmlStreamReader.getLocalName())) {
						packet.setResultsSummary(ResultsSummaryFactory.fromXmlStreamReader(xmlStreamReader));
					} else if (Spell.Schema.SPELL.equals(xmlStreamReader.getLocalName())) {
						packet.setSpell(SpellFactory.fromXmlStreamReader(xmlStreamReader));
					} else if (ResultPacket.Schema.RESULTS.equals(xmlStreamReader.getLocalName())) {
						packet.setResults(parseResults(xmlStreamReader));
					} else if (Error.Schema.ERROR.equals(xmlStreamReader.getLocalName())) {
						packet.setError(ErrorFactory.fromXmlStreamReader(xmlStreamReader));
					} else if (ResultPacket.Schema.RMC.equals(xmlStreamReader.getLocalName())) {
						RMC rmc = parseRmc(xmlStreamReader);
						packet.getRmcs().put(rmc.item, rmc.count);
					} else if (ContextualNavigation.Schema.CONTEXTUAL_NAVIGATION.equals(xmlStreamReader.getLocalName())) {
						packet.setContextualNavigation(ContextualNavigationFactory.fromXmlStreamReader(xmlStreamReader));
					}
					break;
				}
				
			}

			xmlStreamReader.close();
			return packet;
		} catch (XMLStreamException ioe) {
			throw new PadreXmlParsingException(ioe);
		}
		
	}

	private List<Result> parseResults(XMLStreamReader xmlStreamReader) throws XMLStreamException {
		List<Result> results = new ArrayList<Result>();
		
		int type = xmlStreamReader.getEventType();
		do {
			type = xmlStreamReader.next();
			
			switch(type) {
			case XMLStreamReader.START_ELEMENT:
				if (Result.Schema.RESULT.equals(xmlStreamReader.getLocalName())) {
					results.add(ResultFactory.fromXmlStreamReader(xmlStreamReader));
				}
				break;
			}
		} while( type != XMLStreamReader.END_ELEMENT || ( type == XMLStreamReader.END_ELEMENT && !ResultPacket.Schema.RESULTS.equals(xmlStreamReader.getLocalName())) );
		
		return results;
	}
	
	private RMC parseRmc(XMLStreamReader xmlStreamReader) throws NumberFormatException, XMLStreamException {
		if(!ResultPacket.Schema.RMC.equals(xmlStreamReader.getLocalName())) {
			throw new InvalidParameterException();
		}
		
		if ( xmlStreamReader.getAttributeCount() == 1
			&& ResultPacket.Schema.RMC_ITEM.equals(xmlStreamReader.getAttributeLocalName(0))) {
			RMC rmc = new RMC();
			rmc.item = xmlStreamReader.getAttributeValue(0);
			rmc.count = Integer.parseInt(xmlStreamReader.getElementText());
			return rmc; 
		}
		
		return null;
	}
	
	private class RMC {
		public String item;
		public int count;
	}
	
}
