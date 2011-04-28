package com.funnelback.publicui.search.web.views.freemarker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.ConfigRepository;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateScalarModel;

/**
 * Performs an extra search.
 */
@Log
public class SearchMethod implements TemplateMethodModel, TemplateMethodModelEx {

	public final static String NAME = "search";
	@Autowired
	private ConfigRepository configRepository;
	
	@Autowired
	private SearchTransactionProcessor searchTransactionProcessor;
	
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() < 3 || arguments.size() > 4) {
			throw new TemplateModelException(I18n.i18n().tr("This method takes 3 arguments: "
					+ "The original SearchQuestion, "
					+ "the collection to search, "
					+ "and the query terms."
					+ "A forth argument can be set to use a specific map as input parameters"));
		}
		
		SearchQuestion q = (SearchQuestion) ((StringModel) arguments.get(0)).getWrappedObject();
		String collectionId = ((TemplateScalarModel) arguments.get(1)).getAsString();
		String query = ((TemplateScalarModel) arguments.get(2)).getAsString();
		

		Collection collection = configRepository.getCollection(collectionId);
		if (collection == null) {
			throw new TemplateModelException(I18n.i18n().tr("Invalid collection {0}", collectionId));
		}
		
		log.debug("Searching '" + query + "' on collection '" + collectionId);
		SearchQuestion sq = new SearchQuestion();
		sq.setCollection(collection);
		sq.setOriginalQuery(q.getOriginalQuery());
		sq.setQuery(q.getOriginalQuery());
		sq.setProfile(q.getProfile());

		if (arguments.size() == 4) {
			sq.getInputParameterMap().putAll(convertSimpleHashToParmeterMap((TemplateHashModelEx) arguments.get(3)));
		}

		return searchTransactionProcessor.process(sq);
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
