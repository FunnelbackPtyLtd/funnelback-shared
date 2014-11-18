package com.funnelback.publicui.test.search.lifecycle.output.processors;

import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.GlobalOnlyConfig;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.FixPseudoLiveLinks;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.StaxStreamParser;
import org.junit.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FixPseudoLiveLinksTests {

    @Autowired
    protected MockConfigRepository configRepository;
    
    @Autowired
    protected FixPseudoLiveLinks processor;
    
    @Autowired
    private File searchHome;
    
    protected SearchTransaction st;
    
    @Before
    public void before() throws XmlParsingException, IOException, EnvironmentVariableException {
        
        configRepository.setGlobalConfiguration(new GlobalOnlyConfig(searchHome));
        configRepository.getGlobalConfiguration().setValue(Keys.SERVER_SECRET, "server_secret");
        
        SearchQuestion question = new SearchQuestion();
        question.setQuery("livelinks");
        question.setCollection(new Collection("meta-livelinks", new NoOptionsConfig("meta-livelinks")));
        
        SearchResponse response = new SearchResponse();
        response.setResultPacket(new StaxStreamParser().parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/fix-pseudo-live-links.xml")),
            false));
        
        st = new SearchTransaction(question, response);
        
        // Fill repository with mock collections
        configRepository.addCollection(new Collection("collection-db",
                new NoOptionsConfig("collection-db")
                    .setValue(Keys.COLLECTION_TYPE, Type.database.toString())));
        configRepository.addCollection(new Collection("collection-connector",
                new NoOptionsConfig("collection-connector")
                    .setValue(Keys.COLLECTION_TYPE, Type.connector.toString())));
        configRepository.addCollection(new Collection("collection-trim",
                new NoOptionsConfig("collection-trim")
                    .setValue(Keys.COLLECTION_TYPE, Type.trim.toString())
                    .setValue(Keys.Trim.DEFAULT_LIVE_LINKS, "document")));
        configRepository.addCollection(new Collection("collection-trimpush",
            new NoOptionsConfig("collection-trimpush")
                .setValue(Keys.COLLECTION_TYPE, Type.trim.toString())
                .setValue(Keys.Trim.DEFAULT_LIVE_LINKS, "document")));
        configRepository.addCollection(new Collection("collection-filecopy",
                new NoOptionsConfig("collection-filecopy")
                    .setValue(Keys.COLLECTION_TYPE, Type.filecopy.toString())));
        configRepository.addCollection(new Collection("collection-local",
                new NoOptionsConfig("collection-local")
                    .setValue(Keys.COLLECTION_TYPE, Type.local.toString())));
        configRepository.addCollection(new Collection("collection-web",
                new NoOptionsConfig("collection-web")
                    .setValue(Keys.COLLECTION_TYPE, Type.web.toString())));

    }
    
    @Test
    public void testMissingData() throws OutputProcessorException {
        // No transaction
        processor.processOutput(null);
        
        // No response
        processor.processOutput(new SearchTransaction(null, null));
        
        // No results
        SearchResponse response = new SearchResponse();
        processor.processOutput(new SearchTransaction(null, response));
        
        // No results in packet
        response.setResultPacket(new ResultPacket());
        processor.processOutput(new SearchTransaction(null, response));
    }
    
    @Test
    public void testServeFilecopyLink() throws Exception {
        configRepository.getCollection("collection-filecopy")
            .getConfiguration()
            .setValue(Keys.ModernUI.Serve.FILECOPY_LINK, "custom.link");
        
        processor.processOutput(st);
        
        ResultPacket rp = st.getResponse().getResultPacket();

        // Valid URI = no exception
        URI.create(rp.getResults().get(3).getLiveUrl());

        Assert.assertEquals(
            "custom.link?collection=collection-filecopy&uri="
                +URLEncoder.encode("smb://server.funnelback.com/share/folder/file.ext", "UTF-8")
                + "&auth=y4R5VU2sWYUn4IwErgNlZw",
            rp.getResults().get(3).getLiveUrl());
    }
    
    @Test
    public void testServeTrimDocumentLink() throws Exception {
        configRepository.getCollection("collection-trim")
            .getConfiguration()
            .setValue(Keys.Trim.DEFAULT_LIVE_LINKS, "document")
            .setValue(Keys.ModernUI.Serve.TRIM_LINK_PREFIX, "custom-prefix-");
    
        processor.processOutput(st);
        
        ResultPacket rp = st.getResponse().getResultPacket();

        // Valid URI = no exception
        URI.create(rp.getResults().get(2).getLiveUrl());

        Assert.assertEquals(
            "custom-prefix-document?collection=collection-trim&uri=356&url=trim%3A%2F%2F45%2F356%2F&doc=file%3A%2F%2F%2Ffolder%2Ffile%2F356.pan.txt",
            rp.getResults().get(2).getLiveUrl());
    }

    @Test
    public void testServeTrimReferenceLink() throws Exception {
        configRepository.getCollection("collection-trim")
            .getConfiguration()
            .setValue(Keys.Trim.DEFAULT_LIVE_LINKS, "reference")
            .setValue(Keys.ModernUI.Serve.TRIM_LINK_PREFIX, "custom-prefix-");
        
        processor.processOutput(st);
        
        ResultPacket rp = st.getResponse().getResultPacket();

        // Valid URI = no exception
        URI.create(rp.getResults().get(2).getLiveUrl());

        Assert.assertEquals(
            "custom-prefix-reference?collection=collection-trim&uri=356&url=trim%3A%2F%2F45%2F356%2F&doc=file%3A%2F%2F%2Ffolder%2Ffile%2F356.pan.txt",
            rp.getResults().get(2).getLiveUrl());
    }

    @Test
    public void test() throws UnsupportedEncodingException, OutputProcessorException {
        processor.processOutput(st);
        
        ResultPacket rp = st.getResponse().getResultPacket();

        // Ensure valid URIs
        for (Result r: rp.getResults()) {
            URI.create(r.getLiveUrl());
        }
        
        Assert.assertEquals(
                "/search/serve-db-document.tcgi?collection=collection-db&record_id=1234/",
                rp.getResults().get(0).getLiveUrl());
        
        Assert.assertEquals(
                "/search/serve-connector-document.tcgi?collection=collection-connector&primaryAttribute=1234",
                rp.getResults().get(1).getLiveUrl());

        Assert.assertEquals(
                "/search/serve-trim-document.cgi?collection=collection-trim&uri=356&url=trim%3A%2F%2F45%2F356%2F&doc=file%3A%2F%2F%2Ffolder%2Ffile%2F356.pan.txt",
                rp.getResults().get(2).getLiveUrl());
        
        Assert.assertEquals(
                "/search/serve-filecopy-document.cgi?collection=collection-filecopy&uri="
                    +URLEncoder.encode("smb://server.funnelback.com/share/folder/file.ext", "UTF-8")
                    + "&auth=y4R5VU2sWYUn4IwErgNlZw",
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
        
        Assert.assertEquals(
                "TRIM result without cache link shouldn't have a doc parameter",
                "/search/serve-trim-document.cgi?collection=collection-trim&uri=1234&url=trim%3A%2F%2F45%2F1234%2F",
                rp.getResults().get(10).getLiveUrl());        

        Assert.assertEquals(
            "/search/serve-trim-document.cgi?collection=collection-trimpush&uri=356&url=trim%3A%2F%2F45%2F356&doc=file%3A%2F%2F%2Ffolder%2Ffile%2F356.pan.txt",
            rp.getResults().get(11).getLiveUrl());        

        Assert.assertEquals(
            "/search/serve-trim-document.cgi?collection=collection-trimpush&uri=356&url=trim%3A%2F%2F45%2F356%2Fattachment.png&doc=C%3A%5Cfunnelback%5Cdata%5Ccollection-trimpush%5Clive%5Ccontent%5CG52.warc",
            rp.getResults().get(12).getLiveUrl());        

    }
    
}
