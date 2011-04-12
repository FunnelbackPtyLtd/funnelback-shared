package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Map;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Checks is quick links are enabled, and sets the related
 * query processor options, falling back to default values
 * if nothing is specified in the config files.
 * 
 * If the query_processor_option already contains quicklinks related
 * options their value takes precedence.
 */
@Log
@Component("quickLinksInputProcessor")
public class QuickLinks implements InputProcessor {

	/**
	 * Name of the query processor option for quick links depth
	 */
	private static final String QL_OPT_DEPTH = "-QL";
	
	/**
	 * Name of the query processor option for quick links rank
	 */
	private static final String QL_OPT_RANK = "-QL_rank";
	
	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		if (SearchTransactionUtils.hasCollection(searchTransaction)
				&& searchTransaction.getQuestion().getCollection().getQuickLinksConfiguration() != null
				&& searchTransaction.getQuestion().getCollection().getQuickLinksConfiguration().size() > 0) {
			
			if ( Config.isTrue(searchTransaction.getQuestion().getCollection().getQuickLinksConfiguration().get(Keys.QuickLinks.QUICKLINKS))) {
			
				Map<String, String> qlConfig = searchTransaction.getQuestion().getCollection().getQuickLinksConfiguration();
				String qpOptions = searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.QUERY_PROCESSOR_OPTIONS);
	
				if (qpOptions == null || ! qpOptions.matches(".*($|\\s)" + QL_OPT_DEPTH + "=\\d+.*")) {
					// No depth specified on the qp options
					String opt = QL_OPT_DEPTH + "=" + DefaultValues.QuickLinks.DEPTH_DEFAULT;
					if (qlConfig.containsKey(Keys.QuickLinks.DEPTH)) {
						opt = QL_OPT_DEPTH + "=" + qlConfig.get(Keys.QuickLinks.DEPTH);
					}
					searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(opt);
					log.debug("Added query processor option '" + opt + "'");
				}
				
				if (qpOptions == null || ! qpOptions.matches(".*($|\\s)" + QL_OPT_RANK + "=(\\d+|all).*")) {
					// No rank specified on the qp options
					String opt = QL_OPT_RANK + "=" + DefaultValues.QuickLinks.RANK_DEFAULT;
					if (qlConfig.containsKey(Keys.QuickLinks.RANK)) {
						opt = QL_OPT_RANK + "=" + qlConfig.get(Keys.QuickLinks.RANK);
					}
					searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(opt);
					log.debug("Added query processor option '" + opt + "'");
				}
				
			}
		}
	}
}
