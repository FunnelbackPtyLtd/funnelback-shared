package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Date;
import java.util.List;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Returns the current date
 */
public class CurrentDateMethod implements TemplateMethodModel, TemplateMethodModelEx {

	public static final String NAME = "currentDate";
	
	@Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		return new Date();
	}

}
