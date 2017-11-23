package com.funnelback.publicui.search.model.util.map;

import java.util.Optional;

import com.funnelback.publicui.search.model.util.map.AutoConvertingMap.Converter;

public class Converters {

    public static final Converter<String> INTEGER_TO_STRING = new Converter<String>(){

        @Override
        public Optional<String> convert(Object o) {
            if(o instanceof Integer) {
                return Optional.of(o.toString());
            }
            return Optional.empty();
        }

        @Override
        public Class<String> getKeyType() {
            return String.class;
        }
        
    };
    
    @SuppressWarnings("unchecked")
    public static <T> Converter<T> doNothingConverter() {
        return (Converter<T>) DO_NOTHING;
    }
    public static final Converter<Object> DO_NOTHING = new Converter<Object>() {
        @Override
        public Optional<Object> convert(Object o) {
            return Optional.empty();
        }

        @Override
        public Class<Object> getKeyType() {
            return Object.class;
        }
    };
}
