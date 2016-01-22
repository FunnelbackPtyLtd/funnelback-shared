package com.funnelback.publicui.test.search.web.controllers;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.io.store.Store;
import com.funnelback.publicui.search.web.controllers.CacheController;

public class CacheControllerTest {

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
}
