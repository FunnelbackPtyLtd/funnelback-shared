package com.funnelback.publicui.i18n;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Internationalisation utils
 */
@Component("i18n")
public class I18n {

    @Autowired
    @Setter private MessageSource messages;
    
    public String tr(String code) {
        return messages.getMessage(code, null, LocaleContextHolder.getLocale());        
    }
    
    public String tr(String code, Object... args) {
        return messages.getMessage(code, args, LocaleContextHolder.getLocale());
    }
    
}
