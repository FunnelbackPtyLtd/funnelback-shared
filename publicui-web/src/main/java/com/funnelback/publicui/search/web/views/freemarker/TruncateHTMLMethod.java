package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import com.funnelback.common.utils.TextUtils;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * Truncates a String to the right, or to the middle if the optional
 * stripMiddle argument is set.
 */
public class TruncateHTMLMethod extends AbstractTemplateMethod {

    public static final String NAME = "truncateHTML";
    
    public TruncateHTMLMethod() {
        super(2, 0, false);
    }
    
    @Override
    public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        String str = ((TemplateScalarModel) arguments.get(0)).getAsString();
        int length = ((TemplateNumberModel) arguments.get(1)).getAsNumber().intValue();
        
        return TextUtils.truncateHtml(str, "\u2026", length);        
    }

}
