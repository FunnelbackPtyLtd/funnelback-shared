package com.funnelback.publicui.test.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.BoldicizeMethod;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;

public class BoldicizeMethodTests {

	@Test
	public void testMissingArguments() {
		try {
			new BoldicizeMethod().exec(null);
			Assert.fail();
		} catch (NullPointerException npe) {
		} catch (TemplateModelException tme) {
		}

		try {
			new BoldicizeMethod().exec(new ArrayList<SimpleScalar>());
			Assert.fail();
		} catch (TemplateModelException tme) {
		}
		
		try {
			ArrayList<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
			arguments.add(new SimpleScalar("missing"));
			
			new BoldicizeMethod().exec(arguments);
			Assert.fail();
		} catch (TemplateModelException tme) {
		}


	}
	
	@Test
	public void testNoMatchingWords() throws TemplateModelException {
		List<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
		arguments.add(new SimpleScalar("word to boldicize"));
		arguments.add(new SimpleScalar("there is no matching words"));
		
		SimpleScalar result = (SimpleScalar) new BoldicizeMethod().exec(arguments);
		
		Assert.assertEquals("there is no matching words", result.getAsString());		
	}
	
	@Test
	public void testOperators() throws TemplateModelException {
		List<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
		arguments.add(new SimpleScalar("|word [to]"));
		arguments.add(new SimpleScalar("there is a 'word' that we have to boldicize"));
		
		SimpleScalar result = (SimpleScalar) new BoldicizeMethod().exec(arguments);
		
		Assert.assertEquals("there is a '<b>word</b>' that we have <b>to</b> boldicize", result.getAsString());		
	}
	
	@Test
	public void test() throws TemplateModelException {
		List<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
		arguments.add(new SimpleScalar("word to boldicize"));
		arguments.add(new SimpleScalar("this is the sentence to Boldicize"));
		
		SimpleScalar result = (SimpleScalar) new BoldicizeMethod().exec(arguments);
		
		Assert.assertEquals("this is the sentence <b>to</b> <b>Boldicize</b>", result.getAsString());
	}
	
}
