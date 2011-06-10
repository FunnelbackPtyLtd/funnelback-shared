package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * FreeMarker method to change a query string parameter.
 * Given a query string, a parameter name and a value it will
 * either add the parameter if it doesn't exist, or replace its existing
 * value with the new one.
 */
public class ChangeQSParamMethod extends AbstractTemplateMethod {

	public ChangeQSParamMethod() {
		super(3, 0);
	}

	public static final String NAME = "changeParam"; 
	
	@Override
	public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		String qs = ((TemplateScalarModel) arguments.get(0)).getAsString();
		String paramName = ((TemplateScalarModel) arguments.get(1)).getAsString();
		String newValue = "";
		try {
			newValue = ((TemplateScalarModel) arguments.get(2)).getAsString();
		} catch (ClassCastException cce) {
			// Sometimes the new value is passed as a number, not a String.
			newValue = ((TemplateNumberModel) arguments.get(2)).toString();
		}
		
		
		Pattern p = Pattern.compile("([&;]|^)\\Q" + paramName + "\\E=[^&]*");
		Matcher m = p.matcher(qs);
		if (m.find()) {
			// Escape backreferences in value
			newValue = newValue.replace("$", "\\$");

			return new SimpleScalar(m.replaceAll("$1" + paramName + "=" + newValue));
		} else {
			return new SimpleScalar(qs + "&amp;" + paramName + "=" + newValue);
		}

	}
}
