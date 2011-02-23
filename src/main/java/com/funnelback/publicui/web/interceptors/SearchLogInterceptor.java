package com.funnelback.publicui.web.interceptors;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.log.LogService;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.web.controllers.SearchController;

@lombok.extern.apachecommons.Log
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
		if (modelAndView != null) {
			Object o = modelAndView.getModel().get(SearchController.MODEL_KEY_SEARCH_TRANSACTION);
			if (o != null && o instanceof SearchTransaction) {
				SearchTransaction t = (SearchTransaction) o;
				
				if (t.hasQuestion() && t.getQuestion().getCnClickedCluster() != null
						&& t.getQuestion().getCollection() != null) {
					
					ContextualNavigationLog cnl = new ContextualNavigationLog(
							new Date(),
							t.getQuestion().getCollection(),
							t.getQuestion().getCollection().getProfiles().get(t.getQuestion().getProfile()),
							t.getQuestion().getUserId(),
							t.getQuestion().getCnClickedCluster(),
							t.getQuestion().getCnPreviousClusters());
					
					logService.logContextualNavigation(cnl);
				}
			}

			SearchQuestion sq = (SearchQuestion) modelAndView.getModel().get("searchQuery");
			if (sq != null) { log.debug(sq); }
		
			SearchResponse srs = (SearchResponse) modelAndView.getModel().get("searchResults");
			if (srs != null) { log.debug(srs); }
		}
		
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

}
