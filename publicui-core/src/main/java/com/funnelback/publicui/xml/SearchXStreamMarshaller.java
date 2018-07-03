package com.funnelback.publicui.xml;


import java.util.TimeZone;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;

import com.funnelback.publicui.search.model.padre.Details;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchError;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;

/**
 * Custom XStream {@link Marshaller} to deal with specific fields.
 */
public class SearchXStreamMarshaller extends XStreamMarshaller {

    @Override
    protected void customizeXStream(XStream xstream) {
        xstream.omitField(SearchQuestion.class, "requestHeaders");
        xstream.registerLocalConverter(Result.class, "date", new DateConverter(Result.DATE_PATTERN_OUT, new String[] {Result.DATE_PATTERN_OUT}, TimeZone.getDefault()));
        xstream.registerLocalConverter(Details.class, "collectionUpdated", new DateConverter(Details.UPDATED_DATE_PATTERN, new String[] {Details.UPDATED_DATE_PATTERN}, TimeZone.getDefault()));
        xstream.registerLocalConverter(SearchError.class, "additionalData", new ExceptionConverter());
        xstream.registerConverter(new OptionalConverter());
    }
    

}
