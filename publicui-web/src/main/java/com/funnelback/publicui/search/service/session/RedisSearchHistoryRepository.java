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
	
	private ListOperations<String, SearchHistory> writeListOps;
	private ListOperations<String, SearchHistory> readListOps;
	
	@Override
	public void saveHistory(SearchUser u, SearchHistory h, Collection c) {
		final String key = RedisNamespace.searchHistoryForUser(c, u);
		
		try {
			writeListOps.leftPush(key, h);
			writeListOps.trim(key, 0,
					c.getConfiguration().valueAsInt(Keys.ModernUI.Session.SEARCH_HISTORY_SIZE,
							DefaultValues.ModernUI.Session.SEARCH_HISTORY_SIZE) -1 );
		} catch (DataAccessException dae) {
			log.error("Couldn't store user search history of user '"+u.getId()+"' on collection '"+c.getId()+"'", dae);
		}
	}

	@Override
	public List<SearchHistory> getHistory(SearchUser u, Collection c, int maxEntries) {
		try {
			return readListOps.range(RedisNamespace.searchHistoryForUser(c, u), 0, maxEntries);
		} catch (DataAccessException dae) {
			log.error("Couldn't retrieve search history for user " + u + " on collection '"+c.getId()+"'", dae);
			return new ArrayList<SearchHistory>();
		}
	}
	
	@Override
	public void clearHistory(SearchUser user, Collection collection) {
		writeListOps.getOperations().delete(RedisNamespace.searchHistoryForUser(collection, user));		
	}

	/**
	 * Builds helper {@link RedisTemplate}s to read and write hashes of
	 * {@link Result} to Redis
	 */
 	@PostConstruct
	private void createRedisTemplate() {
		RedisTemplate<String, SearchHistory> writeTpl = new RedisTemplate<>();
		writeTpl.setConnectionFactory(applicationContext.getBean("jedisWriteConnectionFactory", RedisConnectionFactory.class));
		writeTpl.setDefaultSerializer(new JacksonJsonRedisSerializer<>(SearchHistory.class));
		writeTpl.setHashKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		writeTpl.setKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		applicationContext.getAutowireCapableBeanFactory().initializeBean(writeTpl, "redisCartWriteTemplate");
		writeListOps = writeTpl.opsForList();
		
		RedisTemplate<String, SearchHistory> readTpl = new RedisTemplate<>();
		readTpl.setConnectionFactory(applicationContext.getBean("jedisReadConnectionFactory", RedisConnectionFactory.class));
		readTpl.setDefaultSerializer(new JacksonJsonRedisSerializer<>(SearchHistory.class));
		readTpl.setHashKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		readTpl.setKeySerializer(applicationContext.getBean("stringRedisSerializer", StringRedisSerializer.class));
		applicationContext.getAutowireCapableBeanFactory().initializeBean(readTpl, "redisCartReadTemplate");
		readListOps = readTpl.opsForList();
	}


}
