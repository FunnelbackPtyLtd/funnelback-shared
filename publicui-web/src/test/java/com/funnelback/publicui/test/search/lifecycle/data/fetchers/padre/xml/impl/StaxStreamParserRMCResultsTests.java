package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre.xml.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.model.padre.RMCItemResult;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

public class StaxStreamParserRMCResultsTests extends StaxStreamParserTests {
	
	@Before
	@Override
	public void before() throws XmlParsingException, IOException {
		StaxStreamParser parser = new StaxStreamParser();
		rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/complex-rmc-results.xml"), "UTF-8"));
		assertNotNull(rp);
	}
	
	@Test
	@Override
	public void testRMC() {
		Map<String, Integer> rmcs = rp.getRmcs();
		assertEquals(2, rmcs.size());
		
		assertEquals(27, rmcs.get("U:divpermanentdiv").intValue());
		assertEquals(4, rmcs.get("V:courier").intValue());
		
		Map<String, List<RMCItemResult>> rmcItemResults = rp.getRmcItemResults();
		assertEquals(2, rmcItemResults.size());
		
		List<RMCItemResult> l1 = rmcItemResults.get("U:divpermanentdiv");
		assertNotNull(l1);
		assertEquals(3, l1.size());
		
		assertEquals("DUCT FIXER/PLUMBER", l1.get(0).getTitle());
		assertEquals("file:///C:/Data/dev/funnelback/trunk/funnelback/data/cone/xml_data/1/job01766.xml", l1.get(0).getLiveUrl());
		assertEquals("470406ac48d580 427a839042a40 Classified Feed 2007-10-04 2007-10-25 00: 00: 00 Herald Sun 2340485 Melbourne, VIC Australia 3000 DIVMelbourneDIV 3000 DUCT FIXER PLUMBER We require a duct fixer plumber", l1.get(0).getSummary());

		assertEquals("PLUMBER", l1.get(1).getTitle());
		assertEquals("file:///C:/Data/dev/funnelback/trunk/funnelback/data/cone/xml_data/3/job03434.xml", l1.get(1).getLiveUrl());
		assertEquals("46fa0f5748df80 428a55b542a40 Classified Feed 2007-09-26 2007-10-17 00: 00: 00 Leader Newspapers 2159911 Melbourne, VIC Australia DIVMelbourneDIV PLUMBER Experienced plumber required for heating co", l1.get(1).getSummary());

		
		List<RMCItemResult> l2 = rmcItemResults.get("V:courier");
		assertNotNull(l2);
		assertEquals(3, l2.size());
	
		assertEquals("PLUMBER EXP", l2.get(0).getTitle());
		assertEquals("file:///C:/Data/dev/funnelback/trunk/funnelback/data/cone/xml_data/5/job05065.xml", l2.get(0).getLiveUrl());
		assertEquals("46f7ef3248dd0c0 428a554242a40 Classified Feed 2007-09-25 2007-10-16 00: 00: 00 The Courier-Mail 1036304 Bowen Hills, QLD Australia 4006 DIVBowen HillsDIV 4006 PLUMBER exp in commercial plumbing unit", l2.get(0).getSummary());

		assertEquals("PLUMBER/DRAINER", l2.get(2).getTitle());
		assertEquals("file:///C:/Data/dev/funnelback/trunk/funnelback/data/cone/xml_data/3/job03252.xml", l2.get(2).getLiveUrl());
		assertEquals("46fd357a48d4f0 428a554242a40 Classified Feed 2007-09-29 2007-10-20 00: 00: 00 The Courier-Mail 1084677 Bowen Hills, QLD Australia 4006 DIVBowen HillsDIV 4006 PLUMBER DRAINER req d self starter able to", l2.get(2).getSummary());
	}

}


