package com.funnelback.publicui.search.web.views.freemarker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.SneakyThrows;
import lombok.extern.apachecommons.Log;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

@Log
public class FacetScopeRemoveMethod extends AbstractTemplateMethod {

	public static final String NAME = "facetScopeRemove"; 
	
	private static final Pattern FACET_SCOPE_PATTERN = Pattern.compile("(^|[&\\?;])("+RequestParameters.FACET_SCOPE+"=)(.*?)(&|$)");
	
	public FacetScopeRemoveMethod() {
		super(2, 0);
	}

	@Override
	protected Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
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
		
		Matcher m = FACET_SCOPE_PATTERN.matcher(qs);
		if (m.find()) {
			String facetScope = m.group(3);
			
			for (int i=0; i<paramNames.size(); i++) {
				Matcher paramMatcher = buildRegexp(paramNames.get(i).toString()).matcher(facetScope);
				facetScope = paramMatcher.replaceAll("");
				log.debug("Removed '"+paramNames.get(i)+"' from '" + qs + "'");
			}
			
			qs = m.replaceAll("$1$2"+facetScope+"$4");
		}

		return qs;
	}
	
	@SneakyThrows(UnsupportedEncodingException.class)
	private Pattern buildRegexp(String paramName) {
		return Pattern.compile(
				"(^|" + URLEncoder.encode("&", "UTF-8") + "|" + URLEncoder.encode("?", "UTF-8") + "|" + URLEncoder.encode("?", "UTF-8") + ")"
				+ URLEncoder.encode( URLEncoder.encode(paramName, "UTF-8") + "=", "UTF-8")
				+ ".*?"
				+ "(" + URLEncoder.encode("&", "UTF-8") + "|$)"
				);
	}

}
