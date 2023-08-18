package com.funnelback.plugin.index.consumers;

public interface QieByUrlConsumer {

    /**
     * A plugin may call this to supply a qie which should be set to the given URLs.
     * 
     * @param qieWeight The QIE weight to be set
     * @param url The URL whose QIE weight is to be set.
     * @throws IllegalArgumentException when one or more of the arguments is not valid.
     */
    void applyQieWhenRegexMatches(double qieWeight, String url)
        throws IllegalArgumentException;
}


