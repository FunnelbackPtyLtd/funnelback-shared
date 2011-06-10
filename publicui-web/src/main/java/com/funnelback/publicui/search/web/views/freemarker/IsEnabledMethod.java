package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import com.funnelback.common.config.Config;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * FreeMarker method to check if a configuration parameter is enabled.
 * Compares the question parameter to various String meaning "enabled".
 */
public class IsEnabledMethod extends AbstractTemplateMethod {

	public static final String NAME = "is_enabled";
	
	public IsEnabledMethod() {
		super(1, 0);
	}
	
	@Override
	public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		TemplateScalarModel arg = (TemplateScalarModel) arguments.get(0);
		if (arg == null) {
			return false;
		} else {
			return Config.isTrue(arg.getAsString());
		}
	}

}
