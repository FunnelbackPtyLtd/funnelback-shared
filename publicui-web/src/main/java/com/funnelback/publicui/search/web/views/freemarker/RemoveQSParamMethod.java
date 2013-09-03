package com.funnelback.publicui.search.web.views.freemarker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.SneakyThrows;

import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

/**
 * <p>Removes a set of parameters from a query string.</p>
 * 
 * <p>Deals with various possible encodings of the parameters.</p>
 */
public class RemoveQSParamMethod extends AbstractTemplateMethod {

    public static final String NAME = "removeParam"; 
    
    public RemoveQSParamMethod() {
        super(2, 0, false);
    }
    
    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    public Object execMethod(@SuppressWarnings("rawtypes")List arguments) throws TemplateModelException {
        String qs = ((TemplateScalarModel) arguments.get(0)).getAsString();
        TemplateSequenceModel paramNames;
        try {
            // Try with a list
            paramNames = (TemplateSequenceModel) arguments.get(1);
        } catch (ClassCastException cce) {
            // Fall back to a single string
            paramNames = new SimpleSequence();
            ((SimpleSequence) paramNames).add( ((SimpleScalar) arguments.get(1)).getAsString());
        }
        
        for (int i=0; i<paramNames.size(); i++) {
            Pattern p = Pattern.compile("([&;]|^)\\Q" + paramNames.get(i) + "\\E=[^&]*");
            Matcher m = p.matcher(qs);
            qs = m.replaceAll("");            
            
            // Try with the same parameter, but with encoded spaces in both forms (+ / %20)
            p = Pattern.compile("([&;]|^)\\Q" + paramNames.get(i).toString().replace(" ", "\\E(%20|\\+)\\Q") + "\\E=[^&]*");
            m = p.matcher(qs);
            qs = m.replaceAll("");
            
            // And with encoded form, with spaces in both forms (+ / %20)
            p = Pattern.compile("([&;]|^)\\Q" + URLEncoder.encode(paramNames.get(i).toString(), "UTF-8").replaceAll("(\\+|%20)", "\\\\E(%20|\\\\+)\\\\Q") + "\\E=[^&]*");
            m = p.matcher(qs);
            qs = m.replaceAll("");

        }
        
        // If the transformed query string starts with a "&", strip it
        return qs.replaceAll("^&(amp;)?", "");

    }

}
