package com.funnelback.publicui.search.model.padre.factories;

import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.Explain;
import com.funnelback.publicui.search.model.padre.QuickLinks;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.xml.XmlStreamUtils;

public class ExplainFactory {
	public static Explain fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
		if (!Result.Schema.EXPLAIN.equals(xmlStreamReader.getLocalName())) {
			throw new IllegalArgumentException();
		}

		//Explain e = new QuickLinks(xmlStreamReader.getAttributeValue(null, QuickLinks.Schema.DOMAIN));

		float finalScore = 0;
		while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
			if (xmlStreamReader.isStartElement()) {
				if (Explain.Schema.FINAL_SCORE.equals(xmlStreamReader.getLocalName().toString())) {
					finalScore = Float.parseFloat(xmlStreamReader.getElementText());
/*					Map<String, String> qlMap = XmlStreamUtils.tagsToMap(QuickLinks.Schema.QUICKLINK, xmlStreamReader);
					q.getQuickLinks().add(
							new QuickLinks.QuickLink(
									qlMap.get(QuickLinks.Schema.QLTEXT),
									qlMap.get(QuickLinks.Schema.QLURL)));*/
				}
			}
		}

		return new Explain(finalScore);
	}
}
