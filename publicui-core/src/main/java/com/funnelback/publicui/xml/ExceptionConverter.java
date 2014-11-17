package com.funnelback.publicui.xml;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * <p>Implementation of {@link Converter} that will convert
 * an {@link Exception} to a String representation of its
 * stacktrace.</p>
 * 
 * <p>Doesn't support unmarshalling.</p>
 *
 */
public class ExceptionConverter implements Converter {

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return Exception.class.equals(type.getGenericSuperclass());
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Exception e = (Exception) source;
        writer.setValue(ExceptionUtils.getStackTrace(e));
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        throw new UnsupportedOperationException();
    }
    
}