package com.funnelback.publicui.search.web.binding;

import java.beans.PropertyEditorSupport;

import com.funnelback.common.config.ProfileId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProfileEditor extends PropertyEditorSupport {
    
    private final String defaultValue;
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || "".equals(text)) {
            setValue(defaultValue);
        } else {
            setValue(new ProfileId(text));
        }
    }

}
