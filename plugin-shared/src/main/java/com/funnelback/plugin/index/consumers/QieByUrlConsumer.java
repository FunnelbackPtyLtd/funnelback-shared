package com.funnelback.plugin.index.consumers;

public interface QieByUrlConsumer {

    /**
     * A plugin may call this to supply a qie which should be set to the given URL(s).
     * 
     * @param qieWeight The QIE weight to be set. range: 0 - 1.
     * @param url The URL whose QIE weight is set.
     * @throws IllegalArgumentException when one or more of the arguments is not valid.
     */
    void applyQieWhenUrlMatches(double qieWeight, String url)
        throws IllegalArgumentException;
}



