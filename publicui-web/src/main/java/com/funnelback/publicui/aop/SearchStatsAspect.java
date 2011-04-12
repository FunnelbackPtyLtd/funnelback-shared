package com.funnelback.publicui.aop;

import java.util.Map;

import javax.annotation.Resource;

import lombok.extern.apachecommons.Log;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.web.controllers.SearchController;

@Component
@Aspect
@Log
public class SearchStatsAspect {

	@Value("#{appProperties['profiling.history.size']}")
	private int historySize;

	@Resource(name = "searchStats")
	private Map<String, Long> stats;

	@Pointcut("within(com.funnelback.publicui.search.web.controllers.SearchController) && execution(public * search(..))")
	public void searchMethod() {
	}
	
	@AfterReturning(pointcut="searchMethod()",returning="mav")
	public void adviceManual(ModelAndView mav) {
		Object o = mav.getModelMap().get(SearchController.MODEL_KEY_SEARCH_TRANSACTION);
		if (o instanceof SearchTransaction) {
			SearchTransaction st = (SearchTransaction) o;
			if (SearchTransactionUtils.hasQueryAndCollection(st)) {
				log.debug("*** Caught search on collection " + st.getQuestion().getCollection().getId());
				
				if (stats.get(st.getQuestion().getCollection().getId()) == null) {
					stats.put(st.getQuestion().getCollection().getId(), (long) 1);
				} else {
					stats.put(st.getQuestion().getCollection().getId(), stats.get(st.getQuestion().getCollection().getId())+1);
				}
			}
		}
	}

	

	
}
