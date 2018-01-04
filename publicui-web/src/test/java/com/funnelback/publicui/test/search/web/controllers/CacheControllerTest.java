package com.funnelback.publicui.test.search.web.controllers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Store;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.views.StoreView;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.cache.CacheQuestion;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.controllers.CacheController;
import com.funnelback.springmvc.service.security.DLSEnabledChecker;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

public class CacheControllerTest {

    public static final String NON_ASCII_STRING = "Can you deal with the real stuff: 日本 é à ê ö";
    public static final String SIMPLE_NON_ASCII_STRING = "Can you deal with the real stuff: é à ê ö";
    
    private CacheController cacheController;
    private DLSEnabledChecker dLSEnabledCheck; 
    
    @Before
    public void getCacheController() {
        cacheController = new CacheController();
        dLSEnabledCheck = mock(DLSEnabledChecker.class);
        cacheController.setDLSEnabledChecker(dLSEnabledCheck);
    }
    
    @Test
    public void getCharsetTestXFunHeader() {
        Multimap<String, String> m = HashMultimap.create();
        m.put(Store.Header.Charset.toString(), "foobar");
        Assert.assertEquals("foobar", cacheController.getCharset(m));
    }
    
    
    @Test
    public void getCharsetTestContentTypeHeader() {
        Multimap<String, String> m = HashMultimap.create();
        m.put("Content-Type", "derp a charset=foobar");
        Assert.assertEquals("foobar", cacheController.getCharset(m));
    }
    
    @Test
    public void getCharsetTestContentTypeHeaderLC() {
        Multimap<String, String> m = HashMultimap.create();
        m.put("content-type", "derp a charset=\"foobar\"");
        Assert.assertEquals("foobar", cacheController.getCharset(m));
    }
    
    @Test
    public void getCharsetTestContentTypeHeaderMixed() {
        Multimap<String, String> m = HashMultimap.create();
        m.put("content-tYPe", "derp a charset=  \"foobar\" some stuff after");
        Assert.assertEquals("foobar", cacheController.getCharset(m));
    }
    
    @Test
    public void getCharsetTestContentTypeHeaderNoCharset() {
        Multimap<String, String> m = HashMultimap.create();
        m.put("Content-Type", "derp a ");
        Assert.assertEquals(CacheController.DEFAULT_CHARSET, cacheController.getCharset(m));
    }
    
    
    @Test
    public void getContentTest() throws Exception {
        RawBytesRecord r = new RawBytesRecord(SIMPLE_NON_ASCII_STRING.getBytes("iso-8859-15"), "key");
        
        String res = cacheController.getContent(r, "iso-8859-15");
        
        Assert.assertEquals(SIMPLE_NON_ASCII_STRING, res);
    }
    
    @Test
    public void getContentTestBadCharset() throws Exception {
        RawBytesRecord r = new RawBytesRecord(NON_ASCII_STRING.getBytes(), "key"); //Intentional use of default charset
        
        String res = cacheController.getContent(r, "badcharset");
        
        Assert.assertEquals("When a bad charset is detected we should use the default charset", NON_ASCII_STRING, res);
    }
    //
    
    @Test
    public void cacheTestCharsetIso_8859_15() throws Exception {
        cacheTestACharset("iso-8859-15");
    }
    
    @Test
    public void cacheTestCharsetUTF_8() throws Exception {
        cacheTestACharset("utf-8");
    }
    
    private void cacheTestACharset(String charset) throws Exception {
        CacheQuestion question = mock(CacheQuestion.class);
        when(question.getUrl()).thenReturn("http://coal.ila/");
        
        Config config = mock(Config.class);
        when(config.valueAsBoolean(Keys.UI_CACHE_DISABLED)).thenReturn(false);
        Collection collection = mock(Collection.class);
        when(collection.getConfiguration()).thenReturn(config);
        when(question.getCollection()).thenReturn(collection);
        
        DataRepository dataRepository = mock(DataRepository.class);
        
        ListMultimap<String, String> m = ArrayListMultimap.create();
        m.put("content-type", "charset=\"" + charset + "\"");
        
        RawBytesRecord r = new RawBytesRecord(SIMPLE_NON_ASCII_STRING.getBytes(charset), "key");
        RecordAndMetadata<RawBytesRecord> ram = new RecordAndMetadata<RawBytesRecord>(r, m);
        
        when(dataRepository.getCachedDocument(collection, StoreView.live, question.getUrl())).thenReturn((RecordAndMetadata) ram);
        
        MetricRegistry metricRegistry = mock(MetricRegistry.class);
        when(metricRegistry.counter(any())).thenReturn(mock(Counter.class));
        
        CacheController cacheController = this.cacheController;
        cacheController.setDataRepository(dataRepository);
        cacheController.setMetrics(metricRegistry);
        
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://ron/"));
        ModelAndView modelAndView = cacheController.cache(httpServletRequest, null, question);
        
        Document doc = (Document) modelAndView.getModelMap().get(CacheController.MODEL_DOCUMENT);
        
        Assert.assertTrue("Expected to find: '" + SIMPLE_NON_ASCII_STRING +"' yet found: \r\n:" + doc.text(), (doc.text()).contains(SIMPLE_NON_ASCII_STRING));
        
    }
    
    
}
