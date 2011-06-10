package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;
import java.util.regex.Pattern;

import com.funnelback.publicui.i18n.I18n;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * Truncates a String to the right, or to the middle if the optional
 * stripMiddle argument is set.
 */
public class TruncateMethod extends AbstractTemplateMethod {

	public static final String NAME = "truncate";
	
	private final Pattern LAST_WORD_PATTERN = Pattern.compile("(\\S+)\\s+(\\S+)$"); 
	
	public TruncateMethod() {
		super(2, 1);
	}
	
	@Override
	public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		String str = ((TemplateScalarModel) arguments.get(0)).getAsString();
		int length = ((TemplateNumberModel) arguments.get(1)).getAsNumber().intValue();
		
		if (str.length() <= length) {
			return arguments.get(0);
		}
		
		boolean stripMiddle = false;
		if (arguments.size() == 3) {
			stripMiddle = ((TemplateBooleanModel) arguments.get(2)).getAsBoolean();
		}
		
		if (! stripMiddle) {
			String out = str.substring(0, length);
			return new SimpleScalar(LAST_WORD_PATTERN.matcher(out).replaceAll("$1") + "\u2026");
		} else {
			// How much do we strip from left-to-middle, and from right-to-middle ?
			// (Adding +1 for the ellipsis)
			int sideStrip = (str.length() - length+1) / 2;
			
			int middle = str.length() / 2;
			String left = str.substring(0, middle);
			String right = str.substring(middle);
			
			if (((str.length()-length+1) % 2) != 0) {
				left = left.substring(0, left.length() - (sideStrip+1));
			} else {
				left = left.substring(0, left.length() - sideStrip);
			}
			right = right.substring(sideStrip);
			
			return new SimpleScalar(left + "\u2026" + right);
		}
		
	}

}
