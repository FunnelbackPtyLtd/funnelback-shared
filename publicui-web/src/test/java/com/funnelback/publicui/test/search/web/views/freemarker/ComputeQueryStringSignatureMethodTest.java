package com.funnelback.publicui.test.search.web.views.freemarker;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.ComputeQueryStringSignatureMethod;
import com.funnelback.publicui.utils.URLSignature;

import freemarker.template.TemplateModelException;

public class ComputeQueryStringSignatureMethodTest extends AbstractMethodTest {

    private static final String[] DATA = {
        "&param1=value1&param2=value2",
        "?param+1=value%201&param+2=value%202"
    };
    
    @Override
    protected AbstractTemplateMethod buildMethod() {
        return new ComputeQueryStringSignatureMethod();
    }

    @Override
    protected int getRequiredArgumentsCount() {
        return 1;
    }

    @Override
    protected int getOptionalArgumentsCount() {
        return 0;
    }
    
    @Test
    public void test() throws TemplateModelException {
        for (String data: DATA) {
            Assert.assertEquals(
                URLSignature.computeQueryStringSignature(data),
                method.exec(buildStringArguments(data)));
        }
    }

}
