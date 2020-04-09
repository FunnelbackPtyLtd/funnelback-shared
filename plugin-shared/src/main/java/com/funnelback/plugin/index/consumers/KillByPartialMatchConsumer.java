package com.funnelback.plugin.index.consumers;

@FunctionalInterface
public interface KillByPartialMatchConsumer {
    
    /**
     * 
     * @param urlToKillByPartialMatch URLs containing this will be killed.
     * @throws IllegalArgumentException when one or more of the arguments is not valid.
     */
    public void killByPartialMatch(String urlToKillByPartialMatch)
        throws IllegalArgumentException;
}
