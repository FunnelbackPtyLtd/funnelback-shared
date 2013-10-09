package com.funnelback.publicui.search.lifecycle.input.processors;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Sets the relevant QP option to include the user ID, if available,
 * in the click logs
 * 
 * @since 12.5
 */
@Component("userIdInQueryLogs")
public class UserIdInQueryLogs extends AbstractInputProcessor {

    /** Name of the QP option to specify the user id */
    public static final String QP_OPT_USERNAME = "-username";

    
    @Override
    public void processInput(SearchTransaction st) throws InputProcessorException {
        if (SearchTransactionUtils.hasQuestion(st)
            && SearchTransactionUtils.hasSession(st)
            && st.getSession().getSearchUser() != null
            && st.getSession().getSearchUser().getId() != null) {

            st.getQuestion().getDynamicQueryProcessorOptions()
                .add(QP_OPT_USERNAME + "=" + st.getSession().getSearchUser().getId());
        }
    }

}
