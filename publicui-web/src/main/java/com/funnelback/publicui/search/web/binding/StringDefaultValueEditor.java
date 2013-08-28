package com.funnelback.publicui.search.web.binding;

import java.beans.PropertyEditorSupport;

public class StringDefaultValueEditor extends PropertyEditorSupport {

    private final String defaultValue;
    
    public StringDefaultValueEditor(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || "".equals(text)) {
            super.setAsText(defaultValue);
        } else {
            super.setAsText(text);
        }
    }
    
}
