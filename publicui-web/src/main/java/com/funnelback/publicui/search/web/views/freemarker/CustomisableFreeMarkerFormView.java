package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.config.configtypes.mix.ProfileAndCollectionConfigOption;
import com.funnelback.config.configtypes.service.ServiceConfigOptionDefinition;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.keys.Keys.FrontEndKeys;
import com.funnelback.config.marshallers.Marshallers;
import com.funnelback.config.validators.Validators;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.search.web.controllers.SearchController;

import lombok.extern.log4j.Log4j2;

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

        if (model.containsKey(SearchController.ModelAttributes.question.toString())) {
            // Model contains a SearchQuestion object, it's a search request
            SearchQuestion q = (SearchQuestion) model.get(SearchController.ModelAttributes.question.toString());
            if (q.getCurrentProfileConfig() != null) {
                // Find the form name by inspecting the last part of the URL,
                // like 'csv' in 'conf/<collection/<profile/csv.ftl'
                String name = url.substring(
                        url.lastIndexOf('/')+1,
                        url.lastIndexOf(DefaultConfigRepository.FTL_SUFFIX));

                ServiceConfigReadOnly serviceConfig = q.getCurrentProfileConfig();
                manipulateHeaderForSearchForm(serviceConfig, response, name);
            }
        } else if (model.containsKey(RequestParameters.COLLECTION) && model.containsKey(RequestParameters.PROFILE)
                && model.get(RequestParameters.COLLECTION) instanceof Collection) {
            // Model looks like it might be a cache request

            String name = url.substring(
                    url.lastIndexOf('/')+1,
                    url.lastIndexOf(DefaultValues.CACHE_FORM_SUFFIX + DefaultConfigRepository.FTL_SUFFIX));

            Collection collection = (Collection) model.get(RequestParameters.COLLECTION);
            String profile = model.get(RequestParameters.PROFILE).toString();
            
            if (!collection.getProfiles().containsKey(profile)) {
                profile = DefaultValues.DEFAULT_PROFILE;
            }
            ServiceConfigReadOnly serviceConfig = collection.getProfiles().get(profile).getServiceConfig();
            manipulateHeaderForCacheForm(serviceConfig, response, name);
        }
    }
    
    void manipulateHeaderForSearchForm(ServiceConfigReadOnly serviceConfig, 
                                    HttpServletResponse response, 
                                    String formName) {
        manipulateHeaders(serviceConfig, response, 
            FrontEndKeys.UI.Modern.getCustomContentTypeOptionForForm(formName),
            "ui.modern.form." + formName + ".headers.",
            FrontEndKeys.UI.Modern.getRemoveHeaderForForm(formName));
    }
    
    void manipulateHeaderForCacheForm(ServiceConfigReadOnly serviceConfig, 
                                    HttpServletResponse response, 
                                    String formName) {
        manipulateHeaders(serviceConfig, response, 
            FrontEndKeys.UI.Modern.Cache.getCustomContentTypeOptionForForm(formName),
            "ui.modern.cache.form." + formName + ".headers.",
            FrontEndKeys.UI.Modern.Cache.getRemoveHeaderForForm(formName));
    }
    
    private void manipulateHeaders(ServiceConfigReadOnly serviceConfig, 
                                    HttpServletResponse response, 
                                    ProfileAndCollectionConfigOption<Optional<String>> contentTypeConfigOption,
                                    String customHeadersPrefix,
                                    ServiceConfigOptionDefinition<List<String>> headerToRemoveKey) {

        setCustomContentType(contentTypeConfigOption, serviceConfig, response);
        setCustomHeaders(customHeadersPrefix, serviceConfig, response);
        removeHeaders(serviceConfig, response, headerToRemoveKey);
    }
    
    /**
     * Sets custom HTTP headers, if configured in the collection's configuration.
     * @param customHeaderKeyPrefix Prefix to use to read the setting in profile/collection.cfg
     * @param serviceConfig Frontend configuration.
     * @param response Response object to add custom headers to.
     */
    void setCustomHeaders(String customHeaderKeyPrefix, ServiceConfigReadOnly serviceConfig, HttpServletResponse response) {
        int sentCount = 0;
        for (ServiceConfigOptionDefinition<String> customHeaderKey : keysStartingWith(customHeaderKeyPrefix, serviceConfig)) {
            String header = serviceConfig.get(customHeaderKey);
            if (header != null && header.contains(":")) {
                response.setHeader(header.substring(0, header.indexOf(':')).trim(),
                    header.substring(header.indexOf(':')+1).trim());
                sentCount++;
            }
            log.debug("Added " + sentCount + " custom headers for " + customHeaderKeyPrefix);
        }
    }
    
    /**
     * Removes headers from the response as configured in the front-ends configurations.
     * 
     * @param formName The form that is being returned.
     * @param serviceConfig
     * @param response
     */
    void removeHeaders(ServiceConfigReadOnly serviceConfig, 
        HttpServletResponse response, 
        ServiceConfigOptionDefinition<List<String>> headerToRemoveKey) {
        for (String headerName : serviceConfig.get(headerToRemoveKey)) {
            // This should remove the header, the java doc doesn't seem to say it will
            // however jetty, tomcat and glassfish all do.
            // https://github.com/eclipse/jetty.project/issues/1116#issuecomment-326084030
            response.setHeader(headerName, null);
            log.debug("Removed header: " + headerName);
        }
    }
    
    /**
     * Gets all keys starting with a prefix as a String type key.
     * 
     * @param keyprefix
     * @param serviceConfig
     * @return
     */
    private List<ServiceConfigOptionDefinition<String>> keysStartingWith(String keyprefix, ServiceConfigReadOnly serviceConfig) {
        return serviceConfig.getRawKeys().stream()
            .filter((k) -> k.startsWith(keyprefix))
            .map(k -> new ProfileAndCollectionConfigOption<String>(k, Marshallers.STRING_MARSHALLER, Validators.acceptAll(), ""))
            .collect(Collectors.toList());
    }
    
    /**
     * Sets a custom content type, if configured in the collection's configuration
     * @param configOption Option to use to read the setting in profile/collection.cfg
     * @param serviceConfig Frontend configuration.
     * @param response Response object to add custom headers to.
     */
    private void setCustomContentType(ProfileAndCollectionConfigOption<Optional<String>> configOption, ServiceConfigReadOnly serviceConfig, HttpServletResponse response) {
        Optional<String> customContentType = serviceConfig.get(configOption);
        
        customContentType.ifPresent((contentType) -> {
            response.setContentType(contentType);
            log.debug("Set custom Content Type '" + contentType + "' due to '"+configOption.getKey()+"'");            
        });
    }
}
