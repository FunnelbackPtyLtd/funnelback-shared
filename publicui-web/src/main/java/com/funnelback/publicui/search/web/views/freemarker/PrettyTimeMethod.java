package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.context.i18n.LocaleContextHolder;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModelException;

/**
 * Exposes {@link PrettyTime} features to FreeMarker.
 */
public class PrettyTimeMethod extends AbstractTemplateMethod {

    /** Name of the method */
    public static final String NAME = "prettyTime";
    
    /**
     * 
     */
    public PrettyTimeMethod() {
        super(1, 1, false);
    }
    
    @Override
    protected Object execMethod(List arguments) throws TemplateModelException {
        Date d = ((TemplateDateModel) arguments.get(0)).getAsDate();
        
        Locale locale = LocaleContextHolder.getLocale();
        if (arguments.size() > 1) {
            locale = (Locale) ((BeanModel) arguments.get(1)).getWrappedObject();
        }
        
        return new PrettyTime(locale).format(d);
    }

}
