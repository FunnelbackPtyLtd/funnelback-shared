package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.CurrentDateMethod;

import freemarker.template.TemplateModelException;

public class CurrentDateMethodTests extends AbstractMethodTest {

    @Test
    public void test() throws TemplateModelException {
        Date now = new Date();
        Date d = (Date) method.exec(null);
        
        Assert.assertNotNull(d);
        // Should have run under 1s
        Assert.assertTrue(d.getTime() >= now.getTime() && d.getTime() < now.getTime()+1000);
    }
    
    @Override
    protected AbstractTemplateMethod buildMethod() {
        return new CurrentDateMethod();
    }

    @Override
    protected int getRequiredArgumentsCount() {
        return 0;
    }

    @Override
    protected int getOptionalArgumentsCount() {
        return 0;
    }

}
