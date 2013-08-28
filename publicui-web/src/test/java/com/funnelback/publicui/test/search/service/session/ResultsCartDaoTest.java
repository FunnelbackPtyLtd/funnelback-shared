package com.funnelback.publicui.test.search.service.session;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ResultsCartRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class ResultsCartDaoTest extends SessionDaoTest {

    @Autowired
    private ResultsCartRepository repository;
    
    @Override
    public void before() throws Exception {
        for (int i=0; i<5; i++) {
            repository.addToCart(generateRandomCartResult());
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
    
            repository.addToCart(cr);
        }
        List<CartResult> cart = repository.getCart(user, collection);
        
        assertEquals(3, cart.size());
        CartResult previous = null;
        for (CartResult cr: cart) {
            assertEquals(user.getId(), cr.getUserId());
            assertEquals(collection.getId(), cr.getCollection());
            assertTrue(cr.getIndexUrl().toString().matches("funnelback://test.result/[0-2]"));
            assertTrue(cr.getSummary().matches("Summary #[0-2]"));
            assertTrue(cr.getTitle().matches("Title #[0-2]"));
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

        repository.addToCart(cr);
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

        repository.addToCart(cr);
        repository.addToCart(cr);
        
        List<CartResult> cart = repository.getCart(user, collection);
        assertEquals(1, cart.size());
        assertEquals(URI.create("funnelback://test.result/"), cart.get(0).getIndexUrl());       
    }
    
}
