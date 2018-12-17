package com.funnelback.publicui.test.search.web.views.freemarker;

import static com.funnelback.publicui.test.search.web.views.freemarker.GetFacetsMethodTest.simpleSequenceOf;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import com.funnelback.publicui.search.web.views.freemarker.IncludeUrlDirective;

import freemarker.template.SimpleScalar;
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
    
    
    @Test
    public void testSelectByCssSelector() throws TemplateModelException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.cssSelector.toString(), new SimpleScalar("#b"));
        
        String actual = this.transformContent("http://server.com/folder/file.html",
            "<html>"
            + "<div id='a'><p>nope</p></div>"
            + "<div id='b'><p>yep</p></div>"
            + "</html>",
            params);
        
        
        Assert.assertEquals("<div id=\"b\"> <p>yep</p></div>",
            actual.replace("\n", "").replace("\r", ""));        
    }
    
    @Test
    public void testSelectByCssSelector_bad_selector() throws TemplateModelException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.cssSelector.toString(), new SimpleScalar("%^&*&^%$#$%^&^"));
        
        String content = "<html>"
            + "<div id='a'><p>nope</p></div>"
            + "<div id='b'><p>yep</p></div>"
            + "</html>";
        String actual = this.transformContent("http://server.com/folder/file.html",
            content,
            params);
        
        
        Assert.assertEquals(content, actual.replace("\n", ""));        
    }
    
    @Test
    public void testSelectByCssSelector_matches_nothing() throws TemplateModelException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.cssSelector.toString(), new SimpleScalar("#doesnotexist"));
        
        String content = "<html>"
            + "<div id='a'><p>nope</p></div>"
            + "<div id='b'><p>yep</p></div>"
            + "</html>";
        String actual = this.transformContent("http://server.com/folder/file.html",
            content,
            params);
        
        
        Assert.assertEquals("", "");        
    }
    
    @Test
    public void testRemoveBySelector() throws TemplateModelException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.removeByCssSelectors.toString(), 
            simpleSequenceOf("$%^$^%^badselectortoignore", "#a", "#b"));
        
        String actual = this.transformContent("http://server.com/folder/file.html",
            "<html>"
            + "<head></head>"
            + "<body>"
            + "<div id='important'>"
            + "<div id='a'><p>nope</p>"
            + "<div id='b'><p>yep</p></div>"
            + "</div>"
            + "<p>foo</p>"
            + "</div>"
            + "</body>"
            + "</html>",
            params);
        
        Assert.assertEquals("<html>"
                 + " <head></head>"
                 + " <body>"
                 + "  <div id=\"important\">"
                 + "   <p>foo</p>"
                 + "  </div>"
                 + " </body>"
                 + "</html>",
            actual.replace("\n", "").replace("\r", ""));        
    }
    
    @Test
    public void testSelectThenRemove() throws TemplateModelException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.cssSelector.toString(), new SimpleScalar("#important"));
        params.put(Parameters.removeByCssSelectors.toString(), 
            simpleSequenceOf("#a", "#b"));
        
        String actual = this.transformContent("http://server.com/folder/file.html",
            "<html>"
            + "<head></head>"
            + "<body>"
            + "<div id='important'>"
            + "<div id='a'><p>nope</p>"
            + "<div id='b'><p>yep</p></div>"
            + "</div>"
            + "<p>foo</p>"
            + "</div>"
            + "</body>"
            + "</html>",
            params);
        
        Assert.assertEquals(
                 "<div id=\"important\">"
                 + " <p>foo</p>"
                 + "</div>",
            actual.replace("\n", "").replace("\r", ""));        
    }
    
    @Test
    public void test_remove_selected_element() throws TemplateModelException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.cssSelector.toString(), new SimpleScalar("#important"));
        params.put(Parameters.removeByCssSelectors.toString(), 
            simpleSequenceOf("#important"));
        
        String actual = this.transformContent("http://server.com/folder/file.html",
            "<html>"
            + "<head></head>"
            + "<body>"
            + "<div id='important'>"
            + "<p>a</p>"
            + "</div>"
            + "</body>"
            + "</html>",
            params);
        
        // I guess it is impossible to remove the selected element
        // perhaos that makes sense. This behaviour shall be documented.
        Assert.assertEquals(
                 "<div id=\"important\"> <p>a</p></div>",
            actual.replace("\n", "").replace("\r", ""));        
    }
    
    @Test
    public void test_removeByCssSelectors_is_set_empty() throws TemplateModelException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.cssSelector.toString(), new SimpleScalar("#important"));
        params.put(Parameters.removeByCssSelectors.toString(), 
            simpleSequenceOf());
        
        String actual = this.transformContent("http://server.com/folder/file.html",
            "<html>"
            + "<head></head>"
            + "<body>"
            + "<div id='important'>"
            + "<p>a</p>"
            + "</div>"
            + "</body>"
            + "</html>",
            params);
        
        // I guess it is impossible to remove the selected element
        // perhaos that makes sense. This behaviour shall be documented.
        Assert.assertEquals(
                 "<div id=\"important\"> <p>a</p></div>",
            actual.replace("\n", "").replace("\r", ""));        
    }
    
    @Test
    public void testRemoveCssSelectorIsRelativeToSelectedElement() throws TemplateModelException {
        Map<String, TemplateModel> params = new HashMap<>();
        params.put(Parameters.cssSelector.toString(), new SimpleScalar("#important"));
        params.put(Parameters.removeByCssSelectors.toString(), 
            simpleSequenceOf("p:nth-of-type(1)"));
        
        String actual = this.transformContent("http://server.com/folder/file.html",
            "<html>"
            + "<head></head>"
            + "<body>"
            + "<p>a</p>"
            + "<div id='important'>"
            + "<p>b</p>"
            + "<p>c</p>"
            + "</div>"
            + "</body>"
            + "</html>",
            params);
        
        Assert.assertEquals(
                 "<div id=\"important\"> <p>c</p></div>",
            actual.replace("\n", "").replace("\r", ""));        
    }
}
