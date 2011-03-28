package com.funnelback.publicui.search.model.padre.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.BestBet;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.xml.XmlStreamUtils;

public class BestBetFactory {

	public static BestBet fromMap(Map<String, String> data) {
		return new BestBet(
				data.get(BestBet.Schema.BB_TRIGGER),
				data.get(BestBet.Schema.BB_LINK),
				data.get(BestBet.Schema.BB_TITLE),
				data.get(BestBet.Schema.BB_DESC),
				data.get(BestBet.Schema.BB_LINK)		// Set click tracking = link by default
			);
	}
	
	public static BestBet fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
		return fromMap(XmlStreamUtils.tagsToMap(BestBet.Schema.BB, xmlStreamReader));
	}
	
	public static List<BestBet> listFromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
		if ( ! ResultPacket.Schema.BEST_BETS.equals(xmlStreamReader.getLocalName())) {
			throw new IllegalArgumentException();
		}
		
		List<BestBet> out = new ArrayList<BestBet>();
		
		int type = xmlStreamReader.getEventType();
		do {
			type = xmlStreamReader.next();
			
			switch(type) {
			case XMLStreamReader.START_ELEMENT:
				if (BestBet.Schema.BB.equals(xmlStreamReader.getLocalName())) {
					out.add(fromXmlStreamReader(xmlStreamReader));
				}
				break;
			}
		} while( type != XMLStreamReader.END_ELEMENT || ( type == XMLStreamReader.END_ELEMENT && !ResultPacket.Schema.BEST_BETS.equals(xmlStreamReader.getLocalName())) );
		
		return out;
	}
}
