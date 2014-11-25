package com.funnelback.publicui.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.service.ConfigRepository;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

/**
 * Returns a list based on the input, but filtered down to items where the given field is present and not-null.
 * 
 * The input list is expected to contain objects/hashes, not scalars or other lists.
 */
public class FilterListMethod extends AbstractTemplateMethod {

    public static final String NAME = "filterList";
    
    @Autowired
    private ConfigRepository configRepository;
    
    public FilterListMethod() {
        super(2, 0, false);
    }
    
    @Override
    public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        TemplateSequenceModel listToFilter = ((TemplateSequenceModel) arguments.get(0));
        String fieldName = ((TemplateScalarModel) arguments.get(1)).getAsString();
        
        List<TemplateHashModel> result = new ArrayList<TemplateHashModel>();
        
        for (int i = 0; i < listToFilter.size(); i++) {
            TemplateHashModel candidate = (TemplateHashModel) listToFilter.get(i);
            if (candidate.get(fieldName) != null) {
                result.add(candidate);
            }
        }
        
        return result;
    }

}
