package com.funnelback.publicui.test.search.web.views.freemarker;

import org.junit.Assert;

import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.HTMLDecodeMethod;

import freemarker.template.TemplateModelException;

public class HTMLDecodeMethodTests extends AbstractMethodTest {

    private static final String[] INPUT = {
        "a test",
        "a &lt;test&gt;",
        "test &amp; test",
        "test & test"
    };
    
    private static final String[] EXPECTED = {
        "a test",
        "a <test>",
        "test & test",
        "test & test"
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
        return new HTMLDecodeMethod();
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
