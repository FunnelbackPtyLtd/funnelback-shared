package com.funnelback.publicui.test.search.web.views.freemarker;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.FacetScopeRemoveMethod;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;

public class FacetScopeRemoveMethodTest extends AbstractMethodTest {


	@Test
	public void testWithoutFacetScope() throws TemplateModelException {
		String result = (String) method.exec(
				buildArguments(
						new SimpleScalar("key1=value1&key2=value2"),
						new SimpleScalar("f.Facet|X")
						)
				);
		Assert.assertEquals("key1=value1&key2=value2", result);	
	}
	
	@Test
	public void testWrongFacet() throws TemplateModelException {
		String result = (String) method.exec(
				buildArguments(
						new SimpleScalar("key1=value1"
								+ "&facetScope=f.Industry%257CZ%3Ddivtrades%2520%2526%2520servicesdiv%26f.Industry%257CY%3Dcourier"
								+ "&key2=value2"),
						new SimpleScalar("f.Facet|X")
						)
				);
		Assert.assertEquals("key1=value1"
				+ "&facetScope=f.Industry%257CZ%3Ddivtrades%2520%2526%2520servicesdiv%26f.Industry%257CY%3Dcourier"
				+ "&key2=value2", result);	
	}
	
	@Test
	public void testCorrectFactet() throws TemplateModelException {
		String result = (String) method.exec(
				buildArguments(
						new SimpleScalar("key1=value1"
								+ "&facetScope=f.Industry%257CZ%3Ddivtrades%2520%2526%2520servicesdiv%26f.Industry%257CY%3Dcourier"
								+ "&key2=value2"),
						buildSequenceArguments("f.Industry|Z", "f.Industry|Y")
						)
				);
		Assert.assertEquals("key1=value1&facetScope=&key2=value2", result);			
	}
	
	@Test
	public void testFirstAndLastParameter() throws TemplateModelException {
		String result = (String) method.exec(
				buildArguments(
						new SimpleScalar("facetScope=f.Industry%257CZ%3Ddivtrades%2520%2526%2520servicesdiv%26f.Industry%257CY%3Dcourier"
								+ "&key1=value1&key2=value2"),
						buildSequenceArguments("f.Industry|Z", "f.Industry|Y")
						)
				);
		Assert.assertEquals("facetScope=&key1=value1&key2=value2", result);
		
		result = (String) method.exec(
				buildArguments(
						new SimpleScalar("key1=value1&key2=value2"
								+ "&facetScope=f.Industry%257CZ%3Ddivtrades%2520%2526%2520servicesdiv%26f.Industry%257CY%3Dcourier"),
						buildSequenceArguments("f.Industry|Z", "f.Industry|Y")
						)
				);
		Assert.assertEquals("key1=value1&key2=value2&facetScope=", result);			
	}

	
	@Test
	public void testMultipleFactet() throws TemplateModelException {
		String result = (String) method.exec(
				buildArguments(
						new SimpleScalar("key1=value1"
								+ "&facetScope=f.Industry%257CZ%3Ddivtrades%2520%2526%2520servicesdiv%26f.Industry%257CY%3Dcourier%26f.State%257CX%3Dqld"
								+ "&key2=value2"),
						buildSequenceArguments("f.Industry|Z", "f.Industry|Y")
						)
				);
		Assert.assertEquals("key1=value1&facetScope=%26f.State%257CX%3Dqld&key2=value2", result);			
	}

	
	@Override
	protected AbstractTemplateMethod buildMethod() {
		return new FacetScopeRemoveMethod();
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
