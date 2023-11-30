package com.funnelback.plugin.index.consumers;

public interface KillByQueryMatchConsumer {

    /**
     * A plugin may call this to kill the URL(s) returned by a given query.
     * @param queryToKillByMatch The query to return a list of URLs to be killed.
     * @throws IllegalArgumentException when one or more of the arguments is not valid.
     */
    void killByQueryMatch(String queryToKillByMatch)
        throws IllegalArgumentException;
}
