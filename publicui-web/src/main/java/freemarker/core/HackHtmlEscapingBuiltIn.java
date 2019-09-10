package freemarker.core;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.utility.StringUtil;

/**
 * Hack freemarker's built-in html and xhtml escaping functions
 * so that they will break any AngularJS interpolation markers
 * being added to the page.
 *
 * The breaking occurs by inserting a zero-width space character
 * between any consecutive open-curly-bracket characters which
 * seemingly prevents AngularJS from processing them.
 *
 * See RNDSUPPORT-3041 for details.
 */
public class HackHtmlEscapingBuiltIn {

    // Called from src/main/webapp/WEB-INF/spring/freemarker.xml
    public static void hackIt() {
        BuiltIn.BUILT_INS_BY_NAME.put("html", new HackedHtmlEscapingBuiltIn());
        BuiltIn.BUILT_INS_BY_NAME.put("xhtml", new HackedXhtmlEscapingBuiltIn());
    }

    public static void unHackIt() {
        BuiltIn.BUILT_INS_BY_NAME.put("html", new BuiltInsForStringsEncoding.htmlBI());
        BuiltIn.BUILT_INS_BY_NAME.put("xhtml", new BuiltInsForStringsEncoding.xhtmlBI());
    }

    public static String breakAngularInterpolation(String result) {
        // Insert a zero-width space between any pair of curly-brackets
        // to prevent angular xss (RNDSUPPORT-3041)
        return result.replace("{{", "{&#8203;{");
    }

    private static class HackedHtmlEscapingBuiltIn extends BuiltInForLegacyEscaping {
        @Override TemplateModel calculateResult(String s, Environment env) throws TemplateException {
            String result = StringUtil.HTMLEnc(s);
            result = breakAngularInterpolation(result);
            return new SimpleScalar(result);
        }
    }

    static class HackedXhtmlEscapingBuiltIn extends BuiltInForLegacyEscaping {
        @Override
        TemplateModel calculateResult(String s, Environment env) {
            String result = StringUtil.XHTMLEnc(s);
            result = breakAngularInterpolation(result);
            return new SimpleScalar(result);
        }
    }

}
