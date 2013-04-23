package com.funnelback.publicui.search.web.interceptors;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.log.LogService;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.utils.web.ModelUtils;

public class SearchLogInterceptor implements HandlerInterceptor {
    
    @Autowired
    @Setter private LogService logService;
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        SearchQuestion q = ModelUtils.getSearchQuestion(modelAndView);
            
        if (q != null && q.getCnClickedCluster() != null && q.getCollection() != null) {
            
            ContextualNavigationLog cnl = new ContextualNavigationLog(
                    new Date(),
                    q.getCollection(),
                    q.getCollection().getProfiles().get(q.getProfile()),
                    q.getRequestIdToLog(),
                    q.getCnClickedCluster(),
                    q.getCnPreviousClusters(),
                    LogUtils.getUserId(ModelUtils.getSearchSession(modelAndView)));
                
            logService.logContextualNavigation(cnl);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

}
