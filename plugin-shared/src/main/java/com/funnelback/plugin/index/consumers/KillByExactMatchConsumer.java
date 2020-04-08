package com.funnelback.plugin.index.consumers;

@FunctionalInterface
public interface KillByExactMatchConsumer {
    
    /**
     * 
     * @param urlToKillByExactMatch
     */
    public void killByExactMatch(String urlToKillByExactMatch);
}
