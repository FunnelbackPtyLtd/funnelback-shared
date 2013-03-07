package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.service.ConfigRepository;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Returns the list of form files for a given collection and profile.
 */
public class FormListMethod extends AbstractTemplateMethod {

    public static final String NAME = "formList";
    
    @Autowired
    private ConfigRepository configRepository;
    
    public FormListMethod() {
        super(2, 0, false);
    }
    
    @Override
    public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        String collectionId = ((TemplateScalarModel) arguments.get(0)).getAsString();
        String profileId = ((TemplateScalarModel) arguments.get(1)).getAsString();
        
        return configRepository.getForms(collectionId, profileId);
    }

}
