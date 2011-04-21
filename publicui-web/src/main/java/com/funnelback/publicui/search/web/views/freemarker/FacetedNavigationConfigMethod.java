package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Helper to find if there is a faceted navigation configuration
 * active for the given collection and the given profile.
 */
public class FacetedNavigationConfigMethod implements TemplateMethodModel, TemplateMethodModelEx {

	public static final String NAME = "facetedNavigationConfig";
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 2) {
			throw new TemplateModelException(I18n.i18n().tr("This function takes 2 arguments: The collection and the profile"));
		}
		
		String collectionId = ((TemplateScalarModel) arguments.get(0)).getAsString();
		String profileId = ((TemplateScalarModel) arguments.get(1)).getAsString();
		
		return FacetedNavigationUtils.selectConfiguration(configRepository.getCollection(collectionId), profileId);
	}

}
