package com.funnelback.publicui.search.lifecycle.input.processors;

import lombok.Getter;
import lombok.extern.apachecommons.Log;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Maps de facto global engine operators such as
 * link:, filetype: ... to the corresponding default metadata class.
 */
@Component("metadataAliasesInputProcessor")
@Log
public class MetadataAliases implements InputProcessor {

	private static final String SEPARATOR = ":";
	private enum Operators {
		link("h"), site("u"), filetype("f"), allinurl("v");

		@Getter private String md;
		
		private Operators(String md) {
			this.md = md;
		}
	}
	
	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		if (SearchTransactionUtils.hasQuery(searchTransaction)) {
			boolean updateQuery = false;
			String[] terms = searchTransaction.getQuestion().getQuery().split("\\s");
			for(int i=0;i<terms.length; i++) {
				for (Operators op: Operators.values()) {
					if (terms[i].startsWith(op + SEPARATOR)) {
						updateQuery = true;
						terms[i] = op.getMd() + terms[i].substring(op.toString().length());
					}
				}
			}
			
			if (updateQuery) {
				log.debug("Update query from '" + searchTransaction.getQuestion().getQuery() + "' to '" + StringUtils.join(terms, " ") + "'");
				searchTransaction.getQuestion().setQuery(StringUtils.join(terms, " "));
			}
		}
	}

}
