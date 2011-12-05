package com.funnelback.publicui.search.web.views.freemarker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.apachecommons.CommonsLog;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * FreeMarker method to resolve a relative URL from a given base.
 * Given a base URL string, a relative URL string it will return
 * a new URL with the relative URL resolved from the given base.
 *
 * If an absolute URL is provided in the second parameter it will
 * be returned unchanged.
 * 
 * See http://docs.oracle.com/javase/6/docs/api/java/net/URL.html#URL(java.net.URL, java.lang.String)
 */
@CommonsLog
public class ResolveUrlMethod extends AbstractTemplateMethod {

	public ResolveUrlMethod() {
		super(2, 0, false);
	}

	public static final String NAME = "resolveUrl"; 
	
	@Override
	public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		String baseString = ((TemplateScalarModel) arguments.get(0)).getAsString();
		String toResolveString = ((TemplateScalarModel) arguments.get(1)).getAsString();
		String result;

		URL resolved;
		try {
			resolved = new URL(new URL(baseString), toResolveString);
			result = resolved.toString();
		} catch (MalformedURLException e) {
			log.warn("Malformed URL exception resolving " + toResolveString + " against " + baseString, e);
			result = toResolveString;
		}
		
		return new SimpleScalar(result);
	}
}
