package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.DefaultPadreForking;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.index.QueryReadLock;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.StaxStreamParser;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinNT;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DefaultPadreForkingTests {

    @Autowired
    private I18n i18n;
    
    private DefaultPadreForking forking;
    
    @Autowired
    private File searchHome;
    
    @Autowired
    private QueryReadLock queryReadLock;
    
    @Before
    public void before() {
        forking = new DefaultPadreForking();
        forking.setI18n(i18n);
        forking.setPadreWaitTimeout(30000);
        forking.setPadreXmlParser(new StaxStreamParser());
        forking.setSearchHome(searchHome);
        forking.setQueryReadLock(queryReadLock);
    }
    
    @Test
    public void testMissingData() throws DataFetchException, FileNotFoundException, EnvironmentVariableException {
        SearchTransaction st = new SearchTransaction(null, null);
        forking.fetchData(st);
        Assert.assertNull(st.getResponse());
        
        SearchQuestion sq = new SearchQuestion();
        st = new SearchTransaction(sq, null);
        forking.fetchData(st);
        Assert.assertNull(st.getResponse());
        
        sq.setQuery("query");
        forking.fetchData(st);
        Assert.assertNull(st.getResponse());
        
        sq.setQuery(null);
        sq.setCollection(new Collection("padre-forking", new NoOptionsConfig(searchHome, "padre-forking")));
        forking.fetchData(st);
        Assert.assertNull(st.getResponse());
        
    }
    
    @Test
    public void testNoExplicitQueryShouldReturnResults() throws Exception {
        List<String> qpOptions = new ArrayList<String>(Arrays.asList(
            new String[]{"src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml"}));
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getQuestion().setCollection(new Collection("padre-forking", new NoOptionsConfig(searchHome, "padre-forking").setValue("query_processor", getMockPadre())));
        st.getQuestion().getDynamicQueryProcessorOptions().addAll(qpOptions);
        st.getQuestion().getMetaParameters().add("t:test");
        forking.fetchData(st);
        Assert.assertNotNull(st.getResponse());
        Assert.assertEquals(FileUtils.readFileToString(new File("src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml")), st.getResponse().getRawPacket());
        Assert.assertEquals(10, st.getResponse().getResultPacket().getResults().size());
        Assert.assertEquals("Online visa applications", st.getResponse().getResultPacket().getResults().get(0).getTitle());
    }
    
    @Test
    public void test() throws DataFetchException, EnvironmentVariableException, IOException {
        List<String> qpOptions = new ArrayList<String>(Arrays.asList(
            new String[]{"src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml"}));
        
        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("padre-forking", new NoOptionsConfig(searchHome, "padre-forking").setValue("query_processor", getMockPadre())));
        qs.getDynamicQueryProcessorOptions().addAll(qpOptions);
        qs.setQuery("test");
        SearchTransaction st = new SearchTransaction(qs, new SearchResponse());
        
        
        forking.fetchData(st);
        
        Assert.assertNotNull(st.getResponse());
        Assert.assertEquals(FileUtils.readFileToString(new File("src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml")), st.getResponse().getRawPacket());
        Assert.assertEquals(10, st.getResponse().getResultPacket().getResults().size());
        Assert.assertEquals("Online visa applications", st.getResponse().getResultPacket().getResults().get(0).getTitle());
    }
    
    @Test
    public void testInvalidPacket() throws Exception {
        List<String> qpOptions = new ArrayList<String>(Arrays.asList(
            new String[]{"src/test/resources/dummy-search_home/conf/padre-forking/mock-packet-invalid.xml.bad"}));
        
        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("padre-forking", new NoOptionsConfig(searchHome, "padre-forking").setValue("query_processor", getMockPadre())));
        qs.setQuery("test");
        qs.getDynamicQueryProcessorOptions().addAll(qpOptions);
        SearchTransaction ts = new SearchTransaction(qs, new SearchResponse());
        
        
        try {
            forking.fetchData(ts);
            Assert.fail();
        } catch (DataFetchException dfe) {
            Assert.assertEquals(XmlParsingException.class, dfe.getCause().getClass());
        }
    }
    
    @Test
    public void testInvalidQueryProcessor() throws FileNotFoundException, EnvironmentVariableException {
        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("padre-forking", new NoOptionsConfig(searchHome, "padre-forking").setValue("query_processor", "invalid")));
        qs.setQuery("test");
        SearchTransaction ts = new SearchTransaction(qs, new SearchResponse());
        
        
        try {
            forking.fetchData(ts);
            Assert.fail();
        } catch (DataFetchException dfe) {
            Assert.assertEquals(PadreForkingException.class, dfe.getCause().getClass());
        }
    }
    
    @Test
    public void testErrorReturn() throws Exception {
        String qp = "mock-padre-error";
        if (OS.isFamilyWindows()) {
            qp += ".bat";
        } else {
            qp += ".sh";
        }

        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("padre-forking", new NoOptionsConfig(searchHome, "padre-forking").setValue("query_processor", qp)));
        qs.setQuery("test");
        SearchTransaction ts = new SearchTransaction(qs, new SearchResponse());
        
        
        forking.fetchData(ts);
        
        Assert.assertEquals(68, ts.getResponse().getReturnCode());
    }
    
    @Test
    public void testWindowsNative() throws Exception {
        if (OS.isFamilyWindows()) {
            
            List<String> qpOptions = new ArrayList<String>(Arrays.asList(
                new String[]{"src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml"}));
            
            SearchQuestion qs = new SearchQuestion();
            qs.setCollection(new Collection("padre-forking", new NoOptionsConfig(searchHome, "padre-forking").setValue("query_processor", getMockPadre())));
            qs.getDynamicQueryProcessorOptions().addAll(qpOptions);
            qs.setQuery("test");
            qs.setImpersonated(true);
            SearchTransaction ts = new SearchTransaction(qs, new SearchResponse());
            
            Advapi32.INSTANCE.ImpersonateSelf(WinNT.SECURITY_IMPERSONATION_LEVEL.SecurityImpersonation);
            forking.fetchData(ts);
            Advapi32.INSTANCE.RevertToSelf();
            
            Assert.assertNotNull(ts.getResponse());
            Assert.assertEquals(FileUtils.readFileToString(new File("src/test/resources/dummy-search_home/conf/padre-forking/mock-packet.xml")), ts.getResponse().getRawPacket());
            Assert.assertEquals(10, ts.getResponse().getResultPacket().getResults().size());
            Assert.assertEquals("Online visa applications", ts.getResponse().getResultPacket().getResults().get(0).getTitle());
        }
    }
    
    private String getMockPadre() {
        if (OS.isFamilyWindows()) {
            return "readfile.exe";
        } else {
            return "mock-padre.sh";
        }
    }
    
}
