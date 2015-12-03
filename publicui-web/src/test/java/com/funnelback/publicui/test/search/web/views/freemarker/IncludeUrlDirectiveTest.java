package com.funnelback.publicui.test.search.web.views.freemarker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.IncludeUrlDirective;

import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class IncludeUrlDirectiveTest extends IncludeUrlDirective {

    public IncludeUrlDirectiveTest() {
        super(null, null);
    }

    @Test
    public void testConvertRelative() throws TemplateModelException, IOException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.convertRelative.toString(), TemplateBooleanModel.TRUE);
        
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
        
        // FUN-5164 again
        actual = this.transformContent("http://server.com/folder/file.html", "<div class=\"hgroup\"> <a href=\"index.html\"><img class=\"bga\" src=\"images/logo.png\" alt=\"Business.gov.au\" /></a><p><a href=\"index.html\">Home</a> <span>»</span> <a href=\"advisor-finder-index.html\">Advisor finder</a></p></div><!-- hgroup -->", params);
        Assert.assertEquals(
            "<div class=\"hgroup\"> <a href=\"http://server.com/folder/index.html\"><img class=\"bga\" src=\"http://server.com/folder/images/logo.png\" alt=\"Business.gov.au\" /></a><p><a href=\"http://server.com/folder/index.html\">Home</a> <span>»</span> <a href=\"http://server.com/folder/advisor-finder-index.html\">Advisor finder</a></p></div><!-- hgroup -->",
            actual);
    }

    @Test
    public void testConvertRelativeAlternate() throws TemplateModelException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.convertrelative.toString(), TemplateBooleanModel.TRUE);
        
        String actual = this.transformContent("http://server.com/folder/file.html", "<a href='test.html'>Link</a>", params);
        Assert.assertEquals(
            "<a href=\"http://server.com/folder/test.html\">Link</a>",
            actual);        
    }
    
    @Test
    public void testTransformInvalidUrls() throws TemplateModelException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.convertRelative.toString(), TemplateBooleanModel.TRUE);
        
        String actual = this.transformContent("http://server.com/folder/file.html", "<a href='invalid with spaces.html'>Link</a> <a href='valid.html'>Other</a>", params);
        Assert.assertEquals(
            "<a href=\"invalid with spaces.html\">Link</a> <a href=\"http://server.com/folder/valid.html\">Other</a>",
            actual);        
    }
    
}
