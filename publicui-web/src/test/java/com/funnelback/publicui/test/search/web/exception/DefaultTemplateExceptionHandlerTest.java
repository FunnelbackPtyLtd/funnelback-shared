package com.funnelback.publicui.test.search.web.exception;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.publicui.search.model.collection.Profile;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.web.exception.DefaultTemplateExceptionHandler;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DefaultTemplateExceptionHandlerTest {

    @Autowired
    private DefaultTemplateExceptionHandler handler;
    
    private Environment env;
    private TemplateModel model;
    private Config config;
    private StringWriter out;
    @Before
    public void before() throws IOException {
        
        config = new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), "dummy");

        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.FREEMARKER.DISPLAY_ERRORS, true);

        Profile profile = new Profile("_default");
        profile.setServiceConfig(serviceConfig);
        Collection collection = new Collection("dummy", config);
        collection.getProfiles().put("_default", profile);

        SearchQuestion sq = new SearchQuestion();
        sq.setCurrentProfile("_default");
        sq.setCollection(collection);
        SearchTransaction st = new SearchTransaction(sq, null);
        model = new BeanModel(st, new DefaultObjectWrapper());
        
        out = new StringWriter();
        env = new Environment(new Template("", new StringReader(""), null), (TemplateHashModel) model, out);


    }
    
    @Test
    public void testNoSearchQuestion() {
        model = new SimpleHash();
        try {
            handler.handleTemplateException(new TemplateException("TPL_ERROR", env), env, out);
            Assert.fail();
        } catch (TemplateException te) { }        
    }
    
    @Test
    public void testDefaultConfig() {
        try {
            handler.handleTemplateException(new TemplateException("TPL_ERROR", env), env, out);
            Assert.fail();
        } catch (TemplateException te) { }        

        Assert.assertFalse(out.getBuffer().toString().equals(""));    
    }

    @Test
    public void testInvalidFormat() {
        config.setValue(Keys.ModernUI.FREEMARKER_ERROR_FORMAT, "invalid");
        try {
            handler.handleTemplateException(new TemplateException("TPL_ERROR", env), env, out);
            Assert.fail();
        } catch (TemplateException te) {
            Assert.assertEquals(te.getCause().getClass(), IllegalArgumentException.class);
        }        

        Assert.assertTrue(out.getBuffer().toString().equals(""));
    }
    
    @Test
    public void testHtml() throws TemplateException {
        config.setValue(Keys.ModernUI.FREEMARKER_ERROR_FORMAT, "html");
        handler.handleTemplateException(new TemplateException("TPL_ERROR", env), env, out);
        
        Assert.assertFalse(out.getBuffer().toString().equals(""));
        Assert.assertTrue(out.getBuffer().toString().startsWith("<!--"));
        Assert.assertTrue(out.getBuffer().toString().endsWith("-->\n"));
        Assert.assertTrue(out.getBuffer().toString().contains("TPL_ERROR"));
    }

    @Test
    public void testJson() throws TemplateException {
        config.setValue(Keys.ModernUI.FREEMARKER_ERROR_FORMAT, "json");
        handler.handleTemplateException(new TemplateException("TPL_ERROR", env), env, out);
        
        Assert.assertFalse(out.getBuffer().toString().equals(""));
        Assert.assertTrue(out.getBuffer().toString().startsWith("/*"));
        Assert.assertTrue(out.getBuffer().toString().endsWith("*/\n"));
        Assert.assertTrue(out.getBuffer().toString().contains("TPL_ERROR"));
    }

    @Test
    public void testString() throws TemplateException {
        config.setValue(Keys.ModernUI.FREEMARKER_ERROR_FORMAT, "json");
        handler.handleTemplateException(new TemplateException("TPL_ERROR", env), env, out);
        
        Assert.assertFalse(out.getBuffer().toString().equals(""));
        Assert.assertTrue(out.getBuffer().toString().contains("TPL_ERROR"));
    }
}
