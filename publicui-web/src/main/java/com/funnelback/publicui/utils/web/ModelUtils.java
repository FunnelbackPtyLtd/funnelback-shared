package com.funnelback.publicui.utils.web;

import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.funnelback.publicui.search.web.controllers.SearchController;

/**
 * Utilities around the model part of Spring's {@link ModelAndView}.
 * 
 * @since v12.4
 */
public class ModelUtils {

    /**
     * Extracts a {@link SearchQuestion} from a Model
     * @param mav {@link ModelAndView} to extract the question from
     * @return the {@link SearchQuestion} or null if not found
     */
    public static SearchQuestion getSearchQuestion(ModelAndView mav) {
        SearchTransaction st = getSearchTransaction(mav);
        if (st != null) {
            return st.getQuestion();
        } else if (mav != null) {
            Object o = mav.getModel().get(SearchController.ModelAttributes.question.toString());
            if (o != null && o instanceof SearchQuestion) {
                return (SearchQuestion) o;
            }
        }
        return null;
    }

    /**
     * Extracts a {@link SearchResponse} from a Model
     * @param mav {@link ModelAndView} to extract the response from
     * @return the {@link SearchResponse} or null if not found
     */
    public static SearchResponse getSearchResponse(ModelAndView mav) {
        SearchTransaction st = getSearchTransaction(mav);
        if (st != null) {
            return st.getResponse();
        } else if (mav != null) {
            Object o = mav.getModel().get(SearchController.ModelAttributes.response.toString());
            if (o != null && o instanceof SearchResponse) {
                return (SearchResponse) o;
            }
        }
        return null;
    }
    
    /**
     * Extracts a {@link SearchSession} from a Model
     * @param mav {@link ModelAndView} to extract the session from
     * @return the {@link SearchSession} or null if not found
     */
    public static SearchSession getSearchSession(ModelAndView mav) {
        SearchTransaction st = getSearchTransaction(mav);
        if (st != null) {
            return st.getSession();
        } else if (mav != null) {
            Object o = mav.getModel().get(SearchController.ModelAttributes.session.toString());
            if (o != null && o instanceof SearchSession) {
                return (SearchSession) o;
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
 