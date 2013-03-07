package com.funnelback.publicui.search.model.padre.factories;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.RMCItemResult;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.xml.XmlStreamUtils;
import com.funnelback.publicui.xml.XmlStreamUtils.TagAndText;

/**
 * Builds {@link RMCItemResult}s from various input sources.
 * 
 */
public class RMCItemResultFactory {

    public static RMCItemResult fromMap(Map<String, String> data) {
        String title = data.get(Result.Schema.TITLE);
        String liveUrl = data.get(Result.Schema.LIVE_URL);
        String summary = data.get(Result.Schema.SUMMARY);

        return new RMCItemResult(title, liveUrl, summary);
    }

    public static RMCItemResult fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        if (!ResultPacket.Schema.RMC_ITEM_RESULT.equals(xmlStreamReader.getLocalName())) {
            throw new IllegalArgumentException();
        }

        Map<String, String> data = new HashMap<String, String>();
        
        while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
            if (xmlStreamReader.isStartElement()) {
                TagAndText tt = XmlStreamUtils.getTagAndValue(xmlStreamReader);
                data.put(tt.tag, tt.text);
            }
        }

        return fromMap(data);
    }

}
