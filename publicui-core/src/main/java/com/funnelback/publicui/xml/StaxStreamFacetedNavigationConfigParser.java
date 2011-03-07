package com.funnelback.publicui.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import lombok.extern.apachecommons.Log;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryType;
import com.funnelback.publicui.search.model.collection.facetednavigation.Facet;

/**
 * Parses a faceted navigation configuration using a Stax Stream parser.
 */
@Log
@Component
public class StaxStreamFacetedNavigationConfigParser implements FacetedNavigationConfigParser {

	/** Attribute of root tag containing query processor options */
	private static final String ATTR_QPOPTIONS = "qpoptions";
	
	/**
	 * Extra properties (In addition to "<Data>" for some
	 * category types
	 **/
	private static final String[] CATEGORY_EXTRA_PROPERTIES = { "Metafield", "UserSetGScope", "Query", "Gscopefield", "Label" };
	
	@Override
	public Facets parseFacetedNavigationConfiguration(String configuration) throws XmlParsingException {
		try {
			XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(configuration));
		
			Facets facets = new Facets();
			
			while(xmlStreamReader.hasNext() && xmlStreamReader.next() != XMLStreamReader.END_DOCUMENT) {
				
				switch(xmlStreamReader.getEventType()){
				case XMLStreamReader.START_ELEMENT:
					
					if (FacetedNavigationConfigParser.Facets.FACETS.equals(xmlStreamReader.getLocalName())) {
						// <Facets qpoptions="-rmcfabcd"> ...
						String qpOptions = xmlStreamReader.getAttributeValue(null, ATTR_QPOPTIONS);
						if (qpOptions != null) {
							facets.qpOptions = qpOptions.trim();
						}
					} else if (Facet.Schema.FACET.equals(xmlStreamReader.getLocalName())) {
						facets.facets.add(parseFacet(xmlStreamReader));
					}
					break;
				}
			}

			xmlStreamReader.close();
			return facets;
		} catch (XMLStreamException e) {
			throw new XmlParsingException(e);
		}
	}
	
	/**
	 * Parses a <Facet>
	 * @param reader
	 * @return
	 * @throws XMLStreamException
	 */
	private Facet parseFacet(XMLStreamReader reader) throws XMLStreamException {
		if (! Facet.Schema.FACET.equals(reader.getLocalName())) {
			throw new IllegalArgumentException();
		}
		
		String data = null;
		List<CategoryType> categories = new ArrayList<CategoryType>();
		while(reader.hasNext() && reader.nextTag() != XMLStreamReader.END_ELEMENT) {
			
			switch(reader.getEventType()){
			case XMLStreamReader.START_ELEMENT:
				
				if (Facet.Schema.DATA.equals(reader.getLocalName())) {
					data = reader.getElementText();
				} else {
					try {
						categories.add(parseCategory(data, reader));
					} catch (Exception e) {
						log.error("Unable to parse Category", e);
						throw new XMLStreamException(e);
					}
				}
				break;
			}
		}
		return new Facet(data, categories);
	}
	
	/**
	 * Recursively parses a Category. A Category can contains an infinite list of sub-categories, and so on.
	 * @param reader
	 * @return
	 * @throws XMLStreamException
	 * @throws BeanInstantiationException
	 * @throws ClassNotFoundException
	 */
	private CategoryType parseCategory(String facetName, XMLStreamReader reader) throws XMLStreamException, BeanInstantiationException, ClassNotFoundException {
		String name = reader.getLocalName();
		
		// The name = the class name of the corresponding Java classes.
		// Ex: <QueryItem> => com.funnelback.publicui.model.collection.facetednavigation.QueryItem
		// We instantiate the bean using reflection
		CategoryType c = (CategoryType) BeanUtils.instantiate(Class.forName(CategoryType.class.getPackage().getName() + "." + name));
		c.setFacetName(facetName);
		
		// Then we'll use a BeanWrapper to set properties on the bean later, without knowing
		// which is the concrete class behind it.
		// Different Category types have different attributes:
		// - QueryItem => query
		// - XPathFill => metafield
		// ...
		BeanWrapperImpl bw = new BeanWrapperImpl(c);
		
		while(reader.hasNext() && reader.nextTag() != XMLStreamReader.END_ELEMENT) {
			
			switch(reader.getEventType()){
			case XMLStreamReader.START_ELEMENT:
				if (Facet.Schema.DATA.equals(reader.getLocalName())) {
					c.setData(reader.getElementText());
				} else if ( ArrayUtils.contains(CATEGORY_EXTRA_PROPERTIES, reader.getLocalName()) ) {
					// This is a property value for our Category bean. Set the value using
					// the bean wrapper, avoiding multiple IF and casts.
					// The property has a name identical to the xml tag:
					//   QueryItem.query => <QueryItem> ... <Query>abc</Query> ... </QueryItem>
					bw.setPropertyValue(StringUtils.uncapitalize(reader.getLocalName()), reader.getElementText());
				} else {
					// If it's not a property, it's a sub category.
					c.getSubCategories().add(parseCategory(facetName, reader));
				}
				break;
			}
		}
		return c;
	}

}
