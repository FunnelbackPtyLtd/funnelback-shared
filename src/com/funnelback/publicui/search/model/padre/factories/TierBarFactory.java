package com.funnelback.publicui.search.model.padre.factories;

import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.XmlStreamUtils;
import com.funnelback.publicui.search.model.padre.TierBar;

public class TierBarFactory {

	
	public static TierBar fromMap(Map<String, String> data) {
		return new TierBar(
				Integer.parseInt(data.get(TierBar.Schema.MATCHED)),
				Integer.parseInt(data.get(TierBar.Schema.OUTOF)));
	}
	
	public static TierBar fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
		return fromMap(XmlStreamUtils.tagsToMap(TierBar.Schema.TIER_BAR, xmlStreamReader));
	}
}
