package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.AllArgsConstructor;

@AllArgsConstructor
// TODO rename
public class PadreForkingOptionsHelper implements PadreForkingOptions {

    private SearchTransaction searchTransaction;
    
    public long getPadreForkingTimeout() {
        return searchTransaction.getQuestion().getPadreTimeout().orElse( 
                searchTransaction.getQuestion().getCollection().getConfiguration()
                    .valueAsLong(Keys.ModernUI.PADRE_FORK_TIMEOUT, DefaultValues.ModernUI.PADRE_FORK_TIMEOUT_MS));
    }
    
    public int getPadreMaxPacketSize() {
        return searchTransaction.getQuestion().getMaxPadrePacketSize()
                .orElse(searchTransaction.getQuestion().getCollection().getConfiguration()
                    .valueAsInt(Keys.ModernUI.PADRE_RESPONSE_SIZE_LIMIT, DefaultValues.ModernUI.PADRE_RESPONSE_SIZE_LIMIT));
    }
    
    public int getSizeAtWhichToCompressPackets() {
        return searchTransaction.getQuestion().getCollection().getConfiguration()
                .valueAsInt(Keys.ModernUI.PADRE_PACKET_COMPRESSION_SIZE, DefaultValues.ModernUI.PADRE_PACKET_COMPRESSION_SIZE);
    }
    
}
