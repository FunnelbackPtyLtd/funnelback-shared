package com.funnelback.publicui.search.model.padre.factories;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.Category;
import com.funnelback.publicui.search.model.padre.ClusterNav;
import com.funnelback.publicui.search.model.padre.ContextualNavigation;

public class ContextualNavigationFactory {

	public static ContextualNavigation fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws NumberFormatException, XMLStreamException {
		if( ! ContextualNavigation.Schema.CONTEXTUAL_NAVIGATION.equals(xmlStreamReader.getLocalName()) ) {
			throw new IllegalArgumentException();
		}
		
		ContextualNavigation cn = new ContextualNavigation();
		
		while(xmlStreamReader.hasNext()) {
			int type = xmlStreamReader.next();
			
			switch(type){
			case XMLStreamReader.START_ELEMENT:
				
				if (ContextualNavigation.Schema.SEARCH_TERMS.equals(xmlStreamReader.getLocalName())) {
					cn.setSearchTerm(xmlStreamReader.getElementText());
				} else if (ClusterNav.Schema.CLUSTER_NAV.equals(xmlStreamReader.getLocalName())) {
					cn.setClusterNav(ClusterNavFactory.fromXmlStreamReader(xmlStreamReader));
				} else if (Category.Schema.CATEGORY.equals(xmlStreamReader.getLocalName())) {
					cn.getCategories().add(CategoryFactory.fromXmlStreamReader(xmlStreamReader));
				} 
				break;
			}
		}
		
		return cn;
	}
	
}
