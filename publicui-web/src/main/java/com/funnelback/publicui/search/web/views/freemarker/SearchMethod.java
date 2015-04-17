package com.funnelback.publicui.search.web.views.freemarker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.ConfigRepository;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateScalarModel;

/**
 * Performs an extra search.
 */
@Log4j2
public class SearchMethod extends AbstractTemplateMethod {

    public final static String NAME = "search";
    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private SearchTransactionProcessor searchTransactionProcessor;
    
    public SearchMethod() {
        super(3, 1, false);
    }
    
    @Override
    public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        SearchQuestion q = (SearchQuestion) ((StringModel) arguments.get(0)).getWrappedObject();
        String collectionId = ((TemplateScalarModel) arguments.get(1)).getAsString();
        String query = ((TemplateScalarModel) arguments.get(2)).getAsString();
        

        Collection collection = configRepository.getCollection(collectionId);
        if (collection == null) {
            throw new TemplateModelException(i18n.tr("collection.invalid", collectionId));
        }
        
        log.debug("Searching '" + query + "' on collection '" + collectionId);
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collection);
        sq.setOriginalQuery(query);
        sq.setQuery(query);
        sq.setProfile(q.getProfile());

        if (arguments.size() == 4) {
            sq.getRawInputParameters().putAll(convertSimpleHashToParmeterMap((TemplateHashModelEx) arguments.get(3)));
        }

        return searchTransactionProcessor.process(sq, null);
    }
    
    
    private Map<String, String[]> convertSimpleHashToParmeterMap(TemplateHashModelEx hash) throws TemplateModelException {
        Map<String, String[]> out = new HashMap<String, String[]>();
        
        for (TemplateModelIterator it = hash.keys().iterator(); it.hasNext();) {
            String key = ((SimpleScalar) it.next()).getAsString();
            String value = hash.get(key).toString();
            out.put(key, new String[] {value});
        }
        
        return out;
    }
    
    

}
