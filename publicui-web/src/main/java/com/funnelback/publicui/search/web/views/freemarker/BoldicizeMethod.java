package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import com.funnelback.publicui.i18n.I18n;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Boldicize words in a given string
 */
public class BoldicizeMethod implements TemplateMethodModel, TemplateMethodModelEx {

	private static final Pattern OPERATORS_PATTERN = Pattern.compile("[^\\w\\s']");

	public static final String NAME = "boldify";	
	
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 2) {
			throw new TemplateModelException(I18n.i18n().tr("This method takes 2 arguments: The terms to boldicize, and the target string."));
		}
		
		String terms = ((SimpleScalar) arguments.get(0)).getAsString();
		String content = ((SimpleScalar) arguments.get(1)).getAsString();
		
		// First throw away any operators
		terms = OPERATORS_PATTERN.matcher(terms).replaceAll("");
		
		// Make the bold words unique
		HashSet<String> termSet = new HashSet<String>(Arrays.asList(terms.split("\\s")));
		// Don't allow 'b' as a word - it would highlight existing <b> tags
		termSet.remove("b");
		
		for (String word: termSet) {
			content = Pattern.compile("\\b(\\Q" + word + "\\E)\\b", Pattern.CASE_INSENSITIVE).matcher(content).replaceAll("<b>$1</b>");
		}
		
		return new SimpleScalar(content);
	}

}
