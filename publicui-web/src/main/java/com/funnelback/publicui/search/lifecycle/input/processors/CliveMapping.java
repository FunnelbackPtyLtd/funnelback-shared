package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.ArrayList;

import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Maps human-friendly clive names to PADRE internal index
 * from meta.cfg 
 *
 */
@Component("cliveMappingInputProcessor")
@Log4j
public class CliveMapping implements InputProcessor {

	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		if (SearchTransactionUtils.hasQuestion(searchTransaction)
				&& SearchTransactionUtils.hasCollection(searchTransaction)
				&& searchTransaction.getQuestion().getClive() != null
				&& searchTransaction.getQuestion().getClive().length > 0) {
			
			ArrayList<String> clives = new ArrayList<String>();
			for (String clive: searchTransaction.getQuestion().getClive()) {
				for (int i=0; i<searchTransaction.getQuestion().getCollection().getMetaComponents().length; i++) {
					if (clive.equals(searchTransaction.getQuestion().getCollection().getMetaComponents()[i])) {
						clives.add(Integer.toString(i));
						break;
					}
				}
			}
	
			if (clives.size() > 0) {
				if (log.isDebugEnabled()) {
					log.debug("Transformed clive parameter from '"
							+ StringUtils.join(searchTransaction.getQuestion().getClive(), ",")
							+ "' to '" + StringUtils.join(clives, ",") + "'");
				}
	
				searchTransaction.getQuestion().getAdditionalParameters().put(RequestParameters.CLIVE, StringUtils.join(clives, ","));
			}
		}

	}

}
