package com.funnelback.publicui.test.search.service.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.CartResultDBModel;
import com.funnelback.publicui.search.model.transaction.session.SessionResult;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.service.session.ResultsCartDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class ResultsCartDaoTest extends SessionDaoTest {

    @Autowired
    private ResultsCartRepository repository;
    
    @Override
    public void before() throws Exception {
        for (int i=0; i<5; i++) {
            repository.addToCart(collection.getId(), generateRandomCartResult());
        }
    }
    
    @Test
    public void testGetCart() {
        Calendar c = Calendar.getInstance();
        for (int i=0; i<3; i++) {
            c.add(Calendar.DAY_OF_MONTH, i);
            CartResult cr = new CartResult();
            cr.setAddedDate(c.getTime());
            cr.setCollection(collection.getId());
            cr.setIndexUrl(URI.create("funnelback://test.result/"+i));
            cr.setSummary("Summary #"+i);
            cr.setUserId(user.getId());
            cr.setTitle("Title #"+i);
    
            repository.addToCart(collection.getId(), cr);
        }
        List<CartResult> cart = repository.getCart(user, collection);
        
        assertEquals(3, cart.size());
        CartResult previous = null;
        for (int i=2,j=0; j<3; i--,j++) {
            CartResult cr = cart.get(j);
            assertEquals(user.getId(), cr.getUserId());
            assertEquals(collection.getId(), cr.getCollection());
            assertTrue(cr.getIndexUrl().toString().matches("funnelback://test.result/"+i));
            assertTrue(cr.getSummary().matches("Summary #"+i));
            assertTrue(cr.getTitle().matches("Title #"+i));
            if (previous != null) {
                assertTrue("Should be sorted by descending date", previous.getAddedDate().after(cr.getAddedDate()));
            }
            
            previous = cr;
        }
        
        repository.removeFromCart(user, collection, URI.create("funnelback://test.result/0"));
        repository.removeFromCart(user, collection, URI.create("funnelback://test.result/1"));
        
        cart = repository.getCart(user, collection);
        assertEquals(1, cart.size());
        assertEquals(URI.create("funnelback://test.result/2"), cart.get(0).getIndexUrl());
        
        repository.clearCart(user, collection);
        
        assertEquals(0, repository.getCart(user, collection).size());
    }
    
    @Test
    public void testClearEmptyCart() {
        assertEquals(0, repository.getCart(user, collection).size());
        repository.clearCart(user, collection);
        assertEquals(0, repository.getCart(user, collection).size());
    }
    
    @Test
    public void removeNonExistingEntry() {
        CartResult cr = new CartResult();
        cr.setAddedDate(new Date());
        cr.setCollection(collection.getId());
        cr.setIndexUrl(URI.create("funnelback://test.result/"));
        cr.setSummary("Summary");
        cr.setUserId(user.getId());
        cr.setTitle("Title");

        repository.addToCart(collection.getId(), cr);
        assertEquals(1, repository.getCart(user, collection).size());
        repository.removeFromCart(user, collection, URI.create("http://server.com/file.html"));
        
        List<CartResult> cart = repository.getCart(user, collection);
        assertEquals(1, cart.size());
        assertEquals(URI.create("funnelback://test.result/"), cart.get(0).getIndexUrl());       
    }
    
    @Test
    public void addExistingEntry() {
        CartResult cr = new CartResult();
        cr.setAddedDate(new Date());
        cr.setCollection(collection.getId());
        cr.setIndexUrl(URI.create("funnelback://test.result/"));
        cr.setSummary("Summary");
        cr.setUserId(user.getId());
        cr.setTitle("Title");

        repository.addToCart(collection.getId(), cr);
        repository.addToCart(collection.getId(), cr);
        
        List<CartResult> cart = repository.getCart(user, collection);
        assertEquals(1, cart.size());
        assertEquals(URI.create("funnelback://test.result/"), cart.get(0).getIndexUrl());       
    }
    
    @Test
    public void testSummaryTruncation() {
        CartResult cr = super.generateRandomCartResult();
        cr.setCollection(collection.getId());
        cr.setUserId(user.getId());
        cr.setSummary(new String(new byte[8192]));
        
        repository.addToCart(collection.getId(), cr);
        
        List<CartResult> cart = repository.getCart(user, collection);
        assertEquals(1, cart.size());
        assertEquals(cr.getIndexUrl(), cart.get(0).getIndexUrl());
        assertEquals(SessionResult.MAX_LEN_SUMMARY-1, cart.get(0).getSummary().length());
    }
    
    @Test
    @Transactional  // Needed for lazy initialisation of the metaData collection
    public void testMetadataTruncation() {
        CartResult cr = super.generateRandomCartResult();
        cr.setCollection(collection.getId());
        cr.setUserId(user.getId());
        cr.getMetaData().put("abc", new String(new byte[8192]));
        
        repository.addToCart(collection.getId(), cr);
        
        List<CartResult> cart = repository.getCart(user, collection);
        assertEquals(1, cart.size());
        assertEquals(cr.getIndexUrl(), cart.get(0).getIndexUrl());
        assertEquals(SessionResult.MAX_LEN_METADATA-1, cart.get(0).getMetaData().get("abc").length());
    }
    
    @Test
    public void testPurgeResultsCart() {
        Calendar c = Calendar.getInstance();
        
        CartResult recent = generateRandomCartResult("compcoll", user.getId());
        recent.setAddedDate(c.getTime());
        repository.addToCart(collection.getId(), recent);
        
        c.add(Calendar.DAY_OF_MONTH, -5);
        CartResult older = generateRandomCartResult();
        older.setAddedDate(c.getTime());
        repository.addToCart(collection.getId(), older);
        
        repository.purgeCartResults(2);
        
        List<CartResult> cart = repository.getCart(user, collection);
        assertEquals("Only 1 entry (older) should have been removed", 1, cart.size());
        assertEquals("The recent cart result should be the one remaining", recent.getIndexUrl(), cart.get(0).getIndexUrl());
        assertEquals("compcoll", recent.getCollection());
    }
    
    @Test
    public void fallBackToSearchedCollection() {
        CartResultDBModel dbModel = new CartResultDBModel();
        dbModel.setIndexUrl(URI.create("http://example.com/"));
        dbModel.setTitle("title");
        dbModel.setSummary("summary");
        Collection collection = mock(Collection.class);
        when(collection.getId()).thenReturn("coll");
        CartResult cartResult = new ResultsCartDao().convertFromDB(dbModel, collection);
        Assert.assertEquals("As the collection for the index of the result is unknown we should fall back"
            + " to the collection we are running on.",
            "coll", cartResult.getCollection());
    }
    
}
