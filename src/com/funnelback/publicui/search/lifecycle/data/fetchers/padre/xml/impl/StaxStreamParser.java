package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl;

import java.io.StringReader;
import java.security.InvalidParameterException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.PadreXmlParser;
import com.funnelback.publicui.search.model.padre.ContextualNavigation;
import com.funnelback.publicui.search.model.padre.Details;
import com.funnelback.publicui.search.model.padre.Error;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.search.model.padre.Spell;
import com.funnelback.publicui.search.model.padre.TierBar;
import com.funnelback.publicui.search.model.padre.factories.BestBetFactory;
import com.funnelback.publicui.search.model.padre.factories.ContextualNavigationFactory;
import com.funnelback.publicui.search.model.padre.factories.DetailsFactory;
import com.funnelback.publicui.search.model.padre.factories.ErrorFactory;
import com.funnelback.publicui.search.model.padre.factories.ResultFactory;
import com.funnelback.publicui.search.model.padre.factories.ResultsSummaryFactory;
import com.funnelback.publicui.search.model.padre.factories.SpellFactory;
import com.funnelback.publicui.search.model.padre.factories.TierBarFactory;
import com.funnelback.publicui.xml.XmlParsingException;

@Component("padreXmlParser")
public class StaxStreamParser implements PadreXmlParser {

	@Override
	public ResultPacket parse(String xml) throws XmlParsingException {

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
					} else if (ResultPacket.Schema.BEST_BETS.equals(xmlStreamReader.getLocalName())) {
						packet.getBestBets().addAll(BestBetFactory.listFromXmlStreamReader(xmlStreamReader));
					} else if (ResultPacket.Schema.RESULTS.equals(xmlStreamReader.getLocalName())) {
						parseResults(xmlStreamReader, packet);
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
			throw new XmlParsingException(ioe);
		}
		
	}

	private void parseResults(XMLStreamReader xmlStreamReader, ResultPacket packet) throws XMLStreamException {
		
		
		int type = xmlStreamReader.getEventType();
		TierBar lastTierBar = null;
		int tierBarFirstRank = 0;
		int tierBarLastRank = 0;
		do {
			type = xmlStreamReader.next();
			
			switch(type) {
			case XMLStreamReader.START_ELEMENT:
				if (Result.Schema.RESULT.equals(xmlStreamReader.getLocalName())) {
					packet.getResults().add(ResultFactory.fromXmlStreamReader(xmlStreamReader));
					tierBarLastRank++;
				} else if (TierBar.Schema.TIER_BAR.equals(xmlStreamReader.getLocalName())) {
					if ( lastTierBar != null) {
						// There was a tier bar before
						lastTierBar.setLastRank(tierBarLastRank);
						packet.getTierBars().add(lastTierBar);
						tierBarFirstRank = tierBarLastRank;
					}
					lastTierBar = TierBarFactory.fromXmlStreamReader(xmlStreamReader);
					lastTierBar.setFirstRank(tierBarFirstRank);
				}
				break;
			}
		} while( type != XMLStreamReader.END_ELEMENT || ( type == XMLStreamReader.END_ELEMENT && !ResultPacket.Schema.RESULTS.equals(xmlStreamReader.getLocalName())) );
		
		if ( lastTierBar != null) {
			// There was a last tier bar before
			lastTierBar.setLastRank(tierBarLastRank);
			packet.getTierBars().add(lastTierBar);
		}
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
