package com.funnelback.publicui.test.search.service.session;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.util.Date;
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
        CartResult cr = new CartResult();
        cr.setAddedDate(new Date());
        cr.setCollection(UUID.randomUUID().toString());
        cr.setIndexUrl(URI.create("url:uuid:"+UUID.randomUUID().toString()));
        cr.setSummary(UUID.randomUUID().toString());
        cr.setUserId(UUID.randomUUID().toString());
        cr.setTitle(UUID.randomUUID().toString());
        
        return cr;
    }
    
}
