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
import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.GlobalOnlyConfig;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.FixPseudoLiveLinks;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FixPseudoLiveLinksPushTests {

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
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/fix-pseudo-live-links-push.xml")),
            false));
        
        st = new SearchTransaction(question, response);
        
        configRepository.addCollection(new Collection("collection-push",
                new NoOptionsConfig("collection-push")
                    .setValue(Keys.COLLECTION_TYPE, Type.push.toString())
                    .setValue(Keys.Trim.DEFAULT_LIVE_LINKS, "document")));
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
        configRepository.getCollection("collection-push")
            .getConfiguration()
            .setValue(Keys.ModernUI.Serve.FILECOPY_LINK, "custom.link");
        
        processor.processOutput(st);
        
        ResultPacket rp = st.getResponse().getResultPacket();
        
        Assert.assertEquals(
            "custom.link?collection=collection-push&uri="
                +URLEncoder.encode("smb://server.funnelback.com/share/folder/file.ext", "UTF-8")
                + "&auth=y4R5VU2sWYUn4IwErgNlZw",
            rp.getResults().get(3).getLiveUrl());
    }
    
    @Test
    public void testServeTrimDocumentLink() throws Exception {
        configRepository.getCollection("collection-push")
            .getConfiguration()
            .setValue(Keys.Trim.DEFAULT_LIVE_LINKS, "document")
            .setValue(Keys.ModernUI.Serve.TRIM_LINK_PREFIX, "custom-prefix-");
    
        processor.processOutput(st);
        
        ResultPacket rp = st.getResponse().getResultPacket();
        
        Assert.assertEquals(
            "custom-prefix-document?collection=collection-push&uri=356&url=trim://45/356/&doc=file:///folder/file/356.pan.txt",
            rp.getResults().get(2).getLiveUrl());
    }

    @Test
    public void testServeTrimReferenceLink() throws Exception {
        configRepository.getCollection("collection-push")
            .getConfiguration()
            .setValue(Keys.Trim.DEFAULT_LIVE_LINKS, "reference")
            .setValue(Keys.ModernUI.Serve.TRIM_LINK_PREFIX, "custom-prefix-");
        
        processor.processOutput(st);
        
        ResultPacket rp = st.getResponse().getResultPacket();
        
        Assert.assertEquals(
            "custom-prefix-reference?collection=collection-push&uri=356&url=trim://45/356/&doc=file:///folder/file/356.pan.txt",
            rp.getResults().get(2).getLiveUrl());
    }

    @Test
    public void test() throws UnsupportedEncodingException, OutputProcessorException {
        processor.processOutput(st);
        
        ResultPacket rp = st.getResponse().getResultPacket();
        
        Assert.assertEquals(
                "/search/serve-db-document.tcgi?collection=collection-db&record_id=1234/",
                rp.getResults().get(0).getLiveUrl());
        
        Assert.assertEquals(
                "/search/serve-connector-document.tcgi?collection=collection-connector&primaryAttribute=1234",
                rp.getResults().get(1).getLiveUrl());

        Assert.assertEquals(
                "/search/serve-trim-document.cgi?collection=collection-push&uri=356&url=trim://45/356/&doc=file:///folder/file/356.pan.txt",
                rp.getResults().get(2).getLiveUrl());
        
        Assert.assertEquals(
                "/search/serve-filecopy-document.cgi?collection=collection-push&uri="
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
                "Web collection should have unchanged URL",
                "http://www.site.com/folder/result.html",
                rp.getResults().get(6).getLiveUrl());

    }
    
}
