package com.funnelback.plugin.gatherer;

public interface GathererPlugin {

    public void gather(String searchHome, String collectionId) throws Exception;
}
