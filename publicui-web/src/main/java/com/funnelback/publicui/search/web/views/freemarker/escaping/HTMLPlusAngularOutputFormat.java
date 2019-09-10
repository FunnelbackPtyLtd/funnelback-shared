package com.funnelback.publicui.search.web.views.freemarker.escaping;

import freemarker.core.CommonMarkupOutputFormat;
import freemarker.core.HTMLOutputFormat;
import freemarker.core.HackHtmlEscapingBuiltIn;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Supersede freemarker's built-in html output format
 * so that it will break any AngularJS interpolation markers
 * being added to the page.
 *
 * The breaking occurs by inserting a zero-width space character
 * between any consecutive open-curly-bracket characters which
 * seemingly prevents AngularJS from processing them.
 *
 * See RNDSUPPORT-3041 for details.
 */
public class HTMLPlusAngularOutputFormat extends CommonMarkupOutputFormat<TemplateHTMLPlusAngularOutputModel> {
    public static final HTMLPlusAngularOutputFormat INSTANCE = new HTMLPlusAngularOutputFormat();

    @Override public void output(String textToEsc, Writer out) throws IOException, TemplateModelException {
        StringWriter sw = new StringWriter();
        HTMLOutputFormat.INSTANCE.output(textToEsc, sw);
        out.write(HackHtmlEscapingBuiltIn.breakAngularInterpolation(sw.getBuffer().toString()));
    }

    @Override public String escapePlainText(String plainTextContent) throws TemplateModelException {
        String result = HTMLOutputFormat.INSTANCE.escapePlainText(plainTextContent);
        result = HackHtmlEscapingBuiltIn.breakAngularInterpolation(result);
        return result;
    }

    @Override public boolean isLegacyBuiltInBypassed(String builtInName) throws TemplateModelException {
        return HTMLOutputFormat.INSTANCE.isLegacyBuiltInBypassed(builtInName);
    }

    @Override protected TemplateHTMLPlusAngularOutputModel newTemplateMarkupOutputModel(String plainTextContent, String markupContent)
        throws TemplateModelException {
        return new TemplateHTMLPlusAngularOutputModel(plainTextContent, markupContent);
    }

    @Override
    public String getName() {
        return "HTML";
    }

    @Override
    public String getMimeType() {
        return "text/html";
    }

}