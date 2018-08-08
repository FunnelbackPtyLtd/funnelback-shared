package com.funnelback.publicui.search.web.filters;

import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.web.interceptors.helpers.IntercepterHelper;
import com.funnelback.publicui.utils.web.ProfilePicker;
import lombok.Setter;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.springmvc.web.filter.CorsFilter;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

/**
 * Filter to add a CORS allow origin header
 *
 * @author nguillaumin@funnelback.com
 */
@Log4j2
public class ConfigurableCorsFilter extends CorsFilter implements Filter {

    @Autowired
    @Setter
    private ConfigRepository configRepository;
    private IntercepterHelper intercepterHelper = new IntercepterHelper();

    @Override
    public Optional<String> getCorsAllowOrigin(ServletRequest request, ServletResponse response) {

        Collection collection = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
        if (collection != null) {
            String profileId = new ProfilePicker().existingProfileForCollection(collection,
                intercepterHelper.getProfileFromRequestOrDefaultProfile((HttpServletRequest) request));
            String collectionId = intercepterHelper.getCollectionFromRequest((HttpServletRequest) request);
            ServiceConfigReadOnly serviceConfig;
            try {
                serviceConfig = configRepository.getServiceConfig(collectionId, profileId);
                return serviceConfig.get(FrontEndKeys.ModernUi.CORS_ALLOW_ORIGIN);
            } catch (ProfileNotFoundException e) {
                log.error("Couldn't find profile '" + profileId + "' in " + collectionId, e);
            }
        }
        return super.getCorsAllowOrigin(request, response);
    }

}
