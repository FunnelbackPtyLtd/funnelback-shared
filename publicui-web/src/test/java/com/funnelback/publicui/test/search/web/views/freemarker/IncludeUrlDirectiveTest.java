package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.web.views.freemarker.IncludeUrlDirective;

import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class IncludeUrlDirectiveTest extends IncludeUrlDirective {

    public IncludeUrlDirectiveTest() {
        super(null, null);
    }

    @Test
    public void testConvertRelative() throws TemplateModelException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.convertrelative.toString(), TemplateBooleanModel.TRUE);
        
        String actual = this.transformContent("http://server.com/folder/file.html", "<a href='test.html'>Link</a>", params);
        Assert.assertEquals(
            "<a href=\"http://server.com/folder/test.html\">Link</a>",
            actual);

        actual = this.transformContent("http://server.com/folder/file.html", "<img src='images/img.jpg' />", params);
        Assert.assertEquals(
            "<img src=\"http://server.com/folder/images/img.jpg\" />",
            actual);

        actual = this.transformContent("http://server.com/folder/file.html", "<a href='/home.html'><img src='http://my-site.com/images/img.jpg' /></a>", params);
        Assert.assertEquals(
            "<a href=\"http://server.com/home.html\"><img src=\"http://my-site.com/images/img.jpg\" /></a>",
            actual);

        actual = this.transformContent("http://server.com/folder/file.html", "<a href='http://server2.com/test.html'>Link</a>", params);
        Assert.assertEquals(
            "<a href=\"http://server2.com/test.html\">Link</a>",
            actual);
        
        actual = this.transformContent("http://server.com/folder/file.html", "<a href='/test.html'>Link</a>", params);
        Assert.assertEquals(
            "<a href=\"http://server.com/test.html\">Link</a>",
            actual);
        
        actual = this.transformContent("http://server.com/folder/file.html", "<!-- a href='/test.html'>Link</a -->", params);
        Assert.assertEquals(
            "<!-- a href='/test.html'>Link</a -->",
            actual);

        // FUN-5164
        actual = this.transformContent("http://server.com/folder/file.html", "var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;", params);
        Assert.assertEquals(
            "var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;",
            actual);

    }
}