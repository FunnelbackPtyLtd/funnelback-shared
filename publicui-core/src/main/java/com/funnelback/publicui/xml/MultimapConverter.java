package com.funnelback.publicui.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Multimap;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MultimapConverter implements Converter {

    @Override
    public boolean canConvert(Class type) {
        System.out.println(type.getName()); 

        return Multimap.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        // Rather than manually write to the writer like the doco would suggest you do
        // simply make the object we want then tell XStream to serialise that instead.
        Map<Object, Object> map = new HashMap<>();
        Map<Object, Collection<Object>> inputMap = ((Multimap<Object, Object>) source).asMap();
        for(Map.Entry<Object, Collection<Object>> e : inputMap.entrySet()) {
            map.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        context.convertAnother(map);
    }
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {    
        return null;
    }
}
