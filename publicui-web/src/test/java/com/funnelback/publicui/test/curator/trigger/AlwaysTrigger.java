package com.funnelback.publicui.test.curator.trigger;

import org.springframework.context.ApplicationContext;

import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class AlwaysTrigger implements Trigger {

    @Override
    public boolean activatesOn(SearchTransaction searchTransaction, ApplicationContext context) {
        return true;
    }

}
