package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;

import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.config.configtypes.mix.ProfileAndCollectionConfigOption;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.keys.Keys.FrontEndKeys;
import com.funnelback.config.marshallers.Marshallers;
import com.funnelback.config.validators.Validators;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.search.web.controllers.SearchController;

/**
 * <p>{@link FreeMarkerView} that allows Content Type and
 * Headers customisation based on the collection configuration.</p>
 */
@Log4j2
public class CustomisableFreeMarkerFormView extends FreeMarkerView {

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (getUrl() != null
                && model != null) {

            customiseOutput(getUrl(), model, response);
        }

        super.render(model, request, response);
    }

    /**
     * Customise the output by sending custom headers and/or content type for
     * search or cache requests, depending of the collection configuration and the form
     * requested.
     * 
     * @param url
     * @param model
     * @param response
     */
    protected void customiseOutput(String url, Map<String, ?> model, HttpServletResponse response) {
        // Find the form name by inspecting the last part of the URL,
        // like 'csv' in 'conf/<collection/<profile/csv.ftl'
        String name = url.substring(
                url.lastIndexOf('/')+1,
                url.lastIndexOf(DefaultConfigRepository.FTL_SUFFIX));

        Config config = null;
        ServiceConfigReadOnly serviceConfig = null;
        String settingPrefix = null;
        
        if (model.containsKey(SearchController.ModelAttributes.question.toString())) {
            // Model contains a SearchQuestion object, it's a search request
            SearchQuestion q = (SearchQuestion) model.get(SearchController.ModelAttributes.question.toString());
            if (q.getCollection() != null && q.getCollection().getConfiguration() != null) {
                config = q.getCollection().getConfiguration();
                serviceConfig = q.getCurrentProfile().getServiceConfig();
                settingPrefix = Keys.ModernUI.FORM_PREFIX;
            }
        } else if (model.containsKey(RequestParameters.COLLECTION)
                && model.get(RequestParameters.COLLECTION) instanceof Collection) {
            // Model contains a Collection object, possibly another non-search controller
            Collection c = (Collection) model.get(RequestParameters.COLLECTION);
            if (c.getConfiguration() != null) {
                config = c.getConfiguration();
                settingPrefix = Keys.ModernUI.FORM_PREFIX;
                
                if (name.endsWith(DefaultValues.CACHE_FORM_SUFFIX)) {
                    // Strip off ".cache" suffix from form name
                    settingPrefix = Keys.ModernUI.Cache.FORM_PREFIX;
                    name = name.substring(0, name.lastIndexOf(DefaultValues.CACHE_FORM_SUFFIX));
                }
            }
        }
        
        if (config != null && settingPrefix != null) {
            setCustomHeaders(settingPrefix, name, config, response);
        }
        if (serviceConfig != null && settingPrefix != null) {
            setCustomContentType(settingPrefix, name, serviceConfig, response);
        }
    }
    
    /**
     * Sets custom HTTP headers, if configured in the collection's configuration.
     * @param settingPrefix Prefix to use to read the setting in collection.cfg
     * @param tplName Name of the current form, without extension.
     * @param config Collection configuration.
     * @param response
     */
    private void setCustomHeaders(String settingPrefix, String tplName, Config config, HttpServletResponse response) {
        // Search for custom headers
        String propertyPrefix = settingPrefix + "." + tplName;
        if (config.hasValue(propertyPrefix+"."+Keys.ModernUI.HEADERS_COUNT_SUFFIX)) {
            int nbHeaders = config.valueAsInt(propertyPrefix+"."+Keys.ModernUI.HEADERS_COUNT_SUFFIX, 0);
            int sent = 0;
            for (int i=0; i<nbHeaders; i++) {
                String header = config.value(propertyPrefix+"."+Keys.ModernUI.HEADERS_SUFFIX+"."+(i+1), null);
                if (header != null && header.contains(":")) {
                    response.setHeader(header.substring(0, header.indexOf(':')).trim(),
                        header.substring(header.indexOf(':')+1).trim());
                    sent++;
                }
            }
            log.debug("Added " + sent + " custom headers for form '"+tplName+"'");
        }
    }
    
    /**
     * Sets a custom content type, if configured in the collection's configuration
     * @param settingPrefix Prefix to use to read the setting in profile/collection.cfg
     * @param tplName Name of the current form, without extension.
     * @param config Collection configuration.
     * @param response
     */
    private void setCustomContentType(String settingPrefix, String tplName, ServiceConfigReadOnly serviceConfig, HttpServletResponse response) {
        Optional<String> customContentType = serviceConfig.get(FrontEndKeys.UI.Modern.getCustomContentTypeOptionForForm(tplName));
        
        customContentType.ifPresent((contentType) -> {
            response.setContentType(contentType);
            log.debug("Set custom Content Type '" + contentType + "' for form '"+tplName+"'");            
        });
    }
}
