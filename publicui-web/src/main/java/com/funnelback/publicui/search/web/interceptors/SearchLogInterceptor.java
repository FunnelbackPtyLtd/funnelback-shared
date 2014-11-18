package com.funnelback.publicui.search.web.interceptors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.log.FacetedNavigationLog;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.log.LogService;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.funnelback.publicui.utils.web.ModelUtils;
import com.google.common.collect.Ordering;

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
            
        if (q != null && q.getCollection() != null) {
            Date now = new Date();
            if (q.getCnClickedCluster() != null) {
                ContextualNavigationLog cnl = new ContextualNavigationLog(
                        now,
                        q.getCollection(),
                        q.getCollection().getProfiles().get(q.getProfile()),
                        q.getRequestId(),
                        q.getCnClickedCluster(),
                        q.getCnPreviousClusters(),
                        LogUtils.getUserId(ModelUtils.getSearchSession(modelAndView)));
                    
                logService.logContextualNavigation(cnl);
            }
            
            if (q.getSelectedFacets() != null && !q.getSelectedFacets().isEmpty()
                && q.getSelectedCategoryValues() != null && !q.getSelectedCategoryValues().isEmpty()) {
                String selected = buildSelectedCategoriesList(q);
                if (selected != null && !"".equals(selected)) {
                    FacetedNavigationLog fnl = new FacetedNavigationLog(
                        now,
                        q.getCollection(),
                        q.getCollection().getProfiles().get(q.getProfile()),
                        q.getRequestId(),
                        LogUtils.getUserId(ModelUtils.getSearchSession(modelAndView)),
                        selected,
                        q.getOriginalQuery());
                    
                    logService.logFacetedNavigation(fnl);
                }
            }
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    /**
     * Builds the list of currently selected facets and categories
     * @param q Current {@link SearchQuestion}
     * @return String representing the currently selected Facets and Categories
     */
    private String buildSelectedCategoriesList(SearchQuestion q) {
        FacetedNavigationConfig cnf = FacetedNavigationUtils.selectConfiguration(q.getCollection(), q.getProfile());
        List<String> selectedFacets = new ArrayList<>();
        
        for (String f: q.getSelectedFacets()) {
            FacetDefinition fDef = cnf.getFacetDefinition(f);
            if (fDef != null) {
                List<String> allValues = new ArrayList<String>();
                for (String param: fDef.getAllQueryStringParamNames()) {
                    List<String> values = q.getSelectedCategoryValues().get(param);
                    if (values != null) {
                        allValues.addAll(values);
                    }
                }
                selectedFacets.add(f + ": " + StringUtils.join(Ordering.natural().sortedCopy(allValues), " + "));
            }
        }
        
        return StringUtils.join(Ordering.natural().sortedCopy(selectedFacets), ", ");
        
    }

}
