package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import static com.funnelback.config.keys.Keys.FrontEndKeys;

public class AccessibilityAuditorDaatOption {

    /**
     * Set a higher DAAT value than the default if configured.
     * 
     * @param searchQuestion SearchQuestion to read the DAAT value from
     * @return PADRE <code>daat</code> option, or default value if not configured
     */
    public String getDaatOption(SearchQuestion searchQuestion) {
        return "-" + QueryProcessorOptionKeys.DAAT + "=" + searchQuestion.getCurrentProfileConfig().get(FrontEndKeys.ModernUi.AccessibilityAuditor.DAAT_LIMIT).toString();
    }
}
