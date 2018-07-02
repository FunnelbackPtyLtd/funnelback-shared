package com.funnelback.publicui.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.funnelback.publicui.utils.MultimapToSingleStringMapWrapper;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MultimapToSingleStringMapConverter implements Converter {

    @Override
    public boolean canConvert(Class type) {
        return (MultimapToSingleStringMapWrapper.class.isAssignableFrom(type));
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        HashMap<String, String> toMarshal = new HashMap<>();
        // I wanted to just pass the underlying metadata map down, but sadly XStream then just
        // shows a reference to the other map, which is not what we want.
        for (Map.Entry<String, String> entry : ((MultimapToSingleStringMapWrapper) source).entrySet()) {
            toMarshal.put(entry.getKey(), entry.getValue());
        }
        
        context.convertAnother(toMarshal);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
        Map map = (Map) arg1.convertAnother(arg0, HashMap.class);
        return new MultimapToSingleStringMapWrapper(map, new HashMap<>(), new HashMap<>());
    }

}
