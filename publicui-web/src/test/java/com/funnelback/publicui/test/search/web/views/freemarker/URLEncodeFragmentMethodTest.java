package com.funnelback.publicui.test.search.web.views.freemarker;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.URLEncodeFragmentMethod;

import freemarker.template.TemplateModelException;

public class URLEncodeFragmentMethodTest extends AbstractMethodTest {

    private static final String[] INPUT = {
        "a test",
        "a#test",
        "http://server.com/folder/page.html?query=value#fragment-part"
    };
    
    private static final String[] EXPECTED = {
        "a%20test",
        "a%23test",
        "http://server.com/folder/page.html?query=value%23fragment-part"
    };
    
    @Test
    public void test() throws TemplateModelException {
        for (int i=0; i<INPUT.length;i++) {
            Assert.assertEquals(
                    EXPECTED[i],
                    method.exec(buildStringArguments(INPUT[i])));
        }        
    }
    
    @Override
    protected AbstractTemplateMethod buildMethod() {
        return new URLEncodeFragmentMethod();
    }

    @Override
    protected int getRequiredArgumentsCount() {
        return 1;
    }

    @Override
    protected int getOptionalArgumentsCount() {
        return 0;
    }

}
