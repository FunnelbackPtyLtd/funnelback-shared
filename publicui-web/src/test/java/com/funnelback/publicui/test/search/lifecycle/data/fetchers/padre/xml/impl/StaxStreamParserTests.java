package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre.xml.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;
import com.funnelback.publicui.search.model.padre.BestBet;
import com.funnelback.publicui.search.model.padre.Category;
import com.funnelback.publicui.search.model.padre.Cluster;
import com.funnelback.publicui.search.model.padre.ClusterNav;
import com.funnelback.publicui.search.model.padre.ContextualNavigation;
import com.funnelback.publicui.search.model.padre.Explain;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.padre.TierBar;
import com.funnelback.publicui.xml.XmlParsingException;

public class StaxStreamParserTests {

	private ResultPacket rp;
	
	@Before
	public void before() throws XmlParsingException, IOException {
		StaxStreamParser parser = new StaxStreamParser();
		rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/complex.xml"), "UTF-8"));
		assertNotNull(rp);
	}
	
	@Test
	public void testDetails() {
		assertEquals("FUNNELBACK_PADRE_9.1.2.0 64MDPLFS-VEC3-DNAMS2 (Web/Enterprise)", rp.getDetails().getPadreVersion());
		assertEquals("/opt/funnelback/data/gov_combined/live/idx/index: 93.5 MB, 3404 docs", rp.getDetails().getCollectionSize());
		
		Calendar c = Calendar.getInstance();
		c.set(2011, 0, 27, 01, 40);
		c.set(Calendar.SECOND, 23);
		c.set(Calendar.MILLISECOND, 0);
		assertEquals(c.getTime(), rp.getDetails().getCollectionUpdated());
	}
	
	@Test
	public void testCoolerWeightings() {
		assertEquals(3,rp.getCoolerWeights().size());
		
		assertNotNull(rp.getCoolerWeights().get("content"));
		assertNotNull(rp.getCoolerWeights().get("offlink"));
		assertNotNull(rp.getCoolerWeights().get("urllen"));
		
		assertEquals(0.41,rp.getCoolerWeights().get("content"),0.0001);
		assertEquals(0.14,rp.getCoolerWeights().get("offlink"),0.0001);
		assertEquals(0.14,rp.getCoolerWeights().get("urllen"),0.0001);
	}
	
	@Test
	public void testExplain() {
		Explain e = rp.getResults().get(0).getExplain();
		assertEquals(0.639, e.getFinalScore(), 0.0001);
		assertEquals(2, e.getConsat());
		assertEquals(0.662,e.getLenratio(),0.0001);
		
		assertNotNull(e.getFeatureScores().get("content"));
		assertNotNull(e.getFeatureScores().get("offlink"));
		assertNotNull(e.getFeatureScores().get("urllen"));
		
		assertEquals(0.997,e.getFeatureScores().get("content"),0.0001);
		assertEquals(0,e.getFeatureScores().get("offlink"),0.0001);
		assertEquals(0.512,e.getFeatureScores().get("urllen"),0.0001);
		
		for (int i=1; i<rp.getResults().size(); i++) {
			assertNull(rp.getResults().get(i).getExplain());
		}
	}
	
	@Test
	public void testWinType() {
		Map<String,String> winTypes = rp.getExplainTypes();
		
		assertEquals("max_other",winTypes.get("content"));
		assertEquals("max_other",winTypes.get("onlink"));
		assertEquals("max_other",winTypes.get("offlink"));
		assertEquals("max_other",winTypes.get("urllen"));
		assertEquals("max_other",winTypes.get("qie"));
		assertEquals("max_other",winTypes.get("annie"));
		assertEquals("max_other",winTypes.get("domain_weight"));
		assertEquals("max_other",winTypes.get("geoprox"));
		assertEquals("max_other",winTypes.get("log_annie"));
		assertEquals("max_other",winTypes.get("annie_rank"));
		assertEquals("max_other",winTypes.get("BM25F"));
		assertEquals("max_other",winTypes.get("an_okapi"));
		assertEquals("max_other",winTypes.get("comp_wt"));
		assertEquals("max_other",winTypes.get("BM25F_rank"));
		assertEquals("max_other",winTypes.get("host_incoming_link_score"));
		assertEquals("max_other",winTypes.get("host_click_score"));
		assertEquals("max_other",winTypes.get("host_linking_hosts_score"));
		assertEquals("max_other",winTypes.get("host_linked_hosts_score"));
		assertEquals("max_other",winTypes.get("host_rank_in_crawl_order_score"));
		assertEquals("max_other",winTypes.get("host_domain_shallowness_score"));		
	
		assertEquals("max_possible",winTypes.get("recency"));
		assertEquals("max_possible",winTypes.get("urltype"));
		assertEquals("max_possible",winTypes.get("nonbin"));
		assertEquals("max_possible",winTypes.get("no_ads"));
		assertEquals("max_possible",winTypes.get("imp_phrase"));
		assertEquals("max_possible",winTypes.get("consistency"));
		assertEquals("max_possible",winTypes.get("mainhosts"));
		assertEquals("max_possible",winTypes.get("document_number"));
	}
	
	@Test
	public void testQueryAndCollection() {
		assertEquals("visa^0.345", rp.getQuery());
		assertEquals("visa", rp.getQueryAsProcessed());
		assertEquals("visa^0.345", rp.getQueryCleaned());
		assertEquals("gov_combined", rp.getCollection());
	}
	
	@Test
	public void testResultsSummary() {
		assertEquals(200, rp.getResultsSummary().getFullyMatching().intValue());
		assertEquals(200, rp.getResultsSummary().getEstimatedHits().intValue());
		assertEquals(0, rp.getResultsSummary().getPartiallyMatching().intValue());
		assertEquals(200, rp.getResultsSummary().getTotalMatching().intValue());
		assertEquals(10, rp.getResultsSummary().getNumRanks().intValue());
		assertEquals(1, rp.getResultsSummary().getCurrStart().intValue());
		assertEquals(10, rp.getResultsSummary().getCurrEnd().intValue());
		assertEquals(11, rp.getResultsSummary().getNextStart().intValue());
	}
	
	@Test
	public void testSpell() {
		assertNotNull(rp.getSpell());
		assertEquals("king", rp.getSpell().getText());
		assertEquals("query=king&collection=shakespeare-lear&profile=_default_preview", rp.getSpell().getUrl());
	}
	
	@Test
	public void testBestBets() {
		Assert.assertEquals(2, rp.getBestBets().size());
		
		BestBet bb = rp.getBestBets().get(0);
		Assert.assertEquals("([^\\w\\-]|^)visa", bb.getTrigger());
		Assert.assertEquals("http://www.immi.gov.au/e_visa/", bb.getLink());
		Assert.assertEquals("Online Applications, including eVisas, ETAs, RRVs", bb.getTitle());
		Assert.assertEquals("List of services provided on the Internet by the Australian Department of Immigration and Citizenship.", bb.getDescription());

		bb = rp.getBestBets().get(1);
		Assert.assertEquals("(([^\\w\\-]|^)visa)|(([^\\w\\-]|^)touris)", bb.getTrigger());
		Assert.assertEquals("http://www.immi.gov.au/visitors/", bb.getLink());
		Assert.assertEquals("Visitors - Visas and Immigration information", bb.getTitle());
		Assert.assertEquals("Official information about visiting Australia for tourism, working visits, business trips or medical visits.", bb.getDescription());
	}
	
	@Test
	public void testRMC() {
		Map<String, Integer> rmcs = rp.getRmcs();
		assertEquals(138, rmcs.size());
		
		// Pick some random keys/values
		assertEquals(139, rmcs.get("A:All").intValue());
		assertEquals(1, rmcs.get("A:Women").intValue());
		assertEquals(15, rmcs.get("C:Life Events").intValue());
		assertEquals(1, rmcs.get("J:VIC").intValue());
		assertEquals(3, rmcs.get("a:NSW Government agency").intValue());
		assertEquals(1, rmcs.get("a:Australian Federal Police").intValue());
		assertEquals(2, rmcs.get("s:tourists").intValue());
		assertEquals(1, rmcs.get("s:real-estate").intValue());
	}
	
	@Test
	public void testUrlCount() {
		Map<String, Integer> urls = rp.getUrlCounts();
		assertEquals(279, urls.size());
		
		// Pick some random keys/values
		assertEquals(1, urls.get("www.portugal.embassy.gov.au/lbon").intValue());
		assertEquals(1, urls.get("www.brisbane.qld.gov.au/traffic-transport").intValue());
		assertEquals(2, urls.get("australia.gov.au/directories/australia/ato").intValue());
		assertEquals(4, urls.get("australia.gov.au/faqs/government-services-faqs").intValue());
		assertEquals(8, urls.get("australia.gov.au/service").intValue());
		assertEquals(1, urls.get("www.tonga.embassy.gov.au").intValue());
		assertEquals(10, urls.get("australia.gov.au/services").intValue());
	}

	public void testGScopeCount() {
		Map<Integer, Integer> gscopes = rp.getGScopeCounts();
		assertEquals(2, gscopes.size());
		
		assertEquals(54, gscopes.get(1).intValue());
		assertEquals(6, gscopes.get(54).intValue());		
	}
	
	@Test
	public void testTierBars() {
		Assert.assertEquals(2, rp.getTierBars().size());
		
		TierBar tb = rp.getTierBars().get(0);
		Assert.assertEquals(0, tb.getFirstRank());
		Assert.assertEquals(8, tb.getLastRank());
		Assert.assertEquals(2, tb.getMatched());
		Assert.assertEquals(2, tb.getOutOf());

		tb = rp.getTierBars().get(1);
		Assert.assertEquals(8, tb.getFirstRank());
		Assert.assertEquals(10, tb.getLastRank());
		Assert.assertEquals(1, tb.getMatched());
		Assert.assertEquals(2, tb.getOutOf());
	}
	
	@Test
	public void testGetResultsMethods() {
		assertEquals(10, rp.getResults().size());
		assertEquals(12, rp.getResultsWithTierBars().size());
		
		for (Result r: rp.getResults()) {
			assertEquals(Result.class, r.getClass());
		}
		
		assertTrue(rp.getResultsWithTierBars().get(0) instanceof TierBar);
		for (int i=1; i<9; i++) {
			assertTrue(rp.getResultsWithTierBars().get(i) instanceof Result);
		}
		assertTrue(rp.getResultsWithTierBars().get(9) instanceof TierBar);
		assertTrue(rp.getResultsWithTierBars().get(10) instanceof Result);
		assertTrue(rp.getResultsWithTierBars().get(11) instanceof Result);

	}
	
	@Test
	public void testResults() {
		assertEquals(10, rp.getResults().size());
		assertTrue(rp.hasResults());
		
		Result first = rp.getResults().get(0);
		assertEquals(1, first.getRank().intValue());
		assertEquals(100, first.getScore().intValue());
		assertEquals("Online visa applications", first.getTitle());
		assertEquals("info-aus", first.getCollection());
		assertEquals(0, first.getComponent().intValue());
		assertEquals("http://www.immi.gov.au/e_visa/", first.getLiveUrl());
		assertEquals("http://www.immi.gov.au/e_visa/", first.getDisplayUrl());
		assertEquals("http://www.immi.gov.au/e_visa/", first.getIndexUrl());
		assertEquals("A Maritime Crew <b>visa</b> is required by foreign crew of non-military ships visiting Australia. "
				+ "... A Superyacht Crew <b>visa</b> is required by crew of Superyachts working in Australia.",
				first.getSummary());
		assertEquals("http://fed-cache.funnelback.com/search/cache.cgi?collection=info-aus&doc=http%2Fwww.immi.gov.au%2Fe_visa%2F_default.fun.html.pan.txt", first.getCacheUrl());
		assertEquals(null, first.getDate());
		assertEquals(27443, first.getFileSize().intValue());
		assertEquals("html", first.getFileType());
		assertEquals(1, first.getTier().intValue());
		assertEquals(2681, first.getDocNum().intValue());
		
		// Quick links
		assertNotNull(first.getQuickLinks());
		assertEquals("www.immi.gov.au/e_visa/", first.getQuickLinks().getDomain());
		assertEquals("Request a Visa", first.getQuickLinks().getQuickLinks().get(0).getText());
		assertEquals("www.immi.gov.au/e_visa/request.html", first.getQuickLinks().getQuickLinks().get(0).getUrl());
		assertEquals("Check your application status", first.getQuickLinks().getQuickLinks().get(1).getText());
		assertEquals("www.immi.gov.au/e_visa/application-status/", first.getQuickLinks().getQuickLinks().get(1).getUrl());
		
		Map<String, String> md = first.getMetaData();
		assertEquals(7, md.size());
		// Pick some metadata
		assertEquals("Department of Immigration and Citizenship", md.get("a"));
		assertEquals("2010-01-14T162745", md.get("M"));
		assertEquals("Topics;topics;Immigration;immigration;Visiting Australia;visiting-australia|Topics;topics;Immigration;immigration;Passports and Visas;passports-and-visas|People;people;Tourists;tourists;Visas;visas|Topics;topics;Tourism and Travel;tourism-and-travel;Passport and <b>Visa</b> Information;passport-and-<b>visa</b>-information|Politics;Politics;Visas;visas", md.get("R"));
		
		Calendar c = Calendar.getInstance();
		c.set(2011, 0, 24, 0, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		Result last = rp.getResults().get(9);
		assertEquals(10, last.getRank().intValue());
		assertEquals(67, last.getScore().intValue());
		assertEquals("Australian Embassy and Permanent Mission to the United Nations", last.getTitle());
		assertEquals("info-aus", last.getCollection());
		assertEquals(0, last.getComponent().intValue());
		assertEquals("http://www.austria.embassy.gov.au/vien/home.html", last.getLiveUrl());
		assertEquals("http://www.austria.embassy.gov.au/vien/home.html", last.getDisplayUrl());
		assertEquals("http://www.austria.embassy.gov.au/vien/home.html", last.getIndexUrl());
		assertEquals("Please wait to be connected to our operator. Please do not use the toll free number for <b>visa</b>"
				+ " and immigration enquiries. ... Visas, migration and citizenship. The Embassy’s <b>Visa</b> &Migration"
				+ " office processes <b>visa</b> applications from nationals and residents", last.getSummary());
		assertEquals("http://fed-cache.funnelback.com/search/cache.cgi?collection=info-aus&doc=http%2Fwww.austria.embassy.gov.au%2Fvien%2Fhome.html.pan.txt", last.getCacheUrl());
		assertEquals(c.getTime(), last.getDate());
		assertEquals(16917, last.getFileSize().intValue());
		assertEquals("html", last.getFileType());
		assertEquals(2, last.getTier().intValue());
		assertEquals(3068, last.getDocNum().intValue());

		md = last.getMetaData();
		assertEquals(6, md.size());
		// Pick some metadata
		assertEquals("Department of Foreign Affairs and Trade", md.get("a"));
		assertEquals("2009-11-09T162611", md.get("M"));
		assertEquals("Politics;Politics;Embassies;embassies", md.get("R"));
		
		assertNull(last.getQuickLinks());

	}
	
	@Test
	public void testMisc() {
		assertEquals(12, rp.getPadreElapsedTime().intValue());
		assertEquals("2x", rp.getQueryProcessorCodes());
		assertEquals(0.020, rp.getPhlusterElapsedTime().floatValue(), 0.001);

		assertEquals(2, rp.getIncludeScopes().size());
		assertEquals("include1", rp.getIncludeScopes().get(0));
		assertEquals("include2", rp.getIncludeScopes().get(1));
		
		assertEquals(2, rp.getExcludeScopes().size());
		assertEquals("excludeA", rp.getExcludeScopes().get(0));
		assertEquals("excludeB", rp.getExcludeScopes().get(1));
	}
	
	@Test
	public void testContextualNavigation() {
		ContextualNavigation cn = rp.getContextualNavigation();
		assertNotNull(cn);
		assertEquals("visa", cn.getSearchTerm());
		
		ClusterNav cnav = cn.getClusterNav();
		assertNotNull(cnav);
		assertEquals(0, cnav.getLevel().intValue());
		assertEquals("profile=info&coverage=info&extra_aus-gov-forms_add_queryterm=%7CC%3Aform&"
				+ "form=simple&collection=gov_combined&cool5=20&extra_aus-gov-faqs_add_queryterm=%7CC%3Afaq&"
				+ "xml=1&advancedSearch=false&extra_aus-gov-faqs_fmo=on&extra_aus-gov-services_num_ranks=5&"
				+ "extra_aus-gov-faqs_num_ranks=5&extra_aus-gov-forms_fmo=on&rmcf=AuJaCs&"
				+ "extra_aus-gov-services_add_queryterm=%7CC%3Aservice&display_featured_pages=false&daat=off&"
				+ "extra_aus-gov-forms_num_ranks=5&fmo=on&extra_aus-gov-services_fmo=on&docpath=%2Fsearch.cgi&"
				+ "clive=2&query=visa", cnav.getUrl());
		assertEquals("visa", cnav.getLabel());
		
		assertEquals(3, cn.getCategories().size());
		
		Category type = cn.getCategories().get(0);
		assertEquals(0, type.getMore().intValue());
		assertEquals("type", type.getName());
		assertNull(type.getMoreLink());
		
		assertEquals(8, type.getClusters().size());
		Cluster cluster = type.getClusters().get(0);
		assertEquals(6, cluster.getCount().intValue());
		assertEquals("Student...", cluster.getLabel());
		assertEquals("?profile=info&coverage=info&extra_aus-gov-forms_add_queryterm=%7CC%3Aform&"
				+ "form=simple&collection=gov_combined&cool5=20&extra_aus-gov-faqs_add_queryterm=%7CC%3Afaq&"
				+ "xml=1&advancedSearch=false&extra_aus-gov-faqs_fmo=on&extra_aus-gov-services_num_ranks=5&"
				+ "extra_aus-gov-faqs_num_ranks=5&extra_aus-gov-forms_fmo=on&rmcf=AuJaCs&"
				+ "extra_aus-gov-services_add_queryterm=%7CC%3Aservice&display_featured_pages=false&"
				+ "daat=off&extra_aus-gov-forms_num_ranks=5&fmo=on&extra_aus-gov-services_fmo=on&"
				+ "docpath=%2Fsearch.cgi&clive=2&query=%60Student%20Visa%60", cluster.getHref());
		
		Category topic = cn.getCategories().get(1);
		assertEquals(0, topic.getMore().intValue());
		assertEquals("topic", topic.getName());
		assertEquals("/search/padre-sw.cgi?profile=info&coverage=info&extra_aus-gov-forms_add_queryterm=%7CC%3Aform"
				+ "&form=simple&collection=gov_combined&cool5=20&extra_aus-gov-faqs_add_queryterm=%7CC%3Afaq&xml=1"
				+ "&advancedSearch=false&extra_aus-gov-faqs_fmo=on&extra_aus-gov-services_num_ranks=5&extra_aus-gov-faqs_num_ranks=5"
				+ "&extra_aus-gov-forms_fmo=on&rmcf=AuJaCs&extra_aus-gov-services_add_queryterm=%7CC%3Aservice"
				+ "&display_featured_pages=false&daat=off&extra_aus-gov-forms_num_ranks=5&fmo=on&extra_aus-gov-services_fmo=on"
				+ "&docpath=%2Fsearch.cgi&clive=2&query=visa&topic.max_clusters=40", topic.getMoreLink());
		assertEquals("/search/padre-sw.cgi?profile=info&coverage=info&extra_aus-gov-forms_add_queryterm=%7CC%3Aform"
				+ "&form=simple&collection=gov_combined&cool5=20&extra_aus-gov-faqs_add_queryterm=%7CC%3Afaq&xml=1"
				+ "&advancedSearch=false&extra_aus-gov-faqs_fmo=on&extra_aus-gov-services_num_ranks=5&extra_aus-gov-faqs_num_ranks=5"
				+ "&extra_aus-gov-forms_fmo=on&rmcf=AuJaCs&extra_aus-gov-services_add_queryterm=%7CC%3Aservice"
				+ "&display_featured_pages=false&daat=off&extra_aus-gov-forms_num_ranks=5&fmo=on&extra_aus-gov-services_fmo=on"
				+ "&docpath=%2Fsearch.cgi&clive=2&query=visa&topic.max_clusters=10", topic.getFewerLink());
	}
	
	@Test(expected=XmlParsingException.class)
	public void testInvalidXml() throws IOException, XmlParsingException {
		new StaxStreamParser().parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/invalid.xml.bad")));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testBadlyFormedExplainTag() throws IllegalStateException, XmlParsingException, IOException {
		new StaxStreamParser().parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/badly-formed-explain-tag.xml")));
	}
}


