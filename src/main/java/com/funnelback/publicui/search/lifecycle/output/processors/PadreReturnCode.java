package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Date;

import lombok.Setter;
import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.log.PublicUIWarningLog;
import com.funnelback.publicui.search.model.padre.ReturnCodes;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.log.LogService;

/**
 * Does stuff depending of the PADRE return code.
 * Ex: Log into 'public-ui.warnings' if return code is 199
 * (check FUN-1990)
 *
 */
@Component("padreReturnCodeOutputProcessor")
@Log
public class PadreReturnCode implements OutputProcessor {

	@Autowired
	@Setter private LogService logService;
	
	@Override
	public void process(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasResponse(searchTransaction)
				&& searchTransaction.getResponse().getReturnCode() != ReturnCodes.SUCCESS
				&& SearchTransactionUtils.hasCollection(searchTransaction)) {
			
			log.trace("Processing return code '" + searchTransaction.getResponse().getReturnCode() + "'");
			switch (searchTransaction.getResponse().getReturnCode()) {
			case ReturnCodes.QUERIES_LOG_ERROR:
				logService.logPublicUIWarning(
						new PublicUIWarningLog(new Date(),
								searchTransaction.getQuestion().getCollection(),
								searchTransaction.getQuestion().getCollection().getProfiles().get(searchTransaction.getQuestion().getProfile()),
								searchTransaction.getQuestion().getUserId(),
								"Could not log query to collection's query log"));
				break;
			}
		}

	}

}
