package com.funnelback.publicui.test.search.web.views.freemarker;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.RemoveQSParamMethod;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

public class RemoveQSParamMethodTests extends AbstractMethodTest {

	@Test
	public void testSingleArgument() throws TemplateModelException {
		String result = (String) method.exec(
				buildArguments(
						new SimpleScalar("key1=value1&key2=value2"),
						new SimpleScalar("key1")
						)
				);
		Assert.assertEquals("key2=value2", result);
	}

	@Test
	public void testMultipleArguments() throws TemplateModelException {
		String result = (String) method.exec(
				buildArguments(
						new SimpleScalar("key1=value1&key2=value2&key3=value3"),
						buildSequenceStringArguments("key1", "key3")
						)
				);
		Assert.assertEquals("key2=value2", result);
	}

	@Test
	public void testInexistentParam() throws TemplateModelException {
		String result = (String) method.exec(
				buildArguments(
						new SimpleScalar("key1=value1&key2=value2&key3=value3"),
						buildSequenceStringArguments("key5", "key6")
						)
				);
		Assert.assertEquals("key1=value1&key2=value2&key3=value3", result);
	}

	@Override
	protected TemplateMethodModel buildMethod() {
		return new RemoveQSParamMethod();
	}

	@Override
	protected int getRequiredArgumentsCount() {
		return 2;
	}

	@Override
	protected int getOptionalArgumentsCount() {
		return 0;
	}
	
}
