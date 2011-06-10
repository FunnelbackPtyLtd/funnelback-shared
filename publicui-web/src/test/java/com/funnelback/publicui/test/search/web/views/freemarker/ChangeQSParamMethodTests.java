package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.ChangeQSParamMethod;

import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class ChangeQSParamMethodTests extends AbstractMethodTest {

	@Test
	public void test() throws TemplateModelException {
		SimpleScalar result = (SimpleScalar) method.exec(buildStringArguments("key1=value1&key2=value2", "key1", "valueA"));
		Assert.assertEquals("key1=valueA&key2=value2", result.getAsString());
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
	protected AbstractTemplateMethod buildMethod() {
		return new ChangeQSParamMethod();
	}

	@Override
	protected int getRequiredArgumentsCount() {
		return 3;
	}

	@Override
	protected int getOptionalArgumentsCount() {
		return 0;
	}
	
}
