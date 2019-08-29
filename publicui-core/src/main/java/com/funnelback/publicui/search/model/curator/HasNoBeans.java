package com.funnelback.publicui.search.model.curator;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.model.curator.config.Configurer;

/**
 * Add this to anything that runs {@link Configurer} which does not require any {@link Autowired} 
 * annotation processing.
 * 
 * This saves a substantial amount of time, as annotation processing is relatively slow.
 */
public interface HasNoBeans {

}
