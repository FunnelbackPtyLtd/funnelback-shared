package com.funnelback.publicui.test.curator.trigger;

import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class NeverTrigger implements Trigger {

    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        return false;
    }

}
