package com.funnelback.plugin.index.consumers;

public interface QieByQueryConsumer {

    /**
     * A plugin may call this to supply a gscope which should be set for all documents that match the query.
     * 
     * @param qieWeight The QIE weight to be set. range: 0 - 1.
     * @param query The query which will be run.
     * @throws IllegalArgumentException when one or more of the arguments is not valid.
     */
    void applyQieWhenQueryMatches(double qieWeight, String query)
        throws IllegalArgumentException;
}

