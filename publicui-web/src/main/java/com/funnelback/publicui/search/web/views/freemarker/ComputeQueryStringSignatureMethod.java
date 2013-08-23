package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import com.funnelback.publicui.utils.URLSignature;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * FreeMarker method to expose {@link URLSignature#computeQueryStringSignature(String)}
 */
public class ComputeQueryStringSignatureMethod extends AbstractTemplateMethod {

    /** Name of the method */
    public static final String NAME = "computeQueryStringSignature";
    
    /**
     * 
     */
    public ComputeQueryStringSignatureMethod() {
        super(1, 0, false);
    }
    
    @Override
    protected Object execMethod(List arguments) throws TemplateModelException {
        String str = ((TemplateScalarModel) arguments.get(0)).getAsString();
        return URLSignature.computeQueryStringSignature(str);
    }

}
