package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Date;
import java.util.List;

import freemarker.template.TemplateModelException;

/**
 * Returns the current date
 */
public class CurrentDateMethod extends AbstractTemplateMethod {

	public static final String NAME = "currentDate";
	
	public CurrentDateMethod() {
		super(0, 0);
	}
	
	@Override
	public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		return new Date();
	}

}
