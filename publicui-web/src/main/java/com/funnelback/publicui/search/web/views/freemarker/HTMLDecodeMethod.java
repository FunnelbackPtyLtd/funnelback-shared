package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import org.springframework.web.util.HtmlUtils;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Decodes HTML.
 */
public class HTMLDecodeMethod extends AbstractTemplateMethod {

	public static final String NAME = "htmlDecode";
	
	public HTMLDecodeMethod() {
		super(1, 0);
	}
	
	@Override
	public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		String str = ((TemplateScalarModel) arguments.get(0)).getAsString();
		return HtmlUtils.htmlUnescape(str);
	}

}
