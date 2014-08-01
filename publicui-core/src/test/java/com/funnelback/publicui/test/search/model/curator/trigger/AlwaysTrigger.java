package com.funnelback.publicui.test.search.model.curator.trigger;

import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class AlwaysTrigger implements Trigger {

    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        return true;
    }

    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }

}
