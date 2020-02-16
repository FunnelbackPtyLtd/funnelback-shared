package com.funnelback.publicui.search.model.padre.factories;

import java.util.Date;
import java.util.Map;

import java.text.SimpleDateFormat;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;

import com.funnelback.publicui.search.model.padre.Details;
import com.funnelback.publicui.xml.XmlStreamUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DetailsFactory {
                                                                                    
    
    private static ThreadLocal<SimpleDateFormat> dateFormatters = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return Details.getUpdateDateFormat();
        }
    };
    
    public static Details fromMap(Map<String, String> data) {
        Date updated = new Date(0);
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
        return dateFormatters.get();
    }
}
