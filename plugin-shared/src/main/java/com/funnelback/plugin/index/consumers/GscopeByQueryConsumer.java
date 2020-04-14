package com.funnelback.plugin.index.consumers;

@FunctionalInterface
public interface GscopeByQueryConsumer {

    /**
     * A plugin may call this to supply a gscope which should be set for all documents that match the query.
     * 
     * @param gscopeName The name the gscope to set. Limited to ASCII alpha numeric 
     * characters and must be no longer than 64 characters. Additionally gscope names which start 
     * with 'Fun' in any upper or lower case form are reserved for internal use.
     * @param query The query which will be run.
     * @throws IllegalArgumentException when one or more of the arguments is not valid.
     */
    void applyGscopeWhenQueryMatches(String gscopeName, String query)
        throws IllegalArgumentException;
}
