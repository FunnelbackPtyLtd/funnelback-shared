package com.funnelback.publicui.search.model.padre.factories;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import lombok.extern.apachecommons.Log;

import com.funnelback.publicui.search.lifecycle.data.fetcher.padre.xml.XmlStreamUtils;
import com.funnelback.publicui.search.model.padre.Details;

@Log
public class DetailsFactory {

	private static final SimpleDateFormat UPDATED_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	
	public static Details fromMap(Map<String, String> data) {
		Date updated = new Date();
		try {
			updated = UPDATED_DATE_FORMAT.parse(data.get(Details.Schema.COLLECTION_UPDATED).trim());
		} catch (Exception e) {
			log.warn("Unable to parse " + Details.Schema.COLLECTION_UPDATED + " date '"
					+ data.get(Details.Schema.COLLECTION_UPDATED) + "'. Will use current date");
		}
		
		return new Details(data.get(Details.Schema.PADRE_VERSION),
				data.get(Details.Schema.COLLECTION_SIZE), updated);
	}
	
	public static Details fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws NumberFormatException, XMLStreamException {
		return fromMap(XmlStreamUtils.tagsToMap(Details.Schema.DETAILS, xmlStreamReader));
	}
}
