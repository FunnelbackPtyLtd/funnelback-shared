package com.funnelback.publicui.test.search.web.views.freemarker;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.ResolveUrlMethod;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;

public class ResolveUrlMethodTests extends AbstractMethodTest {

	@Test
	public void testRelative() throws TemplateModelException {
		SimpleScalar result = (SimpleScalar) method.exec(buildStringArguments("http://www.funnelback.com/products/funnelback", "/favicon.ico"));
		Assert.assertEquals("http://www.funnelback.com/favicon.ico", result.getAsString());
	}

	@Test
	public void testRelativeNotRoot() throws TemplateModelException {
		SimpleScalar result = (SimpleScalar) method.exec(buildStringArguments("http://www.funnelback.com/products/funnelback", "wcag"));
		Assert.assertEquals("http://www.funnelback.com/products/wcag", result.getAsString());
	}

	@Test
	public void testAbsolute() throws TemplateModelException {
		SimpleScalar result = (SimpleScalar) method.exec(buildStringArguments("http://www.funnelback.com/products/services", "http://www.funnelback.com.au"));
		Assert.assertEquals("http://www.funnelback.com.au", result.getAsString());
	}

	@Override
	protected AbstractTemplateMethod buildMethod() {
		return new ResolveUrlMethod();
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
