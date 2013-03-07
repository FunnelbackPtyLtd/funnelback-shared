package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.TagifyMethod;

import freemarker.ext.beans.BooleanModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class TagifyMethodTests extends AbstractMethodTest {

    @Test
    public void testNoMatchingWords() throws TemplateModelException {
        List<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
        arguments.add(new SimpleScalar("tag"));
        arguments.add(new SimpleScalar("word to boldicize"));
        arguments.add(new SimpleScalar("there is no matching words"));
        
        SimpleScalar result = (SimpleScalar) method.exec(arguments);
        
        Assert.assertEquals("there is no matching words", result.getAsString());        
    }
    
    @Test
    public void testOperators() throws TemplateModelException {
        List<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
        arguments.add(new SimpleScalar("tag"));
        arguments.add(new SimpleScalar("|word [to]"));
        arguments.add(new SimpleScalar("there is a 'word' that we have to boldicize"));
        
        SimpleScalar result = (SimpleScalar) method.exec(arguments);
        
        Assert.assertEquals("there is a '<tag>word</tag>' that we have <tag>to</tag> boldicize", result.getAsString());        
    }
    
    @Test
    public void test() throws TemplateModelException {
        List<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
        arguments.add(new SimpleScalar("tag"));
        arguments.add(new SimpleScalar("word to boldicize"));
        arguments.add(new SimpleScalar("this is the sentence to Boldicize"));
        
        SimpleScalar result = (SimpleScalar) method.exec(arguments);
        
        Assert.assertEquals("this is the sentence <tag>to</tag> <tag>Boldicize</tag>", result.getAsString());
    }
    
    @Test
    public void testRegExp() throws TemplateModelException {
        List<TemplateModel> arguments = new ArrayList<TemplateModel>();
        arguments.add(new SimpleScalar("tag"));
        arguments.add(new SimpleScalar("(?i)\\bword\\b|\\bto\\b|\\bbe\\b"));
        arguments.add(new SimpleScalar("This is the Word that should BE boldicized"));
        arguments.add(new BooleanModel(true, new DefaultObjectWrapper()));

        SimpleScalar result = (SimpleScalar) method.exec(arguments);
        
        Assert.assertEquals("This is the <tag>Word</tag> that should <tag>BE</tag> boldicized", result.getAsString());
    }
    
    @Test
    public void testAccents() throws TemplateModelException {
        List<TemplateModel> arguments = new ArrayList<TemplateModel>();
        arguments.add(new SimpleScalar("tag"));
        arguments.add(new SimpleScalar("(?i)école|écoles|\\bhaute\\b|\\bhautes\\b|étude|études"));
        arguments.add(new SimpleScalar("école des hautes études"));
        
        arguments.add(new BooleanModel(true, new DefaultObjectWrapper()));

        SimpleScalar result = (SimpleScalar) method.exec(arguments);
        
        Assert.assertEquals("<tag>école</tag> des <tag>hautes</tag> <tag>étude</tag>s", result.getAsString());

    }

    @Override
    protected AbstractTemplateMethod buildMethod() {
        return new TagifyMethod();
    }

    @Override
    protected int getRequiredArgumentsCount() {        
        return 3;
    }

    @Override
    protected int getOptionalArgumentsCount() {
        return 1;
    }
    
}
