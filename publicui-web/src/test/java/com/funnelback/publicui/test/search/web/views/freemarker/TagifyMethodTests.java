package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.TagifyMethod;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;

public class TagifyMethodTests {

	@Test
	public void testMissingArguments() {
		try {
			new TagifyMethod().exec(null);
			Assert.fail();
		} catch (NullPointerException npe) {
		} catch (TemplateModelException tme) {
		}

		try {
			new TagifyMethod().exec(new ArrayList<SimpleScalar>());
			Assert.fail();
		} catch (TemplateModelException tme) {
		}
		
		try {
			ArrayList<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
			arguments.add(new SimpleScalar("missing"));
			
			new TagifyMethod().exec(arguments);
			Assert.fail();
		} catch (TemplateModelException tme) {
		}


	}
	
	@Test
	public void testNoMatchingWords() throws TemplateModelException {
		List<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
		arguments.add(new SimpleScalar("tag"));
		arguments.add(new SimpleScalar("word to boldicize"));
		arguments.add(new SimpleScalar("there is no matching words"));
		
		SimpleScalar result = (SimpleScalar) new TagifyMethod().exec(arguments);
		
		Assert.assertEquals("there is no matching words", result.getAsString());		
	}
	
	@Test
	public void testOperators() throws TemplateModelException {
		List<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
		arguments.add(new SimpleScalar("tag"));
		arguments.add(new SimpleScalar("|word [to]"));
		arguments.add(new SimpleScalar("there is a 'word' that we have to boldicize"));
		
		SimpleScalar result = (SimpleScalar) new TagifyMethod().exec(arguments);
		
		Assert.assertEquals("there is a '<tag>word</tag>' that we have <tag>to</tag> boldicize", result.getAsString());		
	}
	
	@Test
	public void test() throws TemplateModelException {
		List<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
		arguments.add(new SimpleScalar("tag"));
		arguments.add(new SimpleScalar("word to boldicize"));
		arguments.add(new SimpleScalar("this is the sentence to Boldicize"));
		
		SimpleScalar result = (SimpleScalar) new TagifyMethod().exec(arguments);
		
		Assert.assertEquals("this is the sentence <tag>to</tag> <tag>Boldicize</tag>", result.getAsString());
	}
	
}
