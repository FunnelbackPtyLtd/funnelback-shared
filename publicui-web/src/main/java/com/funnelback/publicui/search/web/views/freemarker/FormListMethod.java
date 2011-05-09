package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.service.ConfigRepository;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Returns the list of form files for a given collection and profile.
 */
public class FormListMethod implements TemplateMethodModel, TemplateMethodModelEx {

	public static final String NAME = "formList";
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		if (arguments.size() != 2) {
			throw new TemplateModelException(I18n.i18n().tr("This method takes 2 argument: "
					+ "The collection ID and the profile ID"));
		}
		
		String collectionId = ((TemplateScalarModel) arguments.get(0)).getAsString();
		String profileId = ((TemplateScalarModel) arguments.get(1)).getAsString();
		
		return configRepository.getForms(collectionId, profileId);
	}

}
