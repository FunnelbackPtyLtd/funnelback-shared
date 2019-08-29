package com.funnelback.publicui.search.model.curator.config;

import java.util.function.Consumer;

import com.funnelback.publicui.search.model.curator.HasNoBeans;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AutowireCuratorConfigurer implements Configurer {

    private final Consumer<Object> autoWireFunction;

    @Override
    public void configure(Object objectToConfigure) {
        if(!(objectToConfigure instanceof HasNoBeans)) {
            autoWireFunction.accept(objectToConfigure);
        }
    }
    
    
}
