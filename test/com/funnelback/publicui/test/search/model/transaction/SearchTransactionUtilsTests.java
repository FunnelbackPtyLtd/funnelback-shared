package com.funnelback.publicui.test.search.model.transaction;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

public class SearchTransactionUtilsTests {

	@Test
	public void testHasCollection() {
		Assert.assertFalse(SearchTransactionUtils.hasCollection(null));
		Assert.assertFalse(SearchTransactionUtils.hasCollection(new SearchTransaction(null, null)));
		Assert.assertFalse(SearchTransactionUtils.hasCollection(new SearchTransaction(new SearchQuestion(), null)));
		
		SearchQuestion q = new SearchQuestion();
		q.setCollection(new Collection("test", null));
		
		Assert.assertTrue(SearchTransactionUtils.hasCollection(new SearchTransaction(q, null)));
	}
	
	@Test
	public void testHasQuery() {
		Assert.assertFalse(SearchTransactionUtils.hasQuery(null));
		Assert.assertFalse(SearchTransactionUtils.hasQuery(new SearchTransaction(null, null)));
		Assert.assertFalse(SearchTransactionUtils.hasQuery(new SearchTransaction(new SearchQuestion(), null)));
		
		SearchQuestion q = new SearchQuestion();
		q.setQuery("test");
		
		Assert.assertTrue(SearchTransactionUtils.hasQuery(new SearchTransaction(q, null)));
	}
	
	@Test
	public void testHasQueryAndCollection() {
		Assert.assertFalse(SearchTransactionUtils.hasQueryAndCollection(null));
		Assert.assertFalse(SearchTransactionUtils.hasQueryAndCollection(new SearchTransaction(null, null)));
		Assert.assertFalse(SearchTransactionUtils.hasQueryAndCollection(new SearchTransaction(new SearchQuestion(), null)));
		
		SearchQuestion q = new SearchQuestion();
		q.setQuery("test");
		
		Assert.assertFalse(SearchTransactionUtils.hasQueryAndCollection(new SearchTransaction(q, null)));
		
		q.setCollection(new Collection("test", null));
		Assert.assertTrue(SearchTransactionUtils.hasQueryAndCollection(new SearchTransaction(q, null)));
		
	}

	@Test
	public void testHasResponse() {
		Assert.assertFalse(SearchTransactionUtils.hasResponse(null));
		Assert.assertFalse(SearchTransactionUtils.hasResponse(new SearchTransaction(null, null)));
		Assert.assertTrue(SearchTransactionUtils.hasResponse(new SearchTransaction(null, new SearchResponse())));
	}
	
	@Test
	public void testHasResults() {
		Assert.assertFalse(SearchTransactionUtils.hasResults(null));
		Assert.assertFalse(SearchTransactionUtils.hasResults(new SearchTransaction(null, null)));
		Assert.assertFalse(SearchTransactionUtils.hasResults(new SearchTransaction(null, new SearchResponse())));
		
		SearchResponse sr = new SearchResponse();
		sr.setResultPacket(new ResultPacket());
		
		Assert.assertFalse(SearchTransactionUtils.hasResults(new SearchTransaction(null, sr)));
		
		sr.getResultPacket().getResults().add(new Result(0, 0, null, null, null, null, null, null, null, null, null, null, null, null, null));
		
		Assert.assertTrue(SearchTransactionUtils.hasResults(new SearchTransaction(null, sr)));
	}
	
	@Test
	public void testHasQuestion() {
		Assert.assertFalse(SearchTransactionUtils.hasQuestion(null));
		Assert.assertFalse(SearchTransactionUtils.hasQuestion(new SearchTransaction(null, null)));
		Assert.assertFalse(SearchTransactionUtils.hasQuestion(new SearchTransaction(null, new SearchResponse())));
		Assert.assertTrue(SearchTransactionUtils.hasQuestion(new SearchTransaction(new SearchQuestion(), null)));
		
	}
	
}
