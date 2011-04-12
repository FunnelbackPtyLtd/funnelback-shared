package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import org.springframework.web.util.HtmlUtils;

import com.funnelback.publicui.i18n.I18n;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class HTMLDecodeMethod implements TemplateMethodModel, TemplateMethodModelEx {

	public static final String NAME = "htmlDecode";
	
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 1) {
			throw new TemplateModelException(I18n.i18n().tr("This method takes 1 argument: The string to decode."));
		}
		
		String str = ((SimpleScalar) arguments.get(0)).getAsString();
		
		return HtmlUtils.htmlUnescape(str);
	}

}
