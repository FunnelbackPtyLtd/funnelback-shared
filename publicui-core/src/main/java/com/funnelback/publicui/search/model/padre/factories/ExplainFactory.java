package com.funnelback.publicui.search.model.padre.factories;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.Explain;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.xml.XmlStreamUtils;

public class ExplainFactory {
	public static Explain fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
		if (!Result.Schema.EXPLAIN.equals(xmlStreamReader.getLocalName())) {
			throw new IllegalArgumentException();
		}

		//Explain e = new QuickLinks(xmlStreamReader.getAttributeValue(null, QuickLinks.Schema.DOMAIN));

		float finalScore = 0;
		int consat = 0;
		float lenratio = 0;
		Map<String,String> stringFeatures = null;
		while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
			if (xmlStreamReader.isStartElement()) {
				if (Explain.Schema.FINAL_SCORE.equals(xmlStreamReader.getLocalName().toString())) {
					finalScore = Float.parseFloat(xmlStreamReader.getElementText());
				} if (Explain.Schema.CONSAT.equals(xmlStreamReader.getLocalName().toString())) {
					consat = Integer.parseInt(xmlStreamReader.getElementText());
				} if (Explain.Schema.LENRATIO.equals(xmlStreamReader.getLocalName().toString())) {
						lenratio = Float.parseFloat(xmlStreamReader.getElementText());
				} if (Explain.Schema.COOLER_SCORES.equals(xmlStreamReader.getLocalName().toString())) {
					stringFeatures = XmlStreamUtils.tagsToMap(Explain.Schema.COOLER_SCORES, xmlStreamReader);
				}
			}
		}
		
		if(stringFeatures == null) {
			throw new IllegalStateException("<explain> tag did not contain <cooler_scores>");
		}
		
		Map<String,Float> features = new HashMap<String,Float>();
		for (Map.Entry<String,String> feature : stringFeatures.entrySet()) {
			features.put(feature.getKey(), Float.parseFloat(feature.getValue()));
		}

		return new Explain(finalScore,consat,lenratio,Collections.unmodifiableMap(features));
	}
}
