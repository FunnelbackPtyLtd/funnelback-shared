package com.funnelback.publicui.search.web.views.freemarker.escaping;

import freemarker.core.CommonTemplateMarkupOutputModel;

public class TemplateHTMLPlusAngularOutputModel extends CommonTemplateMarkupOutputModel<TemplateHTMLPlusAngularOutputModel> {
    public TemplateHTMLPlusAngularOutputModel(String plainTextContent, String markupContent) {
        super(plainTextContent, markupContent);
    }

    @Override
    public HTMLPlusAngularOutputFormat getOutputFormat() {
        return HTMLPlusAngularOutputFormat.INSTANCE;
    }
}
