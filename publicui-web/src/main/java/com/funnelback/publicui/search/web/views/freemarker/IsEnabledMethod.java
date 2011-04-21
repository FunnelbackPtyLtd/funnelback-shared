package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import com.funnelback.common.config.Config;
import com.funnelback.publicui.i18n.I18n;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * FreeMarker method to check if a configuration parameter is enabled.
 * Compares the input parameter to various String meaning "enabled".
 */
public class IsEnabledMethod implements TemplateMethodModel, TemplateMethodModelEx {

	public static final String NAME = "is_enabled";
	
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 1) {
			throw new TemplateModelException(I18n.i18n().tr("This method takes 1 argument: The value to check for."));
		}
		
		TemplateScalarModel arg = (TemplateScalarModel) arguments.get(0);
		if (arg == null) {
			return false;
		} else {
			return Config.isTrue(arg.getAsString());
		}
	}

}
