package com.funnelback.publicui.search.model.padre.factories;


import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.Cluster;

public class ClusterFactory {

	public static Cluster fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws NumberFormatException, XMLStreamException {
		if( ! Cluster.Schema.CLUSTER.equals(xmlStreamReader.getLocalName()) ) {
			throw new IllegalArgumentException();
		}
		
		return new Cluster(
				xmlStreamReader.getAttributeValue(null, Cluster.Schema.HREF),
				Integer.parseInt(xmlStreamReader.getAttributeValue(null, Cluster.Schema.COUNT)),
				xmlStreamReader.getElementText());
	}
	
}
