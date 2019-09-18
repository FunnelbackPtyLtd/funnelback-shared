package com.funnelback.publicui.search.web.views.freemarker.escaping;

import freemarker.core.CommonMarkupOutputFormat;
import freemarker.core.HackHtmlEscapingBuiltIn;
import freemarker.core.XHTMLOutputFormat;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Supersede freemarker's built-in xhtml output format
 * so that it will break any AngularJS interpolation markers
 * being added to the page.
 *
 * The breaking occurs by inserting a zero-width space character
 * between any consecutive open-curly-bracket characters which
 * seemingly prevents AngularJS from processing them.
 *
 * See RNDSUPPORT-3041 for details.
 */
public class XHTMLPlusAngularOutputFormat extends CommonMarkupOutputFormat<TemplateXHTMLPlusAngularOutputModel> {
    public static final XHTMLPlusAngularOutputFormat INSTANCE = new XHTMLPlusAngularOutputFormat();

    // Nothing outside this class should construct an instance because freemarker relies on
    // them being singletons for some reason. See RNDSUPPORT-3041
    private XHTMLPlusAngularOutputFormat() {}

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