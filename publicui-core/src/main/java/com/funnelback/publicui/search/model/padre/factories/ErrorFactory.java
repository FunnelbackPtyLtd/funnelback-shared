package com.funnelback.publicui.search.model.padre.factories;

import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.Error;
import com.funnelback.publicui.xml.XmlStreamUtils;

public class ErrorFactory {
    
    public static Error fromMap(Map<String, String> data) {
        return new Error(data.get(Error.Schema.USERMSG), data.get(Error.Schema.ADMINMSG));
    }
    
    public static Error fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        return fromMap(XmlStreamUtils.tagsToMap(Error.Schema.ERROR, xmlStreamReader));
    }
}
