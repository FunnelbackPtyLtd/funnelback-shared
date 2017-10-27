package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class PadreForkingOptionsHelper {

    
    public long getPadreForkingTimeout(SearchTransaction searchTransaction) {
        return searchTransaction.getQuestion().getPadreTimeout().orElse( 
                searchTransaction.getQuestion().getCollection().getConfiguration()
                    .valueAsLong(Keys.ModernUI.PADRE_FORK_TIMEOUT, DefaultValues.ModernUI.PADRE_FORK_TIMEOUT_MS));
    }
    
    public int getPadreMaxPacketSize(SearchTransaction searchTransaction) {
        return searchTransaction.getQuestion().getMaxPadrePacketSize()
                .orElse(searchTransaction.getQuestion().getCollection().getConfiguration()
                    .valueAsInt(Keys.ModernUI.PADRE_RESPONSE_SIZE_LIMIT, DefaultValues.ModernUI.PADRE_RESPONSE_SIZE_LIMIT));
    }
}
