package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.ocpsoft.prettytime.PrettyTime;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.PrettyTimeMethod;

import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleObjectWrapper;
import freemarker.template.TemplateModelException;

public class PrettyTimeMethodTest extends AbstractMethodTest {

    @Override
    protected AbstractTemplateMethod buildMethod() {
        return new PrettyTimeMethod();
    }

    @Override
    protected int getRequiredArgumentsCount() {
        return 1;
    }

    @Override
    protected int getOptionalArgumentsCount() {
        return 1;
    }
    
    @Test
    public void test() throws TemplateModelException {
        Date d = new Date();
        Assert.assertEquals(
            new PrettyTime(Locale.getDefault()).format(d),
            method.exec(buildArguments(new SimpleDate(d, 0))));
    }
    
    @Test
    public void testLocale() throws TemplateModelException {
        Date d = new Date();
        Assert.assertEquals(
            new PrettyTime(Locale.GERMAN).format(d),
            method.exec(buildArguments(new SimpleDate(d, 0), new BeanModel(Locale.GERMAN, new SimpleObjectWrapper()))));
    }


}
