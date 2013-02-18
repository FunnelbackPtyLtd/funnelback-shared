package com.funnelback.publicui.search.service.session;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.utils.RedisNamespace;

@Repository
@Log4j
public class RedisSearchHistoryRepository implements SearchHistoryRepository, ApplicationContextAware {

	@Setter private ApplicationContext applicationContext;
	
	private ListOperations<String, SearchHistory> writeSearchListOps;
	private ListOperations<String, SearchHistory> readSearchListOps;

	private ListOperations<String, Result> writeClickListOps;
	private ListOperations<String, Result> readClickListOps;
	
	@Override
	public void saveSearch(SearchUser u, SearchHistory h, Collection c) {
		final String key = RedisNamespace.searchHistoryForUser(c, u);
		
		try {
			writeSearchListOps.leftPush(key, h);
			writeSearchListOps.trim(key, 0,
					c.getConfiguration().valueAsInt(Keys.ModernUI.Session.SEARCH_HISTORY_SIZE,
							DefaultValues.ModernUI.Session.SEARCH_HISTORY_SIZE) -1 );
		} catch (DataAccessException dae) {
			log.error("Couldn't store search history of user '"+u.getId()+"' on collection '"+c.getId()+"'", dae);
		}
	}

	@Override
	public List<SearchHistory> getSearchHistory(SearchUser u, Collection c, int maxEntries) {
		try {
			return readSearchListOps.range(RedisNamespace.searchHistoryForUser(c, u), 0, maxEntries);
		} catch (DataAccessException dae) {
			log.error("Couldn't retrieve search history for user " + u + " on collection '"+c.getId()+"'", dae);
			return new ArrayList<>();
		}
	}
	
	@Override
	public void clearSearchHistory(SearchUser user, Collection collection) {
		try {
			writeSearchListOps.getOperations().delete(RedisNamespace.searchHistoryForUser(collection, user));
		} catch (DataAccessException dae) {
			log.error("Couldn't clear search history for user " + user + " on collection '"+collection.getId()+"'", dae);
		}
	}

	@Override
	public void saveClick(SearchUser u, Result r, Collection c) {
		final String key = RedisNamespace.clickHistoryForUser(c, u);

		try {
			writeClickListOps.leftPush(key, r);
			writeClickListOps.trim(key, 0,
					c.getConfiguration().valueAsInt(Keys.ModernUI.Session.SEARCH_HISTORY_SIZE,
							DefaultValues.ModernUI.Session.SEARCH_HISTORY_SIZE) - 1);
		} catch (DataAccessException dae) {
			log.error("Could not store click for user '"+u.getId()+"' on collection '"+c.getId()+"'", dae);
		}
		
	}

	@Override
	public List<Result> getClickHistory(SearchUser u, Collection c,
			int maxEntries) {
		try {
			return readClickListOps.range(RedisNamespace.clickHistoryForUser(c, u), 0, maxEntries);
		} catch (DataAccessException dae) {
			log.error("Couldn't retrieve click history for user '"+u+"' on collection '"+c.getId()+"'", dae);
			return new ArrayList<>();
		}
	}

	@Override
	public void clearClickHistory(SearchUser user, Collection c) {
		try {
			writeClickListOps.getOperations().delete(RedisNamespace.clickHistoryForUser(c, user));
		} catch (DataAccessException dae) {
			log.error("Couldn't clear click history for user '"+user+"' on collection '"+c.getId()+"'", dae);
		}
	}

	
	/**
	 * Builds helper {@link RedisTemplate}s to read and write hashes of
	 * {@link Result} to Redis
	 */
 	@PostConstruct
	private void createRedisTemplate() {
		RedisTemplate<String, SearchHistory> writeSearchTpl = new RedisTemplate<>();
		writeSearchTpl.setConnectionFactory(applicationContext.getBean("jedisWriteConnectionFactory", RedisConnectionFactory.class));
		writeSearchTpl.setDefaultSerializer(new JacksonJsonRedisSerializer<>(SearchHistory.class));
		writeSearchTpl.setHashKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		writeSearchTpl.setKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		applicationContext.getAutowireCapableBeanFactory().initializeBean(writeSearchTpl, "redisSearchHistoryWriteTemplate");
		writeSearchListOps = writeSearchTpl.opsForList();
		
		RedisTemplate<String, SearchHistory> readSearchTpl = new RedisTemplate<>();
		readSearchTpl.setConnectionFactory(applicationContext.getBean("jedisReadConnectionFactory", RedisConnectionFactory.class));
		readSearchTpl.setDefaultSerializer(new JacksonJsonRedisSerializer<>(SearchHistory.class));
		readSearchTpl.setHashKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		readSearchTpl.setKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		applicationContext.getAutowireCapableBeanFactory().initializeBean(readSearchTpl, "redisSearchHistoryReadTemplate");
		readSearchListOps = readSearchTpl.opsForList();
		
		RedisTemplate<String, Result> writeClickTpl = new RedisTemplate<>();
		writeClickTpl.setConnectionFactory(applicationContext.getBean("jedisWriteConnectionFactory", RedisConnectionFactory.class));
		writeClickTpl.setDefaultSerializer(new JacksonJsonRedisSerializer<>(Result.class));
		writeClickTpl.setHashKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		writeClickTpl.setKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		applicationContext.getAutowireCapableBeanFactory().initializeBean(writeClickTpl, "redisCartWriteTemplate");
		writeClickListOps = writeClickTpl.opsForList();
		
		RedisTemplate<String, Result> readClickTpl = new RedisTemplate<>();
		readClickTpl.setConnectionFactory(applicationContext.getBean("jedisReadConnectionFactory", RedisConnectionFactory.class));
		readClickTpl.setDefaultSerializer(new JacksonJsonRedisSerializer<>(Result.class));
		readClickTpl.setHashKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		readClickTpl.setKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		applicationContext.getAutowireCapableBeanFactory().initializeBean(readClickTpl, "redisCartReadTemplate");
		readClickListOps = readClickTpl.opsForList();
	}


}
