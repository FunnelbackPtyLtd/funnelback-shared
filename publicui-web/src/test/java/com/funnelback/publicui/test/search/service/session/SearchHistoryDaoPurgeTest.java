package com.funnelback.publicui.test.search.service.session;

import java.util.List;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.model.transaction.session.ClickHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class SearchHistoryDaoPurgeTest extends SessionDaoTest {

    @Autowired
    private SearchHistoryRepository repository;

    @Override
    public void before() throws Exception {
        // NOP
    };
    
    @Test
    public void testPurgeHistory() {
        Calendar c = Calendar.getInstance();
        
        SearchHistory recentSearch = generateRandomSearchHistory(collection.getId(), user.getId());
        recentSearch.setSearchDate(c.getTime());
        repository.saveSearch(recentSearch);

        ClickHistory recentClick = generateRandomClickHistory(collection.getId(), user.getId());
        recentClick.setClickDate(c.getTime());
        repository.saveClick(recentClick);

        c.add(Calendar.DAY_OF_MONTH, -5);
        SearchHistory olderSearch = generateRandomSearchHistory(collection.getId(), user.getId());
        olderSearch.setSearchDate(c.getTime());
        repository.saveSearch(olderSearch);
        
        ClickHistory olderClick = generateRandomClickHistory(collection.getId(), user.getId());
        olderClick.setClickDate(c.getTime());
        repository.saveClick(olderClick);
        
        repository.purgeHistory(2);
        
        List<ClickHistory> clickHistory = repository.getClickHistory(user, collection, Integer.MAX_VALUE);
        Assert.assertEquals("Only 1 click history should remain", 1, clickHistory.size());
        Assert.assertEquals("The recent click should be the one remaining", recentClick.getIndexUrl(), clickHistory.get(0).getIndexUrl());

        List<SearchHistory> searchHistory = repository.getSearchHistory(user, collection, Integer.MAX_VALUE);
        Assert.assertEquals("Only 1 search history should remain", 1, searchHistory.size());
        Assert.assertEquals("The recent search should be the one remaining", recentSearch.getOriginalQuery(), searchHistory.get(0).getOriginalQuery());
    }

}
