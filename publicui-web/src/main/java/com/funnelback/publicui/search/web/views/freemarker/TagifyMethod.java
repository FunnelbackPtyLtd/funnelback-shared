package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Tagifies words in a given string
 */
public class TagifyMethod extends AbstractTemplateMethod {

	private static final Pattern OPERATORS_PATTERN = Pattern.compile("[^\\w\\s']");

	public static final String NAME = "tagify";	
	
	public TagifyMethod() {
		super(3, 0);
	}
	
	@Override
	public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		String tag = ((TemplateScalarModel) arguments.get(0)).getAsString();
		String terms = ((TemplateScalarModel) arguments.get(1)).getAsString();
		String content = ((TemplateScalarModel) arguments.get(2)).getAsString();
		
		// First throw away any operators
		terms = OPERATORS_PATTERN.matcher(terms).replaceAll("");
		
		// Make the bold words unique
		HashSet<String> termSet = new HashSet<String>(Arrays.asList(terms.split("\\s")));
		// Don't allow 'b' as a word - it would highlight existing <b> tags
		termSet.remove(tag);
		
		for (String word: termSet) {
			content = Pattern.compile("\\b(\\Q" + word + "\\E)\\b", Pattern.CASE_INSENSITIVE).matcher(content).replaceAll("<"+tag+">$1</" +tag+">");
		}
		
		return new SimpleScalar(content);
	}

}
