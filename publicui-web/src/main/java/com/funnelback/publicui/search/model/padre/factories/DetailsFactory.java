package com.funnelback.publicui.search.model.padre.factories;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import lombok.extern.apachecommons.Log;

import org.apache.commons.lang.StringUtils;

import com.funnelback.publicui.search.model.padre.Details;
import com.funnelback.publicui.xml.XmlStreamUtils;

@Log
public class DetailsFactory {
																					
	private static final Map<Long, SimpleDateFormat> dateFormatters = new HashMap<Long, SimpleDateFormat>();
	
	public static Details fromMap(Map<String, String> data) {
		Date updated = new Date();
		String str = data.get(Details.Schema.COLLECTION_UPDATED);
		if (str != null && ! "".equals(str.trim())) {
			try {
				updated = getDateFormatter().parse(str.trim());
			} catch (Exception e) {
				log.debug("Unable to parse " + Details.Schema.COLLECTION_UPDATED + " date '"
						+ str + "'. Will use current date", e);
			}
		}
				
		return new Details(
				StringUtils.trim(data.get(Details.Schema.PADRE_VERSION)),
				StringUtils.trim(data.get(Details.Schema.COLLECTION_SIZE)),
				updated);
	}
	
	public static Details fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws NumberFormatException, XMLStreamException {
		return fromMap(XmlStreamUtils.tagsToMap(Details.Schema.DETAILS, xmlStreamReader));
	}
	
	private static SimpleDateFormat getDateFormatter() {
		SimpleDateFormat df = dateFormatters.get(Thread.currentThread().getId());
		if (df == null) {
			df = new SimpleDateFormat(Details.UPDATED_DATE_PATTERN);
			dateFormatters.put(Thread.currentThread().getId(), df);
		}
		return df;
	}
}
