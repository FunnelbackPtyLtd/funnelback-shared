package com.funnelback.publicui.web.views.freemarker;

import java.util.List;

import com.funnelback.common.config.Config;
import com.funnelback.publicui.i18n.I18n;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

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
		
		String value = ((SimpleScalar) arguments.get(0)).getAsString();
		return Config.isTrue(value);	
	}

}
