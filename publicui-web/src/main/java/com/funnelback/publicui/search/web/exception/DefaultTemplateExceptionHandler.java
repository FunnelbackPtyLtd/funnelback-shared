package com.funnelback.publicui.search.web.exception;

import java.io.IOException;
import java.io.Writer;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.DefaultValues.ModernUI.ErrorFormat;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.web.controllers.SearchController;

import freemarker.core.Environment;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateHashModel;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

/**
 * <p>Customise the way FreeMarker template exceptions are handled.</p>
 * 
 * <p>This handler will simply log the error but won't fail the whole page
 * rendering.</p>
 *
 * @since v11.4
 */
@Component
@Log4j2
public class DefaultTemplateExceptionHandler implements TemplateExceptionHandler {

    @Autowired
    private I18n i18n;
    
    @Override
    public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
        log.error("An error occurred while processing a template", te);
        
        // Try to get the source collection config
        TemplateHashModel model = env.getDataModel();
        if (model.get(SearchController.ModelAttributes.question.toString()) != null) {
            SearchQuestion sq = (SearchQuestion) ((AdapterTemplateModel)model.get(SearchController.ModelAttributes.question.toString())).getAdaptedObject(SearchQuestion.class);
            boolean displayErrors = sq.getCurrentProfileConfig().get(FrontEndKeys.ModernUi.Freemarker.DISPLAY_ERRORS).booleanValue();
            if (displayErrors) {
                String errorFormat = sq.getCurrentProfileConfig().get(FrontEndKeys.ModernUi.Freemarker.ERROR_FORMAT);
                try {
                    ErrorFormat fmt = ErrorFormat.valueOf(errorFormat);
                    switch (fmt) {
                    case exception:
                        out.write(te.getMessage());
                        throw te;
                    case html:
                        out.write("<!-- " + i18n.tr("freemarker.template.error") + ": " + te.getMessage() + " -->\n");
                        break;
                    case json:
                        out.write("/* " + i18n.tr("freemarker.template.error") + ": " + te.getMessage() + " */\n");
                        break;
                    case string:
                        out.write(i18n.tr("freemarker.template.error") + ": " + te.getMessage());
                        break;
                    }
                } catch (IOException ioe) {
                    throw new TemplateException(ioe, env);
                } catch (IllegalArgumentException iae) {
                    log.error(i18n.tr("freemarker.error_format.invalid", errorFormat, StringUtils.join(ErrorFormat.values(), ",")));
                    throw new TemplateException(iae, env);
                }
            }
        } else {
            throw te;
        }
    }
}
