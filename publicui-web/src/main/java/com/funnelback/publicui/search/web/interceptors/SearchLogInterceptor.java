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
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.log.LogService;
import com.funnelback.publicui.search.web.controllers.SearchController;

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
			SearchQuestion q = null;
			Object o = modelAndView.getModel().get(SearchController.ModelAttributes.SearchTransaction.toString());
			
			if (o != null && o instanceof SearchTransaction) {
				SearchTransaction t = (SearchTransaction) o;
				q = t.getQuestion();
			} else {
				// Try directly with the question
				o = modelAndView.getModel().get(SearchController.ModelAttributes.question.toString());
				if ( o != null && o instanceof SearchQuestion) {
					q = (SearchQuestion) o;
				}
			}
			
			if (q != null && q.getCnClickedCluster() != null && q.getCollection() != null) {
				
				ContextualNavigationLog cnl = new ContextualNavigationLog(
						new Date(),
						q.getCollection(),
						q.getCollection().getProfiles().get(q.getProfile()),
						q.getUserId(),
						q.getCnClickedCluster(),
						q.getCnPreviousClusters());
				
				logService.logContextualNavigation(cnl);
			}
		}
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

}
