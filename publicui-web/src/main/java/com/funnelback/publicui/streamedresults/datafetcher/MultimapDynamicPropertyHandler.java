package com.funnelback.publicui.streamedresults.datafetcher;

import org.apache.commons.jxpath.DynamicPropertyHandler;

import com.google.common.collect.Multimap;

public class MultimapDynamicPropertyHandler implements DynamicPropertyHandler {

    @Override
    public String[] getPropertyNames(Object object) {
        Multimap<String, ?> map = (Multimap) object;
        return map.keySet().toArray(new String[]{});
    }

    @Override
    public Object getProperty(Object object, String propertyName) {
        return ((Multimap) object).get(propertyName);
    }

    @Override
    public void setProperty(Object object, String propertyName, Object value) {
        ((Multimap) object).put(propertyName, value);
    }
}
