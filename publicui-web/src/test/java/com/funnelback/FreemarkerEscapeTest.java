package com.funnelback;

import com.funnelback.publicui.search.web.views.freemarker.escaping.HTMLPlusAngularOutputFormat;
import com.funnelback.publicui.search.web.views.freemarker.escaping.XHTMLPlusAngularOutputFormat;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.CommonMarkupOutputFormat;
import freemarker.core.CommonTemplateMarkupOutputModel;
import freemarker.core.HTMLOutputFormat;
import freemarker.core.HackHtmlEscapingBuiltIn;
import freemarker.core.OutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreemarkerEscapeTest {

    @Test
    public void testOldEscaping() throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setDefaultEncoding("UTF-8");

        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("test.ftl", "This is a test ${user?html}");
        cfg.setTemplateLoader(templateLoader);

        HackHtmlEscapingBuiltIn.hackIt();

        Map<String, Object> root = new HashMap<>();
        root.put("user", "Matt{{}}<>");

        Template temp = cfg.getTemplate("test.ftl");

        StringWriter out = new StringWriter();
        HackHtmlEscapingBuiltIn.hackIt();
        temp.process(root, out);
        HackHtmlEscapingBuiltIn.unHackIt();

        Assert.assertEquals("This is a test Matt{&#8203;{}}&lt;&gt;", out.toString());
    }

    @Test
    public void testNewHTMLEscaping() throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setDefaultEncoding("UTF-8");

        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("test.ftl",
            "<#ftl output_format=\"HTML\" encoding=\"utf-8\" />\n"
            + "This is a test ${user}");
        cfg.setTemplateLoader(templateLoader);

        List<OutputFormat> outputFormats = new ArrayList<>();
        outputFormats.add(new HTMLPlusAngularOutputFormat());
        cfg.setRegisteredCustomOutputFormats(outputFormats);

        Map<String, Object> root = new HashMap<>();
        root.put("user", "Matt{{}}<>");

        Template temp = cfg.getTemplate("test.ftl");

        StringWriter out = new StringWriter();
        temp.process(root, out);

        Assert.assertEquals("This is a test Matt{&#8203;{}}&lt;&gt;", out.toString());
    }

    @Test
    public void testNewXHTMLEscaping() throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setDefaultEncoding("UTF-8");

        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("test.ftl",
            "<#ftl output_format=\"XHTML\" encoding=\"utf-8\" />\n"
                + "This is a test ${user}");
        cfg.setTemplateLoader(templateLoader);

        List<OutputFormat> outputFormats = new ArrayList<>();
        outputFormats.add(new XHTMLPlusAngularOutputFormat());
        cfg.setRegisteredCustomOutputFormats(outputFormats);

        Map<String, Object> root = new HashMap<>();
        root.put("user", "Matt{{}}<>");

        Template temp = cfg.getTemplate("test.ftl");

        StringWriter out = new StringWriter();
        temp.process(root, out);

        Assert.assertEquals("This is a test Matt{&#8203;{}}&lt;&gt;", out.toString());
    }

}
