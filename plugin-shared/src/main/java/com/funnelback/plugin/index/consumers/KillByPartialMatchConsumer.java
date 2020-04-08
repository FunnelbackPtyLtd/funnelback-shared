package com.funnelback.plugin.index.consumers;

@FunctionalInterface
public interface KillByPartialMatchConsumer {
    
    /**
     * 
     * @param urlToKillByPartialMatch URLs containing this will be killed.
     */
    public void killByPartialMatch(String urlToKillByPartialMatch);
}
