package com.funnelback.publicui.search.model.padre.factories;

import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.QuickLinks;
import com.funnelback.publicui.xml.XmlStreamUtils;

public class QuickLinksFactory {

	public static QuickLinks fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
		if (!QuickLinks.Schema.QUICKLINKS.equals(xmlStreamReader.getLocalName())) {
			throw new IllegalArgumentException();
		}

		QuickLinks q = new QuickLinks(xmlStreamReader.getAttributeValue(null, QuickLinks.Schema.DOMAIN));

		while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
			if (xmlStreamReader.isStartElement()) {
				if (QuickLinks.Schema.QUICKLINK.equals(xmlStreamReader.getLocalName().toString())) {
					Map<String, String> qlMap = XmlStreamUtils.tagsToMap(QuickLinks.Schema.QUICKLINK, xmlStreamReader);
					q.getQuickLinks().add(
							new QuickLinks.QuickLink(
									qlMap.get(QuickLinks.Schema.QLTEXT),
									qlMap.get(QuickLinks.Schema.QLURL)));
				}
			}
		}

		return q;
	}
}
