package com.funnelback.plugin.details.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class PluginConfigKeyType {
    @RequiredArgsConstructor
    public enum Format {
        ARRAY("array"),
        BOOLEAN("boolean"),
        DATE("date"),
        INTEGER("integer"),
        LONG("long"),
        METADATA("metadata"),
        PASSWORD("password"),
        STRING("string");

        private final String type;
    }

    private final Format type;
    private final Format subtype;

    public PluginConfigKeyType(Format type) {
        this(type, null);
    }

    public PluginConfigKeyType(Format type, Format subtype) {
        this.type = type;
        if (type == Format.ARRAY) {
            if (subtype == null) {
                throw new IllegalArgumentException("Type 'ARRAY' requires to provide subtype but found null");
            }
            this.subtype = subtype;
        } else {
            this.subtype = null;
        }
    }
}
