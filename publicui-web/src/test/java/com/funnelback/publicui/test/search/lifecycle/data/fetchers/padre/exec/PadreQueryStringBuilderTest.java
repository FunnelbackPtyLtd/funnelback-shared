package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre.exec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;

public class PadreQueryStringBuilderTest {

	SearchQuestion q;
	
	@Before
	public void before() {
		q = new SearchQuestion();
		q.setCollection(new Collection("dummy", null));
		q.setQuery("chocolate");
		q.getAdditionalParameters().put("a", new String[] {"1"});
	}
	
	@Test
	public void testNoUserEnteredQuery() {
		SearchQuestion qs = new SearchQuestion();
		qs.setCollection(new Collection("dummy", null));
		qs.setQuery(null);
		qs.getQueryExpressions().add("additional expr");
		
		Assert.assertEquals("additional expr", new PadreQueryStringBuilder(qs, true).buildQuery());
	}
	
	@Test
	public void test() {
		PadreQueryStringBuilder builder = new PadreQueryStringBuilder(q, false);
		Assert.assertEquals("a=1&collection=dummy&profile=_default&query=chocolate", builder.buildQueryString());
	}
	
	@Test
	public void testEncoding() {
		q.getAdditionalParameters().put("encoded", new String[] {"a nice & encoded + string"});

		Assert.assertEquals(
				"a=1&collection=dummy&encoded=a+nice+%26+encoded+%2B+string&profile=_default&query=chocolate",
				new PadreQueryStringBuilder(q, false).buildQueryString());
	}

	@Test
	public void testEnvironmentVariables() {
		q.getEnvironmentVariables().put("ABC", "DEF");
		q.getEnvironmentVariables().put("123", "456");
		q.getAdditionalParameters().put("123", new String[] {"456"});
		
		Assert.assertFalse(new PadreQueryStringBuilder(q, false).buildQueryString().contains("ABC"));
		Assert.assertFalse(new PadreQueryStringBuilder(q, false).buildQueryString().contains("123"));
	}
	
	@Test
	public void testQueryExpressions() {
		q.getQueryExpressions().add("is");
		q.getQueryExpressions().add("good");
		
		Assert.assertEquals(
				"a=1&collection=dummy&profile=_default&query=chocolate+is+good",
				new PadreQueryStringBuilder(q, false).buildQueryString());
	}
	
	@Test
	public void testMetaParameters() {
		q.getMetaParameters().add("really");
		q.getMetaParameters().add("rules");
		
		Assert.assertEquals(
				"a=1&collection=dummy&profile=_default&query=chocolate+really+rules",
				new PadreQueryStringBuilder(q, false).buildQueryString());
	}

	@Test
	public void testFacetQueryConstraints() {
		q.getFacetsQueryConstraints().add("or");
		q.getFacetsQueryConstraints().add("coffee");

		Assert.assertEquals(
				"a=1&collection=dummy&profile=_default&query=chocolate",
				new PadreQueryStringBuilder(q, false).buildQueryString());

		Assert.assertEquals(
				"a=1&collection=dummy&profile=_default&query=chocolate+or+coffee",
				new PadreQueryStringBuilder(q, true).buildQueryString());

	}
	
	@Test
	public void testFacetGScopeConstraints() {
		q.setFacetsGScopeConstraints("1,2+");
		
		Assert.assertEquals(
				"a=1&collection=dummy&profile=_default&query=chocolate",
				new PadreQueryStringBuilder(q, false).buildQueryString());

		Assert.assertEquals(
				"a=1&collection=dummy&gscope1=1%2C2%2B&profile=_default&query=chocolate",
				new PadreQueryStringBuilder(q, true).buildQueryString());
		
		q.getAdditionalParameters().put("gscope1", new String[] {"4,5+"});

		Assert.assertEquals(
				"a=1&collection=dummy&gscope1=1%2C2%2B4%2C5%2B%2B&profile=_default&query=chocolate",
				new PadreQueryStringBuilder(q, true).buildQueryString());

		q.getAdditionalParameters().put("gscope1", new String[] {"6"});

		Assert.assertEquals(
				"a=1&collection=dummy&gscope1=1%2C2%2B6%2B&profile=_default&query=chocolate",
				new PadreQueryStringBuilder(q, true).buildQueryString());
		
		q.setFacetsGScopeConstraints(null);

		Assert.assertEquals(
				"a=1&collection=dummy&gscope1=6&profile=_default&query=chocolate",
				new PadreQueryStringBuilder(q, true).buildQueryString());		
	}
	
	@Test
	public void testMultiValues() {
		q.setQuery("multi");
		q.getAdditionalParameters().put("scope", new String[] {"ab", "cd"});
		
		Assert.assertEquals(
				"a=1&collection=dummy&profile=_default&query=multi&scope=ab&scope=cd",
				new PadreQueryStringBuilder(q, true).buildQueryString());
	}
	
}
