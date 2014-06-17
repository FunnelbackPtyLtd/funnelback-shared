package com.funnelback.publicui.curator;

import java.io.File;

import com.funnelback.publicui.curator.trigger.GroovyTrigger;
import com.funnelback.publicui.curator.trigger.GroovyTriggerInterface;

public interface GroovyTriggerResourceManager {
    
    public GroovyTriggerInterface getGroovyObject(File script);
    
}
