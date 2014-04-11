package com.funnelback.publicui.recommender.interceptors;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is used to intercept requests to the DevRecommenderController which is
 * disabled by default and must be explicitly enabled for a given collection. This can be done
 * by setting:
 *
 *     recommender.dev_access_allowed=true
 *
 * in the relevant collection.cfg file.
 * @author fcrimmins@funnelback.com
 */
public class DevRecommenderInterceptor implements HandlerInterceptor {
    public static final String DEV_ACCESS_ALLOWED = "recommender.dev_access_allowed";

    @Autowired
    private ConfigRepository configRepository;

    @Override
    public void afterCompletion(HttpServletRequest arg0,
        HttpServletResponse arg1, Object arg2, Exception arg3) throws Exception {
    }

    @Override
    public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
            Object arg2, ModelAndView arg3) throws Exception {
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        if (request.getParameter(SearchQuestion.RequestParameters.COLLECTION) != null) {
            Collection collection = configRepository.getCollection(request
                    .getParameter(SearchQuestion.RequestParameters.COLLECTION));

            if (collection != null) {
                boolean accessAllowed = collection.getConfiguration().valueAsBoolean(DEV_ACCESS_ALLOWED, false);

                if (accessAllowed) {
                    return true;
                }
            }
        }

        // Return an "Access forbidden" message
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/plain");
        response.getWriter().write("Access forbidden.");

        return false;
    }
}
