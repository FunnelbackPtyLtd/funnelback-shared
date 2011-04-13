package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.service.ConfigRepository;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Returns the current date
 */
public class UpdatedDateMethod implements TemplateMethodModel, TemplateMethodModelEx {

	public static final String NAME = "updatedDate";
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 1) {
			throw new TemplateModelException(I18n.i18n().tr("This method takes 1 argument: The ID of a collection."));
		}
		
		String collectionId = ((SimpleScalar) arguments.get(0)).getAsString();
		
		return configRepository.getLastUpdated(collectionId);
	}

}
