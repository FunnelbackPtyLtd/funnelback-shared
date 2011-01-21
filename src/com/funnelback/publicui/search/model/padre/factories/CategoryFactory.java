package com.funnelback.publicui.search.model.padre.factories;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.Category;
import com.funnelback.publicui.search.model.padre.Cluster;

public class CategoryFactory {

	public static Category fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
		if( ! Category.Schema.CATEGORY.equals(xmlStreamReader.getLocalName()) ) {
			throw new IllegalArgumentException();
		}
		
		Category c = new Category(
				xmlStreamReader.getAttributeValue(null, Category.Schema.NAME),
				Integer.parseInt(xmlStreamReader.getAttributeValue(null, Category.Schema.MORE)));
		
		while(xmlStreamReader.hasNext()) {
			int type = xmlStreamReader.next();
			
			switch(type){
			case XMLStreamReader.START_ELEMENT:
				
				if (Cluster.Schema.CLUSTER.equals(xmlStreamReader.getLocalName())) {
					c.getClusters().add(ClusterFactory.fromXmlStreamReader(xmlStreamReader));
				}
				break;
			case XMLStreamReader.END_ELEMENT:
				if (Category.Schema.CATEGORY.equals(xmlStreamReader.getLocalName())) {
					// End of this category
					return c;
				}
				break;
			}
		}
		
		return c;
	}
}
