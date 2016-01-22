package com.funnelback.publicui.test.search.web.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;
import org.jsoup.nodes.Document;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.views.StoreView;
import com.funnelback.publicui.search.model.transaction.cache.CacheQuestion;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.controllers.CacheController;

import static org.mockito.Matchers.any;

public class CacheControllerTest {

    public static final String NON_ASCII_STRING = "Can you deal with the real stuff: 日本 é à ê ö";
    public static final String SIMPLE_NON_ASCII_STRING = "Can you deal with the real stuff: é à ê ö";
    
    @Test
    public void getCharsetTestXFunHeader() {
        Map<String, String> m = new HashMap<>();
        m.put(Store.Header.Charset.toString(), "foobar");
        Assert.assertEquals("foobar", new CacheController().getCharset(m));
    }
    
    
    @Test
    public void getCharsetTestContentTypeHeader() {
        Map<String, String> m = new HashMap<>();
        m.put("Content-Type", "derp a charset=foobar");
        Assert.assertEquals("foobar", new CacheController().getCharset(m));
    }
    
    @Test
    public void getCharsetTestContentTypeHeaderLC() {
        Map<String, String> m = new HashMap<>();
        m.put("content-type", "derp a charset=\"foobar\"");
        Assert.assertEquals("foobar", new CacheController().getCharset(m));
    }
    
    @Test
    public void getCharsetTestContentTypeHeaderMixed() {
        Map<String, String> m = new HashMap<>();
        m.put("content-tYPe", "derp a charset=  \"foobar\" some stuff after");
        Assert.assertEquals("foobar", new CacheController().getCharset(m));
    }
    
    @Test
    public void getCharsetTestContentTypeHeaderNoCharset() {
        Map<String, String> m = new HashMap<>();
        m.put("Content-Type", "derp a ");
        Assert.assertEquals(CacheController.DEFAULT_CHARSET, new CacheController().getCharset(m));
    }
    
    
    @Test
    public void getContentTest() throws Exception {
        RawBytesRecord r = new RawBytesRecord(SIMPLE_NON_ASCII_STRING.getBytes("iso-8859-15"), "key");
        
        String res = new CacheController().getContent(r, "iso-8859-15");
        
        Assert.assertEquals(SIMPLE_NON_ASCII_STRING, res);
    }
    
    @Test
    public void getContentTestBadCharset() throws Exception {
        RawBytesRecord r = new RawBytesRecord(NON_ASCII_STRING.getBytes(), "key"); //Intentional use of default charset
        
        String res = new CacheController().getContent(r, "badcharset");
        
        Assert.assertEquals("When a bad charset is detected we should use the default charset", NON_ASCII_STRING, res);
    }
    
    
    @Test
    public void cacheTestComplexCharset() throws Exception {
        CacheQuestion question = mock(CacheQuestion.class);
        when(question.getUrl()).thenReturn("http://coal.ila/");
        
        Config config = mock(Config.class);
        when(config.valueAsBoolean(Keys.UI_CACHE_DISABLED)).thenReturn(false);
        Collection collection = mock(Collection.class);
        when(collection.getConfiguration()).thenReturn(config);
        when(question.getCollection()).thenReturn(collection);
        
        DataRepository dataRepository = mock(DataRepository.class);
        
        Map<String, String> m = new HashMap<>();
        m.put("content-type", "charset=\"iso-8859-15\"");
        
        RawBytesRecord r = new RawBytesRecord(SIMPLE_NON_ASCII_STRING.getBytes("iso-8859-15"), "key");
        RecordAndMetadata<RawBytesRecord> ram = new RecordAndMetadata<RawBytesRecord>(r, m);
        
        when(dataRepository.getCachedDocument(collection, StoreView.live, question.getUrl())).thenReturn((RecordAndMetadata) ram);
        
        MetricRegistry metricRegistry = mock(MetricRegistry.class);
        when(metricRegistry.counter(any())).thenReturn(mock(Counter.class));
        
        CacheController cacheController = new CacheController();
        cacheController.setDataRepository(dataRepository);
        cacheController.setMetrics(metricRegistry);
        
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://ron/"));
        ModelAndView modelAndView = cacheController.cache(httpServletRequest, null, question);
        
        Document doc = (Document) modelAndView.getModelMap().get(CacheController.MODEL_DOCUMENT);
        
        Assert.assertTrue("Expected to find: '" + SIMPLE_NON_ASCII_STRING +"' yet found: \r\n:" + doc.text(), (doc.text()).contains(SIMPLE_NON_ASCII_STRING));
        
    }
    
    
}
