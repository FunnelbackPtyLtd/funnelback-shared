package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.FormatMethod;

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BooleanModel;
import freemarker.ext.beans.DateModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FormatMethodTest extends AbstractMethodTest {

    @Override
    protected AbstractTemplateMethod buildMethod() {
        return new FormatMethod();
    }

    @Override
    protected int getRequiredArgumentsCount() {
        return 2;
    }

    @Override
    protected int getOptionalArgumentsCount() {
        return Integer.MAX_VALUE;
    }

    @Test
    public void testNoArguments() throws TemplateModelException {
        ArrayList<TemplateModel> arguments = new ArrayList<TemplateModel>();
        arguments.add(new BeanModel(Locale.getDefault(), new DefaultObjectWrapper()));
        arguments.add(new SimpleScalar("Format this !"));
        Assert.assertEquals("Format this !", method.exec(arguments));
    }
    
    @Test
    public void testArguments() throws TemplateModelException {
        DefaultObjectWrapper wrapper = new DefaultObjectWrapper();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2012);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        
        ArrayList<TemplateModel> arguments = new ArrayList<TemplateModel>();
        arguments.add(new BeanModel(new Locale("en", "GB"), new DefaultObjectWrapper()));
        arguments.add(new SimpleScalar("String: %s, Number: %x, Boolean: %b, Date: %4$tb %4$tB"));
        arguments.add(buildSequenceArguments(
                new SimpleScalar("Hello"),
                new SimpleNumber(255),
                new BooleanModel(true, wrapper),
                new DateModel(cal.getTime(), wrapper)));
        Assert.assertEquals("String: Hello, Number: ff, Boolean: true, Date: Jan January", method.exec(arguments));    
    }
    
    @Test
    public void testOneArgument() throws TemplateModelException {
        ArrayList<TemplateModel> arguments = new ArrayList<TemplateModel>();
        arguments.add(new BeanModel(new Locale("en", "GB"), new DefaultObjectWrapper()));
        arguments.add(new SimpleScalar("%d argument"));
        arguments.add(new SimpleNumber(1));
        Assert.assertEquals("1 argument", method.exec(arguments));
    }
    
}
