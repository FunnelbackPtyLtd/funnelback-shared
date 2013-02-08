package com.funnelback.publicui.utils.web;

import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.web.controllers.SearchController;

/**
 * Utilities around the model part of Spring's {@link ModelAndView}.
 * 
 * @since v12.4
 */
public class ModelUtils {

	/**
	 * Extracts a {@link SearchQuestion} from a Model
	 * @param mav
	 * @return the {@link SearchQuestion} or null if not found
	 */
	public static SearchQuestion getSearchQuestion(ModelAndView mav) {
		SearchTransaction st = getSearchTransaction(mav);
		if (st != null) {
			return st.getQuestion();
		} else {
			Object o = mav.getModel().get(SearchController.ModelAttributes.question.toString());
			if (o != null && o instanceof SearchQuestion) {
				return (SearchQuestion) o;
			}
		}
		return null;
	}

	/**
	 * Extracts a {@link SearchResponse} from a Model
	 * @param mav
	 * @return the {@link SearchResponse} or null if not found
	 */
	public static SearchResponse getSearchResponse(ModelAndView mav) {
		SearchTransaction st = getSearchTransaction(mav);
		if (st != null) {
			return st.getResponse();
		} else {
			Object o = mav.getModel().get(SearchController.ModelAttributes.response.toString());
			if (o != null && o instanceof SearchResponse) {
				return (SearchResponse) o;
			}
		}
		return null;
	}

	/**
	 * Extracts a {@link SearchTransaction} from a Model
	 * @param mav
	 * @return the {@link SearchTransaction}, or null if not found
	 */
	public static SearchTransaction getSearchTransaction(ModelAndView mav) {
		if (mav != null) {
			Object o = mav.getModel().get(SearchController.ModelAttributes.SearchTransaction.toString());
			if (o != null && o instanceof SearchTransaction) {
				return (SearchTransaction) o;
			}
		}
		return null;
	}

}
 