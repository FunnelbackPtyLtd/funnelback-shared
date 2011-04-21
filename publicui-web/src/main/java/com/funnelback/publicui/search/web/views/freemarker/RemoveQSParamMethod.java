package com.funnelback.publicui.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.funnelback.publicui.i18n.I18n;

import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Removes a set of parameters from a query string.
 */
public class RemoveQSParamMethod implements TemplateMethodModel, TemplateMethodModelEx {

	public static final String NAME = "removeParam"; 
	
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 2) {
			throw new TemplateModelException(I18n.i18n().tr("This function takes 2 arguments: The query string and the parameter name(s)"));
		}
		
		String qs = ((TemplateScalarModel) arguments.get(0)).getAsString();
		List<String> paramNames;
		try {
			// Try with a list
			paramNames = ((SimpleSequence) arguments.get(1)).toList();
		} catch (ClassCastException cce) {
			// Fall back to a single string
			paramNames = new ArrayList<String>();
			paramNames.add( ((SimpleScalar) arguments.get(1)).getAsString());
		}
		
		for (String paramName: paramNames) {
			Pattern p = Pattern.compile("([&;]|^)\\Q" + paramName + "\\E=[^&]*");
			Matcher m = p.matcher(qs);
			qs = m.replaceAll("");			
		}
		
		// If the tansformed query string starts with a "&", strip it
		return qs.replaceAll("^&(amp;)?", "");

	}

}
