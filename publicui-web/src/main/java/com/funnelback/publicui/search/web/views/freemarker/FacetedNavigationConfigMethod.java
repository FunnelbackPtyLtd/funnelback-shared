package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

import freemarker.ext.beans.BeanModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.SimpleScalar;
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
        super(2, 0, false);
    }
    
    @Override
    public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        Object arg = arguments.get(0);
        
        Collection c = null;
        if (arg instanceof SimpleScalar) {
            String collectionId = ((TemplateScalarModel) arg).getAsString();
            c = configRepository.getCollection(collectionId);
        } else if (arg instanceof AdapterTemplateModel) {
            c = (Collection) ((BeanModel) arg).getWrappedObject();
        }
    
        String profileId = ((TemplateScalarModel) arguments.get(1)).getAsString();
        
        return FacetedNavigationUtils.selectConfiguration(c, profileId);
    }

}
