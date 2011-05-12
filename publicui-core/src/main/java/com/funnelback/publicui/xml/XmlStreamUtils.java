package com.funnelback.publicui.xml;

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
		if( ! rootTagName.equals(xmlStreamReader.getLocalName()) || ! xmlStreamReader.isStartElement()) {
			throw new IllegalArgumentException();
		}
		
		return tagsToMap(xmlStreamReader);
	} 
	
	private static Map<String, String> tagsToMap(XMLStreamReader xmlStreamReader) throws XMLStreamException {
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
	
	/**
	 * Given an element, returns a pair with the tag name, and the tag text.
	 * If the element contains nested tag, their text will be collected too (but the
	 * nested tag names will be lost)
	 * 
	 * @param xmlStreamReader an {@link XMLStreamReader} currently on a start element
	 * @return
	 * @throws XMLStreamException
	 */
	public static TagAndText getTagAndValue(XMLStreamReader xmlStreamReader) throws XMLStreamException {
		if (xmlStreamReader.getEventType() != XMLStreamReader.START_ELEMENT) {
			throw new IllegalArgumentException("Expecting to be on a START_ELEMENT");
		}
		
		TagAndText tv = new TagAndText();
		tv.tag = xmlStreamReader.getName().toString();
		
		StringBuffer text = new StringBuffer();
		do {
			if (xmlStreamReader.hasText()) {
				text.append(xmlStreamReader.getText());
			}
		} while (xmlStreamReader.next() != XMLStreamReader.END_ELEMENT || ! xmlStreamReader.getName().toString().equals(tv.tag));
		
		tv.text = text.toString();
		
		return tv;
	}
	
	public static class TagAndText {
		public String tag;
		public String text;
	}
	
}
