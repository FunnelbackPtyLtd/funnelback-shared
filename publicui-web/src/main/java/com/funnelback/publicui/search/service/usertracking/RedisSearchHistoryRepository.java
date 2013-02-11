package com.funnelback.publicui.search.service.usertracking;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.log4j.Log4j;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Repository;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.usertracking.SearchHistory;
import com.funnelback.publicui.search.model.transaction.usertracking.SearchUser;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

@Repository
@Log4j
public class RedisSearchHistoryRepository implements SearchHistoryRepository {

	@Resource(name="redisWriteTemplate")
	private ListOperations<String, SearchHistory> writeListOps;

	@Resource(name="redisReadTemplate")
	private ListOperations<String, SearchHistory> readListOps;
	
	@Override
	public void save(SearchUser u, SearchHistory h, Collection c) {
		final String key = RedisNamespace.searchHistoryForUser(c, u);
		
		try {
			writeListOps.leftPush(key, h);
			writeListOps.trim(key, 0,
					c.getConfiguration().valueAsInt(Keys.ModernUI.UserTracking.SEARCH_HISTORY_SIZE,
							DefaultValues.ModernUI.UserTracking.SEARCH_HISTORY_SIZE) -1 );
		} catch (DataAccessException dae) {
			log.error("Couldn't store user search history of user '"+u.getId()+"' on collection '"+c.getId()+"'", dae);
		}
	}

	@Override
	public List<SearchHistory> get(SearchUser u, Collection c, int maxEntries) {
		try {
			return readListOps.range(RedisNamespace.searchHistoryForUser(c, u), 0, maxEntries);
		} catch (DataAccessException dae) {
			log.error("Couldn't retrieve search history for user " + u + " on collection '"+c.getId()+"'", dae);
			return new ArrayList<SearchHistory>();
		}
	}


}
