package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.PadreXmlParsingException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;
import com.funnelback.publicui.search.lifecycle.output.processors.FixPseudoLiveLinks;
import com.funnelback.publicui.search.model.Collection;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:test_data/spring/applicationContext.xml")
public class FixPseudoLiveLinksTests {

	@Autowired
	private MockConfigRepository configRepository;
	
	@Autowired
	private FixPseudoLiveLinks processor;
	
	private SearchTransaction st;
	
	@Before
	public void before() throws PadreXmlParsingException, IOException, EnvironmentVariableException {
		
		SearchQuestion question = new SearchQuestion();
		question.setQuery("livelinks");
		question.setCollection(new Collection("meta-livelinks", null));
		
		SearchResponse response = new SearchResponse();
		response.setResultPacket(new StaxStreamParser().parse(FileUtils.readFileToString(new File("test_data/padre-xml/fix-pseudo-live-links.xml"))));
		
		st = new SearchTransaction(question, response);
		
		// Fill repository with mock collections
		configRepository.addCollection(new Collection("collection-db",
				new NoOptionsConfig("collection-db")
					.setValue(Keys.COLLECTION_TYPE, Collection.Type.database.toString())));
		configRepository.addCollection(new Collection("collection-connector",
				new NoOptionsConfig("collection-connector")
					.setValue(Keys.COLLECTION_TYPE, Collection.Type.connector.toString())));
		configRepository.addCollection(new Collection("collection-trim",
				new NoOptionsConfig("collection-trim")
					.setValue(Keys.COLLECTION_TYPE, Collection.Type.trim.toString())
					.setValue(Keys.Trim.DEFAULT_LIVE_LINKS, "document")));
		configRepository.addCollection(new Collection("collection-filecopy",
				new NoOptionsConfig("collection-filecopy")
					.setValue(Keys.COLLECTION_TYPE, Collection.Type.filecopy.toString())));
		configRepository.addCollection(new Collection("collection-local",
				new NoOptionsConfig("collection-local")
					.setValue(Keys.COLLECTION_TYPE, Collection.Type.local.toString())));
		configRepository.addCollection(new Collection("collection-web",
				new NoOptionsConfig("collection-web")
					.setValue(Keys.COLLECTION_TYPE, Collection.Type.web.toString())));


	}
	
	@Test
	public void testErrorCases() {
		// No response
		processor.process(new SearchTransaction(null, null));
		
		// No results
		SearchResponse response = new SearchResponse();
		processor.process(new SearchTransaction(null, response));
		
		// No results in packet
		response.setResultPacket(new ResultPacket());
		processor.process(new SearchTransaction(null, response));
	}
	
	@Test
	public void test() throws UnsupportedEncodingException {
		processor.process(st);
		
		ResultPacket rp = st.getResponse().getResultPacket();
		
		Assert.assertEquals(
				"serve-db-document.tcgi?collection=collection-db&record_id=1234/",
				rp.getResults().get(0).getLiveUrl());
		
		Assert.assertEquals(
				"serve-connector-document.tcgi?collection=collection-connector&primaryAttribute=1234",
				rp.getResults().get(1).getLiveUrl());

		Assert.assertEquals(
				"serve-trim-document.cgi?collection=collection-trim&uri=356&doc=file:///folder/file/356.pan.txt",
				rp.getResults().get(2).getLiveUrl());
		
		Assert.assertEquals(
				"serve-filecopy-document.cgi?collection=collection-filecopy&uri="+URLEncoder.encode("smb://server.funnelback.com/share/folder/file.ext", "UTF-8"),
				rp.getResults().get(3).getLiveUrl());

		Assert.assertEquals(
				"file:///C:/folder/file.ext",
				rp.getResults().get(4).getLiveUrl());

		Assert.assertEquals(
				"file:///folder/file.ext",
				rp.getResults().get(5).getLiveUrl());

		Assert.assertEquals(
				"Unknown collection URL should be left inchanged",
				"http://www.result.com",
				rp.getResults().get(6).getLiveUrl());

		Assert.assertEquals(
				"Web collection should have unchanged URL",
				"http://www.site.com/folder/result.html",
				rp.getResults().get(7).getLiveUrl());

		Assert.assertEquals(
				"DB result without local:// prefix should be left unchanged",
				"custom-show-db-record.cgi?collection=collection-db&record_id=1234/",
				rp.getResults().get(8).getLiveUrl());

		Assert.assertEquals(
				"TRIM result without trim:// prefix should be left unchanged",
				"http://trim-ws.company.com/show-record?uri=356",
				rp.getResults().get(9).getLiveUrl());		
		
	}
	
}
