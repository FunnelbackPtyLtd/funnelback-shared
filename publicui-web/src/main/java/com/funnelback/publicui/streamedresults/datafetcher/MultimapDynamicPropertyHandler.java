package com.funnelback.publicui.streamedresults.datafetcher;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.DynamicPropertyHandler;

import com.google.common.collect.Multimap;

public class MultimapDynamicPropertyHandler implements DynamicPropertyHandler {

    public String[] getPropertyNames(Object object) {
        Multimap map = (Multimap) object;
        Set set = map.keySet();
        String[] names = new String[set.size()];
        Iterator it = set.iterator();
        for (int i = 0; i < names.length; i++) {
            names[i] = String.valueOf(it.next());
        }
        return names;
    }

    public Object getProperty(Object object, String propertyName) {
        return ((Multimap) object).get(propertyName);
    }

    public void setProperty(Object object, String propertyName, Object value) {
        ((Multimap) object).put(propertyName, value);
    }
}
