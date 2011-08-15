package com.funnelback.publicui.search.model.padre.factories;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import lombok.extern.apachecommons.Log;

import com.funnelback.publicui.search.model.padre.TierBar;
import com.funnelback.publicui.xml.XmlStreamUtils;

@Log
public class TierBarFactory {

	private static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(TierBar.DATE_PATTERN);
		};
	};
	
	public static TierBar fromMap(Map<String, String> data) {
		String dateString = data.get(TierBar.Schema.EVENT_DATE);
		
		Date date = null;
		if (dateString != null && !"".equals(dateString)) {
			try {
				date = dateFormat.get().parse(dateString);
			} catch (Exception e) {
				log.debug("Unparseable date: '" + dateString + "'");
			}
		}
		
		return new TierBar(
				Integer.parseInt(data.get(TierBar.Schema.MATCHED)),
				Integer.parseInt(data.get(TierBar.Schema.OUTOF)),
				date);
	}
	
	public static TierBar fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
		return fromMap(XmlStreamUtils.tagsToMap(TierBar.Schema.TIER_BAR, xmlStreamReader));
	}
}
