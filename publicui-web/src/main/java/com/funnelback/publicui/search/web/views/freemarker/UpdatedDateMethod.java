package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.service.IndexRepository;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Returns the current date
 */
public class UpdatedDateMethod extends AbstractTemplateMethod {

	public static final String NAME = "updatedDate";
	
	@Autowired
	private IndexRepository indexRepository;
	
	public UpdatedDateMethod() {
		super(1, 0, false);
	}
	
	@Override
	public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		String collectionId = ((TemplateScalarModel) arguments.get(0)).getAsString();
		return indexRepository.getLastUpdated(collectionId);
	}

}
