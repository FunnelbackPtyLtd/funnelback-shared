package com.funnelback.publicui.search.web.views.freemarker.escaping;

import freemarker.core.CommonTemplateMarkupOutputModel;

public class TemplateXHTMLPlusAngularOutputModel extends CommonTemplateMarkupOutputModel<TemplateXHTMLPlusAngularOutputModel> {
    public TemplateXHTMLPlusAngularOutputModel(String plainTextContent, String markupContent) {
        super(plainTextContent, markupContent);
    }

    @Override
    public XHTMLPlusAngularOutputFormat getOutputFormat() {
        return XHTMLPlusAngularOutputFormat.INSTANCE;
    }
}
