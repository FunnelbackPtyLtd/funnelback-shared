package com.funnelback.publicui.curator;

import java.io.File;

import com.funnelback.publicui.curator.action.GroovyActionInterface;

public interface GroovyActionResourceManager {
    
    public GroovyActionInterface getGroovyObject(File script);
    
}
