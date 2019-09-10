package com.funnelback.publicui.search.web.views.freemarker.escaping;

import freemarker.core.CommonMarkupOutputFormat;
import freemarker.core.HackHtmlEscapingBuiltIn;
import freemarker.core.XHTMLOutputFormat;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class XHTMLPlusAngularOutputFormat extends CommonMarkupOutputFormat<TemplateXHTMLPlusAngularOutputModel> {
    public static final XHTMLPlusAngularOutputFormat INSTANCE = new XHTMLPlusAngularOutputFormat();

    @Override public void output(String textToEsc, Writer out) throws IOException, TemplateModelException {
        StringWriter sw = new StringWriter();
        XHTMLOutputFormat.INSTANCE.output(textToEsc, sw);
        out.write(HackHtmlEscapingBuiltIn.breakAngularInterpolation(sw.getBuffer().toString()));
    }

    @Override public String escapePlainText(String plainTextContent) throws TemplateModelException {
        String result = XHTMLOutputFormat.INSTANCE.escapePlainText(plainTextContent);
        result = HackHtmlEscapingBuiltIn.breakAngularInterpolation(result);
        return result;
    }

    @Override public boolean isLegacyBuiltInBypassed(String builtInName) throws TemplateModelException {
        return XHTMLOutputFormat.INSTANCE.isLegacyBuiltInBypassed(builtInName);
    }

    @Override protected TemplateXHTMLPlusAngularOutputModel newTemplateMarkupOutputModel(String plainTextContent, String markupContent)
        throws TemplateModelException {
        return new TemplateXHTMLPlusAngularOutputModel(plainTextContent, markupContent);
    }

    @Override
    public String getName() {
        return "XHTML";
    }

    @Override
    public String getMimeType() {
        return "application/xhtml+xml";
    }

}