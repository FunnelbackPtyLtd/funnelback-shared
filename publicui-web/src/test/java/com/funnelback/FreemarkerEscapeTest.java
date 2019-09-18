package com.funnelback;

import com.funnelback.publicui.search.web.views.freemarker.escaping.HTMLPlusAngularOutputFormat;
import com.funnelback.publicui.search.web.views.freemarker.escaping.XHTMLPlusAngularOutputFormat;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.HackHtmlEscapingBuiltIn;
import freemarker.core.OutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
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
                + "This is a test ${user}\n"
                + "This has no escaping ${user?no_esc}\n"
                + "This has another no escaping <#noautoesc>${user}</#noautoesc>\n"
                + "This is different output format <#outputformat \"JavaScript\">${user}</#outputformat>");
        cfg.setTemplateLoader(templateLoader);

        List<OutputFormat> outputFormats = new ArrayList<>();
        outputFormats.add(HTMLPlusAngularOutputFormat.INSTANCE);
        cfg.setRegisteredCustomOutputFormats(outputFormats);

        Map<String, Object> root = new HashMap<>();
        root.put("user", "Matt{{}}<>");

        Template temp = cfg.getTemplate("test.ftl");

        StringWriter out = new StringWriter();
        temp.process(root, out);

        Assert.assertEquals("This is a test Matt{&#8203;{}}&lt;&gt;\n"
            + "This has no escaping Matt{{}}<>\n"
            + "This has another no escaping Matt{{}}<>\n"
            + "This is different output format Matt{{}}<>", out.toString());
    }

    @Test
    public void testNewXHTMLEscaping() throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setDefaultEncoding("UTF-8");

        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("test.ftl",
            "<#ftl output_format=\"XHTML\" encoding=\"utf-8\" />\n"
                + "This is a test ${user}\n"
                + "This has no escaping ${user?no_esc}\n"
                + "This has another no escaping <#noautoesc>${user}</#noautoesc>\n"
                + "This is different output format <#outputformat \"JavaScript\">${user}</#outputformat>");
        cfg.setTemplateLoader(templateLoader);

        List<OutputFormat> outputFormats = new ArrayList<>();
        outputFormats.add(XHTMLPlusAngularOutputFormat.INSTANCE);
        cfg.setRegisteredCustomOutputFormats(outputFormats);

        Map<String, Object> root = new HashMap<>();
        root.put("user", "Matt{{}}<>");

        Template temp = cfg.getTemplate("test.ftl");

        StringWriter out = new StringWriter();
        temp.process(root, out);

        Assert.assertEquals("This is a test Matt{&#8203;{}}&lt;&gt;\n"
            + "This has no escaping Matt{{}}<>\n"
            + "This has another no escaping Matt{{}}<>\n"
            + "This is different output format Matt{{}}<>", out.toString());
    }
}
