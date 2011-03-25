package com.funnelback.publicui.web.views.freemarker;

import java.util.List;

import lombok.extern.apachecommons.Log;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Freemarker function that sleeps for some milliseconds.
 * For testing purposes only.
 */
@Log
public class SleepMethod implements TemplateMethodModel, TemplateMethodModelEx {

	public static final String NAME = "sleep";
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 1) {
			throw new TemplateModelException("This method takes 1 argument: Sleep delay (in ms)");
		}
		
		long delay = ((SimpleNumber) arguments.get(0)).getAsNumber().longValue();
		
		try {
			log.debug("Sleeping for " + delay + " ms...");
			Thread.sleep(delay);
			log.debug("Slept for " + delay + " ms !");
		} catch (InterruptedException ie) {}
		
		return new SimpleScalar("");
	}

}
