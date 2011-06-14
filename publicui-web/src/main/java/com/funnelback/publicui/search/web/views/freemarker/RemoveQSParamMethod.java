package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

/**
 * Removes a set of parameters from a query string.
 */
public class RemoveQSParamMethod extends AbstractTemplateMethod {

	public static final String NAME = "removeParam"; 
	
	public RemoveQSParamMethod() {
		super(2, 0);
	}
	
	@Override
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
		}
		
		// If the transformed query string starts with a "&", strip it
		return qs.replaceAll("^&(amp;)?", "");

	}

}
