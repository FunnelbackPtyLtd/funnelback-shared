package com.funnelback.publicui.search.lifecycle.output.processors;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.jmx.SearchMonitor;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

@Component("searchMonitorUpdaterOutputProcessor")
public class SearchMonitorUpdater extends AbstractOutputProcessor {

	@Autowired
	@Setter private SearchMonitor monitor;

	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		monitor.incrementQueryCount();

		if (searchTransaction != null && searchTransaction.hasResponse()
				&& searchTransaction.getResponse().hasResultPacket()
				&& searchTransaction.getResponse().getResultPacket().getPadreElapsedTime() != null) {
			monitor.addResponseTime(searchTransaction.getResponse().getResultPacket().getPadreElapsedTime());
		}
	}
}
