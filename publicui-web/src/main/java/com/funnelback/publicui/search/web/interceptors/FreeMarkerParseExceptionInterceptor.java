package com.funnelback.publicui.search.web.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.web.exception.InvalidCollectionException;
import com.funnelback.publicui.search.web.interceptors.helpers.IntercepterHelper;
import com.funnelback.publicui.utils.web.ProfilePicker;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

import freemarker.core.ParseException;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

/**
 * <p>Interceptor handling FreeMarker {@link ParseException}s.</p>
 *
 * <p>They cannot be handled via standard FreeMarker error handling or controller
 * level Exception handlers because they happen after the controller, when the view
 * is rendered.</p>
 *
 * <p>The interceptor will check if FreeMarker error output is enabled on the collection
 * before writing the exception message to the response</p>
 *
 * @author Nicolas Guillaumin
 *
 */
@Log4j2
public class FreeMarkerParseExceptionInterceptor implements HandlerInterceptor {

    private IntercepterHelper intercepterHelper = new IntercepterHelper();

    @Autowired
    @Setter private ConfigRepository configRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
        throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (ex != null && request.getParameter(RequestParameters.COLLECTION) != null) {
            // From experience the ParseException can be nested at different levels
            // Extract it
            ParseException pe = ExceptionUtils.getThrowableList(ex)
                .stream()
                .filter(t -> t.getClass().equals(ParseException.class))
                .map(t -> (ParseException) t)
                .findFirst()
                .orElse(null);

            if (pe != null) {
                // Handle ParseException depending on the config
                Collection collection = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));

                if (collection != null) {
                    String profileId = new ProfilePicker().existingProfileForCollection(collection, intercepterHelper.getProfileFromRequestOrDefaultProfile(request));
                    String collectionId = intercepterHelper.getCollectionFromRequest(request);
                    ServiceConfigReadOnly serviceConfig;
                    try {
                        serviceConfig = configRepository.getServiceConfig(collectionId, profileId);
                    } catch (ProfileNotFoundException e) {
                        throw new InvalidCollectionException(collectionId + " appears to exist but is invalid as it is missing the '"
                                + profileId + "' profile which is expected to exist.");
                    }
                    if (serviceConfig.get(FrontEndKeys.FREEMARKER_DISPLAY_ERRORS).booleanValue()) {
                        // No need to be fancy about the format of the error. If we were unable
                        // to parse the template, nothing will be rendered anyway. Just output
                        // the error as-is
                        response.setContentType("text/plain");
                        response.getWriter().write(pe.getMessage());
                    }
                    // Also log it for the per-collection log file
                    log.error("Error parsing FreeMarker template", pe);
                }
            }
        }

    }


}
