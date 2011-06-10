package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Helper to find if there is a faceted navigation configuration
 * active for the given collection and the given profile.
 */
public class FacetedNavigationConfigMethod extends AbstractTemplateMethod {

	public static final String NAME = "facetedNavigationConfig";
	
	@Autowired
	private ConfigRepository configRepository;
	
	public FacetedNavigationConfigMethod() {
		super(2, 0);
	}
	
	@Override
	public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		String collectionId = ((TemplateScalarModel) arguments.get(0)).getAsString();
		String profileId = ((TemplateScalarModel) arguments.get(1)).getAsString();
		
		return FacetedNavigationUtils.selectConfiguration(configRepository.getCollection(collectionId), profileId);
	}

}
