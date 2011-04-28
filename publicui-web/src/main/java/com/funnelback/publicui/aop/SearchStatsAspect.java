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

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
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
		SearchQuestion q = null;
		Object o = mav.getModelMap().get(SearchController.ModelAttributes.SearchTransaction.toString());
		if (o != null && o instanceof SearchTransaction) {
			q = ((SearchTransaction) o).getQuestion();
		} else {
			// Try with the question directly
			o = mav.getModelMap().get(SearchController.ModelAttributes.question.toString());
			if (o != null && o instanceof SearchQuestion) {
				q = (SearchQuestion) o;
			}
		}
		
		if (q != null && q.getCollection() != null) {
			if (stats.get(q.getCollection().getId()) == null) {
				stats.put(q.getCollection().getId(), (long) 1);
			} else {
				stats.put(q.getCollection().getId(), stats.get(q.getCollection().getId())+1);
			}
		}
	}
	
}
