package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.RemoveQSParamMethod;

import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class RemoveQSParamMethodTests extends AbstractMethodTest {

	@Test
	public void test() throws TemplateModelException {
		new SimpleSequence().
		String result = method.exec(buildStringArguments("key1=value1&key2=value2", );
		Assert.assertEquals("key1=valueA&key2=value2", result);
	}
	
	@Test
	public void testInexistentParamShouldAddParam() throws TemplateModelException {
		SimpleScalar result = (SimpleScalar) method.exec(buildStringArguments("key1=value1&key2=value2", "keyA", "valueA"));
		Assert.assertEquals("key1=value1&key2=value2&amp;keyA=valueA", result.getAsString());
	}

	@Test
	public void testMultipleValues() throws TemplateModelException {
		SimpleScalar result = (SimpleScalar) method.exec(buildStringArguments("key1=value1&key1=value2", "key1", "valueA"));
		Assert.assertEquals("key1=valueA&key1=valueA", result.getAsString());
	}
	
	@Test
	public void testWithValueAsNumber() throws TemplateModelException {
		List<TemplateModel> arguments = new ArrayList<TemplateModel>();
		arguments.add(new SimpleScalar("key1=value1&key2=value2"));
		arguments.add(new SimpleScalar("key2"));
		arguments.add(new SimpleNumber(42));
		
		SimpleScalar result = (SimpleScalar) method.exec(arguments);
		Assert.assertEquals("key1=value1&key2=42", result.getAsString());
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
