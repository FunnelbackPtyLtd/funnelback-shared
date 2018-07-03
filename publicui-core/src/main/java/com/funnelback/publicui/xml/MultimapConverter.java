package com.funnelback.publicui.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder.ListMultimapBuilder;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MultimapConverter implements Converter {

    @Override
    public boolean canConvert(Class type) {
        return Multimap.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        // Make a new map XStream understands and marshal that instead
        Map<Object, Object> map = new HashMap<>();
        Map<Object, Collection<Object>> inputMap = ((Multimap<Object, Object>) source).asMap();
        for(Map.Entry<Object, Collection<Object>> e : inputMap.entrySet()) {
            map.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        context.convertAnother(map);
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map<String, List<String>> map = (Map) context.convertAnother(reader, HashMap.class);
        ListMultimap<String, String> multimap = ListMultimapBuilder.hashKeys().arrayListValues().build();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            multimap.putAll(entry.getKey(), entry.getValue());            
        }
        return multimap;
    }
}
