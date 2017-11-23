package com.funnelback.publicui.xml;

import com.funnelback.publicui.search.model.util.map.AutoConvertingMap;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class AutoConvertingMapXStreamConverter implements Converter {
    
    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(Class type) {
        return type.equals(AutoConvertingMap.class);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        // Rather than manually write to the writer like the doco would suggest you do
        // simply make the object we want then tell XStream to serialise that instead.
        context.convertAnother(((AutoConvertingMap) source).getUnderlyingMap());
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
        return null;
    }


}
