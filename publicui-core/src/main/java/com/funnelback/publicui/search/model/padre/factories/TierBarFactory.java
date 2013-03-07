package com.funnelback.publicui.search.model.padre.factories;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.math.NumberUtils;

import lombok.extern.log4j.Log4j;

import com.funnelback.publicui.search.model.padre.TierBar;
import com.funnelback.publicui.xml.XmlStreamUtils;

@Log4j
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
                NumberUtils.toInt(data.get(TierBar.Schema.MATCHED), 0),
                NumberUtils.toInt(data.get(TierBar.Schema.OUTOF), 0),
                date);
    }
    
    public static TierBar fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        return fromMap(XmlStreamUtils.tagsToMap(TierBar.Schema.TIER_BAR, xmlStreamReader));
    }
}
