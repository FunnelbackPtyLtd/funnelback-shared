package com.funnelback.publicui.search.lifecycle;

import groovy.lang.Binding;
import groovy.lang.Script;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.ArrayUtils;

import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Generic hook script runner that can be used as an {@link InputProcessor},
 * {@link OutputProcessor} or {@link DataFetcher}
 * 
 * @see Collection.Hook
 */
@CommonsLog
public class GenericHookScriptRunner implements DataFetcher, InputProcessor, OutputProcessor {

	public static enum Phase {
		Input, Data, Output;
	}
	
	/**
	 * Type of hook script to run
	 */
	private Hook hookScriptToRun;
	
	/**
	 * On which phases to run
	 */
	private Phase[] phases;
	
	/**
	 * @param hookScriptToRun Which hook script to run
	 * @param phases At which phase(s) to run
	 */
	public GenericHookScriptRunner(Hook hookScriptToRun, Phase... phases) {
		this.hookScriptToRun = hookScriptToRun;
		this.phases = phases;
	}
	
	public void fetchData(SearchTransaction searchTransaction) throws DataFetchException {
		if (ArrayUtils.contains(phases, Phase.Data)) {
			runHookScript(searchTransaction);
		}
	}
	
	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		if (ArrayUtils.contains(phases, Phase.Input)) {
			runHookScript(searchTransaction);
		}
	}

	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (ArrayUtils.contains(phases, Phase.Output)) {
			runHookScript(searchTransaction);
		}
	}
	
	/**
	 * Runs the hook script for the collection of the current transaction
	 */
	public void runHookScript(SearchTransaction searchTransaction) {
		if (SearchTransactionUtils.hasCollection(searchTransaction)
				&& searchTransaction.getQuestion().getCollection().getHookScriptsClasses().size() > 0) {
			
			Class<Script> hookScriptClass = searchTransaction.getQuestion().getCollection().getHookScriptsClasses().get(hookScriptToRun);
			if (hookScriptClass != null) {
				try {
					Script s = hookScriptClass.newInstance();
					Binding binding = new Binding();
					binding.setVariable(Hook.SEARCH_TRANSACTION_KEY, searchTransaction);
					s.setBinding(binding);
					s.run();
				} catch (Throwable t) {
					log.error("Error while running " + hookScriptToRun.toString() + " hook for collection '" + searchTransaction.getQuestion().getCollection().getId() + "'", t);
				}
			}
		}
	}


}
