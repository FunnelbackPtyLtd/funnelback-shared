package com.funnelback.publicui.search.web.setup;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TimeLimitedScope implements Scope {

    private Cache<String, Object> cache;
    
    public TimeLimitedScope(long objectLifetimeSeconds) {
        cache = CacheBuilder.newBuilder()
            .expireAfterWrite(objectLifetimeSeconds, TimeUnit.SECONDS)
            .build();
    }
    
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        try {
            return cache.get(name, () -> {
                log.trace("Building a new " + name + " bean");
                return objectFactory.getObject();
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object remove(String name) {
        Object result = cache.getIfPresent(name);
        cache.invalidate(name);
        return result;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        log.error("Destruction callbacks not supported.");
    }

    @Override
    public Object resolveContextualObject(String key) {
        // I think based on http://bit.ly/2nlDgMu#L96 we can just not implement this.
        return null;
    }

    @Override
    public String getConversationId() {
        return TimeLimitedScope.class.getSimpleName();
    }

}
