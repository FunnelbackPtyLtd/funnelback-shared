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
}
