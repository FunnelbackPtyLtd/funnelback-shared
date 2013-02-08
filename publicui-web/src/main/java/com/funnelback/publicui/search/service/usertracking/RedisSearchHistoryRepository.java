package com.funnelback.publicui.search.service.usertracking;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Repository;

import com.funnelback.publicui.search.model.transaction.usertracking.SearchHistory;
import com.funnelback.publicui.search.model.transaction.usertracking.SearchUser;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

@Repository
public class RedisSearchHistoryRepository implements SearchHistoryRepository {

	@Resource(name="redisWriteTemplate")
	private ListOperations<String, SearchHistory> writeListOps;

	@Resource(name="redisReadTemplate")
	private ListOperations<String, SearchHistory> readListOps;
	
	@Override
	public void save(SearchUser u, SearchHistory h) {
		writeListOps.leftPush(RedisNamespace.searchHistoryForUser(u), h);
	}

	@Override
	public List<SearchHistory> get(SearchUser u, int maxEntries) {
		return readListOps.range(RedisNamespace.searchHistoryForUser(u), 0, maxEntries);
	}


}
