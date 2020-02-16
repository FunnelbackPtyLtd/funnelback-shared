package com.funnelback.publicui.search.model.padre.factories;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.CoolerWeighting;
import com.funnelback.publicui.search.model.padre.Explain;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

public class ExplainFactory {
    public static Explain fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        if (!Result.Schema.EXPLAIN.equals(xmlStreamReader.getLocalName())) {
            throw new IllegalArgumentException();
        }

        //Explain e = new QuickLinks(xmlStreamReader.getAttributeValue(null, QuickLinks.Schema.DOMAIN));

        float finalScore = 0;
        int consat = 0;
        float lenratio = 0;
        Map<CoolerWeighting, String> stringFeatures = new HashMap<CoolerWeighting, String>();
        while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
            if (xmlStreamReader.isStartElement()) {
                if (Explain.Schema.FINAL_SCORE.equals(xmlStreamReader.getLocalName().toString())) {
                    finalScore = Float.parseFloat(xmlStreamReader.getElementText());
                }
                if (Explain.Schema.CONSAT.equals(xmlStreamReader.getLocalName().toString())) {
                    consat = Integer.parseInt(xmlStreamReader.getElementText());
                }
                if (Explain.Schema.LENRATIO.equals(xmlStreamReader.getLocalName().toString())) {
                    lenratio = Float.parseFloat(xmlStreamReader.getElementText());
                }
                if (Explain.Schema.COOLER_SCORES.equals(xmlStreamReader.getLocalName().toString())) {
                    while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
                        StaxStreamParser.CoolValue cv = StaxStreamParser.parseCoolValue(xmlStreamReader);
                        stringFeatures.put(new CoolerWeighting(cv.name, cv.id), cv.value);
                    }
                }
            }
        }
        
        if(stringFeatures.isEmpty()) {
            throw new IllegalStateException("<explain> tag did not contain <cooler_scores>");
        }
        
        Map<CoolerWeighting,Float> features = new HashMap<CoolerWeighting,Float>();
        for (Map.Entry<CoolerWeighting, String> feature : stringFeatures.entrySet()) {
            features.put(feature.getKey(), Float.parseFloat(feature.getValue()));
        }

        return new Explain(finalScore,consat,lenratio,Collections.unmodifiableMap(features));
    }
}
