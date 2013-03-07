package com.funnelback.publicui.search.model.padre.factories;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.ClusterNav;

public class ClusterNavFactory {

    public static ClusterNav fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        if( ! ClusterNav.Schema.CLUSTER_NAV.equals(xmlStreamReader.getLocalName()) ) {
            throw new IllegalArgumentException();
        }
        
        String url = xmlStreamReader.getAttributeValue(null, ClusterNav.Schema.URL);
        Integer level = Integer.parseInt(xmlStreamReader.getAttributeValue(null, ClusterNav.Schema.LEVEL));
        
        return new ClusterNav(level, url, xmlStreamReader.getElementText());
    }
    
}
