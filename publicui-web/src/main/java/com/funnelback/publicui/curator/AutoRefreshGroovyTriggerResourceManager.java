package com.funnelback.publicui.curator;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.curator.trigger.GroovyTriggerInterface;
import com.funnelback.publicui.search.service.resource.impl.GroovyObjectResource;
import com.funnelback.springmvc.service.resource.ResourceManager;

@Component
public class AutoRefreshGroovyTriggerResourceManager implements GroovyTriggerResourceManager {

    @Autowired
    ResourceManager resourceManager;
    
    @Override
    public GroovyTriggerInterface getGroovyObject(File script) {
        GroovyTriggerInterface result;
        try {
            result = resourceManager.load(new GroovyObjectResource<GroovyTriggerInterface>(script));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (result == null) {
            throw new RuntimeException(new ClassNotFoundException("No class was loaded from " + script));
        }

        return result;
    }
}