package com.funnelback.plugin.index.consumers;

@FunctionalInterface
public interface KillByExactMatchConsumer {
    
    /**
     * 
     * @param urlToKillByExactMatch
     * @throws IllegalArgumentException when one or more of the arguments is not valid.
     */
    public void killByExactMatch(String urlToKillByExactMatch)
        throws IllegalArgumentException;
}
