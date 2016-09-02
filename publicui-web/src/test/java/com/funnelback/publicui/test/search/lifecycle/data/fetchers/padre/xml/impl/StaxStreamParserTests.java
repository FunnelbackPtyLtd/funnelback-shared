package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre.xml.impl;

import com.funnelback.publicui.search.model.padre.*;
import com.funnelback.publicui.search.model.padre.QSup.Source;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

import org.junit.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

public class StaxStreamParserTests {

    protected ResultPacket rp;
    
    @Before
    public void before() throws XmlParsingException, IOException {
        StaxStreamParser parser = new StaxStreamParser();
        rp = parser.parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/complex.xml"), "UTF-8"),
            false);
        assertNotNull(rp);
    }
    
    @Test
    public void testMetadataSums() throws Exception {
//       Format is:
//       <rm_sums>
//           <s on="failures2">7218.000000</s>
//           <s on="failures1">7210.100000</s>
//       </rm_sums>
        
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/metadata-sums.xml"), "UTF-8"),
            false);
        
        Assert.assertEquals(new Double(7218.000000), rp.getMetadataSums().get("failures2"));
        Assert.assertEquals(new Double(7210.100000), rp.getMetadataSums().get("failures1"));
        Assert.assertEquals(2, rp.getMetadataSums().size());
    }
    
    @Test
    public void testMetadataSumsEmpty() throws Exception {   
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/metadata-sums-EMPTY.xml"), "UTF-8"),
            false);
        
        Assert.assertEquals(0, rp.getMetadataSums().size());
    }
    
    @Test
    public void testUniqueCountsByGroups() throws Exception {
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/unique_counts_by_groups.xml"), "UTF-8"),
            false);
        Assert.assertEquals(4, rp.getUniqueCountsByGroups().size());
        
        Assert.assertEquals(new UniqueByGroup("FunAAProfile", "FunAAOccurrences244"), 
                                rp.getUniqueCountsByGroups().get(0));
        
        Assert.assertEquals("FunAAProfile", rp.getUniqueCountsByGroups().get(2).getBy());
        Assert.assertEquals("FunAAOccurrences143", rp.getUniqueCountsByGroups().get(2).getOn());
        
        Assert.assertEquals(new Double(3D), rp.getUniqueCountsByGroups().get(2).getGroupAndCounts().get("profile3"));
        
    }
    
    @Test
    public void testUniqueCountsByGroupsEmpty() throws Exception {
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/unique_counts_by_groups-EMPTY.xml"), "UTF-8"),
            false);
        Assert.assertEquals(0, rp.getUniqueCountsByGroups().size());
    }
    
    @Test
    public void testSumByGroup() throws Exception {
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/sum_by_groups.xml"), "UTF-8"),
            false);
        Assert.assertEquals(3, rp.getSumByGroups().size());
        
        Assert.assertEquals(new SumByGroup("profile", "failures3"), rp.getSumByGroups().get(2));
        
        Assert.assertEquals("profile", rp.getSumByGroups().get(0).getBy());
        Assert.assertEquals("failures1", rp.getSumByGroups().get(0).getOn());
        
        Assert.assertEquals(new Double(4010D), rp.getSumByGroups().get(0).getGroupAndSums().get("health"));
        
        Assert.assertEquals(new Double(3002D), rp.getSumByGroups().get(1).getGroupAndSums().get("health foobar"));
    }
    
    @Test
    public void testSumByGroupEmpty() throws Exception {
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/sum_by_groups-EMPTY.xml"), "UTF-8"),
            false);
        Assert.assertEquals(rp.getSumByGroups().size(), 0);
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
        
        assertNotNull(rp.getCoolerWeights().get(new CoolerWeighting("content", 0)));
        assertNotNull(rp.getCoolerWeights().get(new CoolerWeighting("offlink", 2)));
        assertNotNull(rp.getCoolerWeights().get(new CoolerWeighting("urllen", 3)));
        
        assertEquals(0.41,rp.getCoolerWeights().get(new CoolerWeighting("content", 0)),0.0001);
        assertEquals(0.14,rp.getCoolerWeights().get(new CoolerWeighting("offlink", 2)),0.0001);
        assertEquals(0.14,rp.getCoolerWeights().get(new CoolerWeighting("urllen", 3)),0.0001);
    }
    
    @Test
    public void testCoolerNames() {
        assertEquals(12, rp.getCoolerNames().size());
        assertEquals("content weight",rp.getCoolerNames().get(new CoolerWeighting("content", 0)));
        assertEquals("onsite link weight",rp.getCoolerNames().get(new CoolerWeighting("onlink", 1)));
        assertEquals("offsite link weight",rp.getCoolerNames().get(new CoolerWeighting("offlink", 2)));
        assertEquals("URL length weight",rp.getCoolerNames().get(new CoolerWeighting("urllen", 3)));
        assertEquals("recency weight",rp.getCoolerNames().get(new CoolerWeighting("recency", 5)));
        assertEquals("URL attractiveness ",rp.getCoolerNames().get(new CoolerWeighting("urltype", 6)));
        assertEquals("non-binariness ",rp.getCoolerNames().get(new CoolerWeighting("nonbin", 10)));
        assertEquals("freedom from ads",rp.getCoolerNames().get(new CoolerWeighting("no_ads", 11)));
        assertEquals("implicit phrase match score",rp.getCoolerNames().get(new CoolerWeighting("imp_phrase", 12)));
        assertEquals("bias in favour of principal servers ",rp.getCoolerNames().get(new CoolerWeighting("mainhosts", 20)));
        assertEquals("",rp.getCoolerNames().get(new CoolerWeighting("host_incoming_link_score", 23)));
        assertEquals("",rp.getCoolerNames().get(new CoolerWeighting("lexical_span_score", 67)));
    }

    @Test
    public void testStemEquivs() {
        Set<String> expected = new HashSet<String>( Arrays.asList(new String[] {"visa"}));
        assertEquals(expected,rp.getStemmedEquivs().get("visa"));
        assertEquals(expected,rp.getStemmedEquivs().get("visas"));
    }
    
    @Test
    public void testExplain() {
        String expectedStopWords = "a ai am an and any are as at au aussi aux avec be been but by c ca can ce ceci cela ces cet cette ci comme d dans de des do du elle elles en est et etc for from had has her his how i if  il ils in is it j je l la le les leur leurs lui ma mais me mes moi mon my n ne ni non nor not of on ont or ou out par pas qu quand que qui quoi se ses si sont suis sur that the their then there to toi tu un une vous was were what when where who why with y you";
        
        Explain e = rp.getResults().get(0).getExplain();
        assertEquals(0.639, e.getFinalScore(), 0.0001);
        assertEquals(2, e.getConsat());
        assertEquals(0.662,e.getLenratio(),0.0001);
        
        assertNotNull(e.getFeatureScores().get(new CoolerWeighting("content", 0)));
        assertNotNull(e.getFeatureScores().get(new CoolerWeighting("offlink", 2)));
        assertNotNull(e.getFeatureScores().get(new CoolerWeighting("urllen", 3)));
        
        assertEquals(0.997,e.getFeatureScores().get(new CoolerWeighting("content", 0)),0.0001);
        assertEquals(0,e.getFeatureScores().get(new CoolerWeighting("offlink", 2)),0.0001);
        assertEquals(0.512,e.getFeatureScores().get(new CoolerWeighting("urllen", 3)),0.0001);
        
        for (int i=1; i<rp.getResults().size(); i++) {
            assertNull(rp.getResults().get(i).getExplain());
        }
        
        String[] expectedStopWordsArray = expectedStopWords.split("\\s+");
        int i =0;
        assertEquals("All stop words should be present",expectedStopWordsArray.length,rp.getStopWords().size());
        for(String stopWord : rp.getStopWords()) {
            assertEquals("Stop words should match expected stop words",expectedStopWordsArray[i],stopWord);
            i++;
        }
    }
    
    @Test
    public void testWinType() {
        Map<CoolerWeighting, String> winTypes = rp.getExplainTypes();
        assertEquals(72, winTypes.size());
        
        assertEquals("max_other",winTypes.get(new CoolerWeighting("content", 0)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("onlink", 1)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("offlink", 2)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("urllen", 3)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("qie", 4)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("recency", 5)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("urltype", 6)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("annie", 7)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("domain_weight", 8)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("geoprox", 9)));
        assertEquals("max_possible",winTypes.get(new CoolerWeighting("nonbin", 10)));
        assertEquals("max_possible",winTypes.get(new CoolerWeighting("no_ads", 11)));
        assertEquals("max_possible_multiword_only",winTypes.get(new CoolerWeighting("imp_phrase", 12)));
        assertEquals("max_possible",winTypes.get(new CoolerWeighting("consistency", 13)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("log_annie", 14)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("annie_rank", 16)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("BM25F", 17)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("an_okapi", 18)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("comp_wt", 21)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("BM25F_rank", 19)));
        assertEquals("max_possible",winTypes.get(new CoolerWeighting("mainhosts", 20)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("document_number", 22)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("host_incoming_link_score", 23)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("host_click_score", 24)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("host_linking_hosts_score", 25)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("host_linked_hosts_score", 26)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("host_rank_in_crawl_order_score", 27)));
        assertEquals("max_other",winTypes.get(new CoolerWeighting("host_domain_shallowness_score", 28)));        
    }
    
    @Test
    public void testQueryAndCollection() {
        assertEquals("visa^0.345", rp.getQuery());
        assertEquals("visa", rp.getQueryAsProcessed());
        assertEquals("visa raw", rp.getQueryRaw());
        assertEquals("visa system raw", rp.getQuerySystemRaw());
        assertEquals("visa^0.345", rp.getQueryCleaned());
        
        assertEquals("gov_combined", rp.getCollection());

        assertEquals(2, rp.getQSups().size());
        assertEquals(Source.SPEL, rp.getQSups().get(0).getSrc());
        assertEquals("taxation", rp.getQSups().get(0).getQuery());
        assertEquals(Source.SYNS, rp.getQSups().get(1).getSrc());
        assertEquals("u:australia.gov.au t:\"tax\"", rp.getQSups().get(1).getQuery());
    }
    
    @Test
    public void testSvgs() {
        assertEquals(1, rp.getSvgs().size());
        assertEquals("syntaxtree", rp.getSvgs().keySet().iterator().next());
        
        String svg = rp.getSvgs().values().iterator().next();
        assertTrue(svg.contains("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"100%\" height=\"332\">"));
        assertTrue(svg.contains("<rect x=\"0\" y=\"83\" width=\"120\" height=\"50\" rx=\"4\" ry=\"4\" style=\"fill:rgb(255,20,20);stroke:rgb(0,0,0)\"/>"));
        assertTrue(svg.contains("</svg>"));
    }
    
    @Test
    public void testResultsSummary() {
        assertEquals(200, rp.getResultsSummary().getFullyMatching().intValue());
        assertEquals(42, rp.getResultsSummary().getCollapsed().intValue());
        assertEquals(0, rp.getResultsSummary().getPartiallyMatching().intValue());
        assertEquals(200, rp.getResultsSummary().getTotalMatching().intValue());
        assertTrue(rp.getResultsSummary().getEstimatedCounts());
        assertEquals(4, rp.getResultsSummary().getCarriedOverFtd().intValue());
        assertEquals(57, rp.getResultsSummary().getTotalDistinctMatchingUrls().intValue());
        assertEquals(10, rp.getResultsSummary().getNumRanks().intValue());
        assertEquals(1, rp.getResultsSummary().getCurrStart().intValue());
        assertEquals(10, rp.getResultsSummary().getCurrEnd().intValue());
        assertEquals(2, rp.getResultsSummary().getPrevStart().intValue());
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
    public void testEntityList() {
        Map<String, Integer> entityList = rp.getEntityList();
        assertNotNull(entityList);
        assertEquals(5, entityList.size());
        
        assertEquals(39, entityList.get("GLOUCESTER").intValue());
        assertEquals(36, entityList.get("LEAR").intValue());
        assertEquals(34, entityList.get("KENT").intValue());
        assertEquals(32, entityList.get("EDGAR").intValue());
        assertEquals(25, entityList.get("EDMUND").intValue());
    }
    
    @Test
    public void testRMC() {
        Map<String, Integer> rmcs = rp.getRmcs();
        assertEquals(143, rmcs.size());
        
        // Pick some random keys/values
        assertEquals(139, rmcs.get("A:All").intValue());
        assertEquals(1, rmcs.get("A:Women").intValue());
        assertEquals(15, rmcs.get("C:Life Events").intValue());
        assertEquals(1, rmcs.get("J:VIC").intValue());
        assertEquals(3, rmcs.get("a:NSW Government agency").intValue());
        assertEquals(1, rmcs.get("a:Australian Federal Police").intValue());
        assertEquals(2, rmcs.get("s:tourists").intValue());
        assertEquals(1, rmcs.get("s:real-estate").intValue());
        assertEquals(1, rmcs.get("-s:").intValue());
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

    @Test
    public void testGScopeCount() {
        Map<Integer, Integer> gscopes = rp.getGScopeCounts();
        assertEquals(2, gscopes.size());
        
        assertEquals(54, gscopes.get(1).intValue());
        assertEquals(6, gscopes.get(2).intValue());        
    }
    
    @Test
    public void testDateCount() {
        Map<String, DateCount> dcs = rp.getDateCounts();
        Assert.assertEquals(8, dcs.size());
        
        assertEquals(1, dcs.get("d:In the past 3 months").getCount());
        assertEquals("d<25Jun2003>24Mar2003", dcs.get("d:In the past 3 months").getQueryTerm());
        
        assertEquals(2, dcs.get("d:2003").getCount());
        assertEquals("d=2003", dcs.get("d:2003").getQueryTerm());

        assertEquals(8, dcs.get("d:Today").getCount());
        assertEquals("d=24Jun2003", dcs.get("d:Today").getQueryTerm());

    }
    
    @Test
    public void testTierBars() {
        Assert.assertEquals(2, rp.getTierBars().size());
        
        TierBar tb = rp.getTierBars().get(0);
        Assert.assertEquals(0, tb.getFirstRank());
        Assert.assertEquals(8, tb.getLastRank());
        Assert.assertEquals(2, tb.getMatched());
        Assert.assertEquals(2, tb.getOutOf());
        Assert.assertEquals("20110717", new SimpleDateFormat("yyyyMMdd").format(tb.getEventDate()));

        tb = rp.getTierBars().get(1);
        Assert.assertEquals(8, tb.getFirstRank());
        Assert.assertEquals(10, tb.getLastRank());
        Assert.assertEquals(1, tb.getMatched());
        Assert.assertEquals(2, tb.getOutOf());
        Assert.assertNull(null, tb.getEventDate());
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
    public void testCollapsedResults() {
        Result first = rp.getResults().get(0);

        assertEquals("ABCDEF", first.getCollapsed().getSignature());
        assertEquals(12, first.getCollapsed().getCount());
        assertEquals("a", first.getCollapsed().getColumn());
        assertEquals(2, first.getCollapsed().getResults().size());

        Result r1 = first.getCollapsed().getResults().get(0);
        assertEquals("PLUMBERS FAMILY PLUMBING", r1.getTitle());
        assertEquals("file:///C:/Data/dev/funnelback/trunk/test/funnelback-selenium/additional-resources/faceted_navigation_test/xml_data/4/job04281.xml", r1.getLiveUrl());
        assertEquals(3, r1.getMetaData().size());
        assertEquals("NSW", r1.getMetaData().get("X"));
        assertEquals("Plumbing", r1.getMetaData().get("Y"));
        assertEquals("DIVTrades & ServicesDIV", r1.getMetaData().get("Z"));

        Result r2 = first.getCollapsed().getResults().get(1);
        assertEquals("PLUMBERS &AMP; DRAINERS", r2.getTitle());
        assertEquals("file:///C:/Data/dev/funnelback/trunk/test/funnelback-selenium/additional-resources/faceted_navigation_test/xml_data/2/job02074.xml", r2.getLiveUrl());
        assertEquals(2, r2.getMetaData().size());
        assertEquals("ACT", r2.getMetaData().get("X"));
        assertEquals("Plumbing", r2.getMetaData().get("Y"));
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
        assertEquals("/search/padre-rf.cgi?&profile=_default&xml=&fluent=5&collection=info-aus&query=visa&comp=0&dox=29&aoi=7&vsimple=on&daat=0&script=/search/search.cgi", first.getExploreLink());
        assertEquals(3, first.getTags().size());
        assertTrue(first.getTags().contains("cat"));
        assertTrue(first.getTags().contains("dog"));
        assertTrue(first.getTags().contains("kangaroo"));
        
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
        assertNull(last.getCollapsed());
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
        
        // Geo features
        Result fifth = rp.getResults().get(4);
        Assert.assertEquals(1295.8, fifth.getKmFromOrigin(), .001);
        // Padre can return XML without <summary> tags on some conditions
        Assert.assertNull(fifth.getSummary());
        // And also without cache_url too
        Assert.assertNull(fifth.getCacheUrl());

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
        
        assertEquals("\\bmanager\\b|\\bassistant\\b", rp.getQueryHighlightRegex());
        
        assertEquals(2, rp.getOrigin().length);
        assertEquals(-42.43, rp.getOrigin()[0].floatValue(), .001);
        assertEquals(83.49, rp.getOrigin()[1].floatValue(), .001);
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
        new StaxStreamParser().parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/invalid.xml.bad")),
            false);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testBadlyFormedExplainTag() throws IllegalStateException, XmlParsingException, IOException {
        new StaxStreamParser().parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/badly-formed-explain-tag.xml")),
            false);
    }
    
    @Test
    public void testBoundingBox() throws Exception{
        Map<String, GeoBoundingBox> boxes = new StaxStreamParser().parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/bounding-box.xml")),
            false).boundingBoxes;
        Assert.assertTrue(boxes.containsKey("location"));
        GeoBoundingBox box = boxes.get("location");
        Assert.assertEquals(45.000000d, box.getUpperRight().getLatitude(), 0.0d);
        Assert.assertEquals(100.000000d, box.getUpperRight().getLongitude(), 0.0d);
        Assert.assertEquals(5.000000d, box.getLowerLeft().getLatitude(), 0.0d);
        Assert.assertEquals(8.000000d, box.getLowerLeft().getLongitude(), 0.0d);
    }
    
    @Test
    public void testNanOrgin() throws Exception{
        ResultPacket resultPacket = new StaxStreamParser().parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/bounding-box.xml")),
            false);
        Assert.assertEquals("a nan,nan orgin returned by padre means the orgin was not set but was used", 0, resultPacket.getOrigin().length);
    }
}


