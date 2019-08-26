package freemarker.core;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template._TemplateAPI;
import freemarker.template.utility.StringUtil;

public class HackHtmlEscapingBuiltIn {

    public static void hackIt() {
        BuiltIn.BUILT_INS_BY_NAME.put("html", new HackedHtmlEscapingBuiltIn());
        BuiltIn.BUILT_INS_BY_NAME.put("xhtml", new HackedXhtmlEscapingBuiltIn());
    }

    private static String htmlEncodeCurlyBrackets(String result) {
        // Escape curly-brackets for angular xss (RNDSUPPORT-3041)
        result = result.replace("{", "&#123;");
        result = result.replace("}", "&#125;");
        return result;
    }

    private static class HackedHtmlEscapingBuiltIn extends BuiltInForLegacyEscaping {
        @Override TemplateModel calculateResult(String s, Environment env) throws TemplateException {
            String result = StringUtil.HTMLEnc(s);
            result = htmlEncodeCurlyBrackets(result);
            return new SimpleScalar(result);
        }
    }

    static class HackedXhtmlEscapingBuiltIn extends BuiltInForLegacyEscaping {
        @Override
        TemplateModel calculateResult(String s, Environment env) {
            String result = StringUtil.XHTMLEnc(s);
            result = htmlEncodeCurlyBrackets(result);
            return new SimpleScalar(result);
        }
    }

}
