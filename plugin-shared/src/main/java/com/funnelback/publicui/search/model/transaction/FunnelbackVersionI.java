package com.funnelback.publicui.search.model.transaction;

public interface FunnelbackVersionI {

    int getMajor();
    int getMinor();
    int getRevision();
    
    /**
     * Version in human readable form.
     * 
     * @return
     */
    String toString();
}
