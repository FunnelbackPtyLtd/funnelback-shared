package com.funnelback.publicui.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * Truncates an URL in a smart way: Break only on directory separators
 * and use 2 lines max.
 */
public class TruncateURLMethod extends AbstractTemplateMethod {

	private TruncateMethod truncateMethod = new TruncateMethod();
	
	public static final String NAME = "truncateURL";
	
	private static final String URL_SEPARATOR = "/";
	private static final String PATH_SEPARATOR = "\\";
	
	private static final String SPLIT = "<br/>";
	
	public TruncateURLMethod() {
		super(2, 0);
	}
	
	@Override
	public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		String str = ((TemplateScalarModel) arguments.get(0)).getAsString();
		int length = ((TemplateNumberModel) arguments.get(1)).getAsNumber().intValue();

		if (str.length() <= length) {
			return arguments.get(0);
		}

		String separator = URL_SEPARATOR;
		if (str.contains(PATH_SEPARATOR)) {
			// Windows style path
			separator = PATH_SEPARATOR;
		}
		
		int breakPoint = str.lastIndexOf(separator, length);
		if (str.length() > length*2) {
			if (breakPoint < 0) {
				// no usable breakpoint and too long, simple concat in the middle to fit on a single line
				return fallBackTrunate(arguments);
			} else {
				String left = str.substring(0, breakPoint);
				String right = str.substring(breakPoint);
				
				boolean wasReplaced = false;
				// Remove intermediary directories: /f1/f2/f3/file.ext becomes /file.ext
				while ( right.matches(".*"+separator+"[^"+separator+"]+"+separator+"[^"+separator+"]+$") && right.length() > length) {
					right = right.replaceAll(".*"+separator+"[^"+separator+"]+"+separator+"([^"+separator+"]+)$", separator+"$1");
					wasReplaced=true;
				}
				
				// We have replaced directories, embed an ellipsis: /file.ext becomes /.../file.ext
				if (wasReplaced) {
					right = right.replaceAll(separator+"([^"+separator+"]+)$", separator+"\u2026"+separator+"$1");
				}
				
				if ((left.length() + right.length()) <= length*2) {
					return new SimpleScalar(left + SPLIT + right);
				} else {
					return fallBackTrunate(arguments);
				}				
			}
		} else {
			// Should fit on 2 lines
			if (breakPoint < 0 || str.substring(breakPoint).length() > length) {
				// Could not break it nicely, or the second part will be too long
				// Fall back to simple truncate
				return fallBackTrunate(arguments);
			} else {
				return new SimpleScalar(str.substring(0, breakPoint) + SPLIT + str.substring(breakPoint));
			}
		}
		
	}

	/**
	 * Falls back to the default truncate method
	 * @param arguments
	 * @return
	 * @throws TemplateModelException
	 */
	private Object fallBackTrunate(List arguments) throws TemplateModelException {
		List<TemplateModel> truncateArgs = new ArrayList<TemplateModel>(arguments);
		truncateArgs.add(TemplateBooleanModel.TRUE);
		return truncateMethod.exec(truncateArgs);
	}
	
}
