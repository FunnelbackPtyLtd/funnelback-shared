package com.funnelback.publicui.search.service.session;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.utils.RedisNamespace;

@Component
@Log4j
public class RedisResultsCartRepository implements ResultsCartRepository, ApplicationContextAware {

    @Setter private ApplicationContext applicationContext;
    
    private HashOperations<String, String, Result> writeHashOps;
    private HashOperations<String, String, Result> readHashOps;

    @Override
    public void addToCart(SearchUser user, Collection collection,
            Result result, String query) {
        try {
            writeHashOps.put(RedisNamespace.resultsCartForUser(collection, user),
                    result.getIndexUrl(), result);
        } catch (DataAccessException dae) {
            log.error("Error while adding to cart", dae);
        }
    }

    @Override
    public void removeFromCart(SearchUser user, Collection collection,
            String url) {
        try {
            writeHashOps.delete(RedisNamespace.resultsCartForUser(collection, user), url);
        } catch (DataAccessException dae) {
            log.error("Error while deleting from cart", dae);
        }
    }
    
    @Override
    public void clearCart(SearchUser user, Collection collection) {
        try {
            writeHashOps.getOperations().delete(RedisNamespace.resultsCartForUser(collection, user));
        } catch (DataAccessException dae) {
            log.error("Error while clearing cart", dae);
        }
    }
    
    @Override
    public Map<String, Result> getCart(SearchUser user, Collection collection) {
        try {
            return readHashOps.entries(RedisNamespace.resultsCartForUser(collection, user));
        } catch (DataAccessException dae) {
            log.error("Error retrieving cart", dae);
            return new HashMap<String, Result>();
        }
    }
    
    /**
     * Builds helper {@link RedisTemplate}s to read and write hashes of
     * {@link Result} to Redis
     */
    @PostConstruct
    private void createRedisTemplate() {
        RedisTemplate<String, Result> writeTpl = new RedisTemplate<>();
        writeTpl.setConnectionFactory(applicationContext.getBean("jedisWriteConnectionFactory", RedisConnectionFactory.class));
        writeTpl.setDefaultSerializer(new JacksonJsonRedisSerializer<>(Result.class));
        writeTpl.setHashKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
        writeTpl.setKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
        applicationContext.getAutowireCapableBeanFactory().initializeBean(writeTpl, "redisCartWriteTemplate");
        writeHashOps = writeTpl.opsForHash();
        
        RedisTemplate<String, Result> readTpl = new RedisTemplate<>();
        readTpl.setConnectionFactory(applicationContext.getBean("jedisReadConnectionFactory", RedisConnectionFactory.class));
        readTpl.setDefaultSerializer(new JacksonJsonRedisSerializer<>(Result.class));
        readTpl.setHashKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
        readTpl.setKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
        applicationContext.getAutowireCapableBeanFactory().initializeBean(readTpl, "redisCartReadTemplate");
        readHashOps = readTpl.opsForHash();
    }

}
