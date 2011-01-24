package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XmlStreamUtils {

	/**
	 * Converts a list of tags with values to a Map. Doesn't process nested tags.
	 * @param rootTagName Expected tag name of the root tag, to check that the {@link XMLStreamReader} is in the right state.
	 * @param xmlStreamReader
	 * @return
	 * @throws XMLStreamException
	 */
	public static Map<String, String> tagsToMap(String rootTagName, XMLStreamReader xmlStreamReader) throws XMLStreamException {
		if( ! rootTagName.equals(xmlStreamReader.getLocalName()) ) {
			throw new IllegalArgumentException();
		}
		
		Map<String, String> data = new HashMap<String, String>();

		while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
			if (xmlStreamReader.isStartElement()) {
				// Start tag for an result entry
				String name = xmlStreamReader.getName().toString();
				String value = xmlStreamReader.getElementText();
				data.put(name, value);
			}		
		}
		
		return data;
	}
	
}
