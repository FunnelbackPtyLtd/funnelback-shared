package com.funnelback.publicui.test.search.service.session;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.ClickHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * <p>Base class for tests that need to interact with the session database.</p>
 * 
 * <p>Will create a fresh in-memory database and cleanup after the test.</p>
 */
public abstract class SessionDaoTest {
    
    @Autowired
    protected DataSource dataSource;
    
    protected SearchUser user;
    protected Collection collection;

    
    @Before
    public void setupDatabase() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            Liquibase lb = new Liquibase(
                "com/funnelback/publicui/search/model/transaction/session/session-db.changelog-master.xml",
                new ClassLoaderResourceAccessor(),
                new JdbcConnection(c));
            lb.dropAll();
            lb.update(null);
        }
        
        user = new SearchUser(UUID.randomUUID().toString());
        collection = new Collection("dummy", new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), "dummy"));
        
        before();
    }
    
    /**
     * Method to run before each test. Will be called
     * after the database has been setup
     */
    public abstract void before() throws Exception;
    
    protected CartResult generateRandomCartResult() {
        return generateRandomCartResult(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }
    
    protected CartResult generateRandomCartResult(String collectionId, String userId) {
        CartResult cr = new CartResult();
        cr.setAddedDate(new Date());
        cr.setCollection(collectionId);
        cr.setIndexUrl(URI.create("urn:uuid:"+UUID.randomUUID().toString()));
        cr.setSummary(UUID.randomUUID().toString());
        cr.setUserId(userId);
        cr.setTitle(UUID.randomUUID().toString());
        
        return cr;
    }
    
    protected ClickHistory generateRandomClickHistory() {
        return generateRandomClickHistory(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }
    protected ClickHistory generateRandomClickHistory(String collectionId, String userId) {
        ClickHistory ch = new ClickHistory();
        ch.setClickDate(new Date());
        ch.setCollection(collectionId);
        ch.setIndexUrl(URI.create("urn:uuid:"+UUID.randomUUID().toString()));
        ch.setSummary(UUID.randomUUID().toString());
        ch.setTitle(UUID.randomUUID().toString());
        ch.setUserId(userId);
        ch.setQuery(UUID.randomUUID().toString());
        
        return ch;
    }

    protected SearchHistory generateRandomSearchHistory() {
        return generateRandomSearchHistory(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }
    protected SearchHistory generateRandomSearchHistory(String collectionId, String userId) {
        Random r = new Random();
        SearchHistory sh = new SearchHistory();
        sh.setCollection(collectionId);
        sh.setCurrStart(r.nextInt());
        sh.setNumRanks(r.nextInt());
        sh.setOriginalQuery(UUID.randomUUID().toString());
        sh.setQueryAsProcessed(UUID.randomUUID().toString());
        sh.setSearchDate(new Date());
        sh.setSearchParams("&param1="+UUID.randomUUID().toString()+"&param2="+r.nextInt());
        sh.setTotalMatching(r.nextInt());
        sh.setUserId(userId);
        
        return sh;
    }
    
    
}
