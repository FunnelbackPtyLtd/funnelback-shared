package com.funnelback.publicui.search.web.interceptors;

import java.net.URL;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.usertracking.SearchHistory;
import com.funnelback.publicui.search.model.transaction.usertracking.SearchUser;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.utils.web.ModelUtils;

/**
 * Store the current search transaction in the user
 * search history.
 * 
 * @since v12.4
 */
public class SearchHistoryInterceptor implements HandlerInterceptor {

	@Autowired
	private SearchHistoryRepository repository;
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
		SearchUser user = (SearchUser) request.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE);
		if (user != null) {

			SearchResponse r = ModelUtils.getSearchResponse(modelAndView);
			SearchQuestion q = ModelUtils.getSearchQuestion(modelAndView);
			if (q != null && r != null) {
				SearchHistory h = new SearchHistory();
				h.setCurrStart(r.getResultPacket().getResultsSummary().getCurrStart());
				h.setNumRanks(r.getResultPacket().getResultsSummary().getNumRanks());
				h.setOriginalQuery(q.getOriginalQuery());
				h.setQueryAsProcessed(r.getResultPacket().getQueryAsProcessed());
				h.setSearchDate(new Date());
				h.setSearchUrl(new URL(request.getRequestURL()+"?"+request.getQueryString()));
				h.setTotalMatching(r.getResultPacket().getResultsSummary().getTotalMatching());
				h.setUser(user);
				
				repository.save(user, h);
				
				modelAndView.getModel().put("searchHistory", repository.get(user, 5));
			}
		}

	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}

}
