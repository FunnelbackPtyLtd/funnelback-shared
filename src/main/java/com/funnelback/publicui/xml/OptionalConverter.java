package com.funnelback.publicui.xml;
import java.util.Optional;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class OptionalConverter implements Converter{

    @Override
    public boolean canConvert(Class type) {
        return type.equals(Optional.class);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Optional<?> optional = (Optional<?>) source;
        
        if(optional.isPresent()) {
            context.convertAnother(optional.get());
        }
        
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        // I don't know how to convert back we probably need to know what class
        // is inside of the optional and then give the context and instance of that
        // class.
        return null;
    }

}
