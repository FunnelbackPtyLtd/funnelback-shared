package com.funnelback.publicui.search.service.session;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import java.net.URI;
import java.text.SimpleDateFormat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.CartResultDBModel;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.model.transaction.session.SessionResultPK;
import com.funnelback.publicui.search.service.ResultsCartRepository;

import lombok.extern.log4j.Log4j2;

/**
 * JDBC repository for results cart
 * 
 * @since 12.5
 */
@Repository
@Transactional
@Log4j2
public class ResultsCartDao implements ResultsCartRepository {

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public void addToCart(String collectionToStoreTheCartUnder, CartResult result) {
        SessionResultPK pk = new SessionResultPK(result.getUserId(),
            collectionToStoreTheCartUnder, result.getIndexUrl().toString());
        if (em.find(CartResultDBModel.class, pk) == null) {
            em.persist(CartResultDBModel.fromResult(collectionToStoreTheCartUnder, result));
            log.debug("Saved item with URL" +result.getIndexUrl());
        }
    }

    @Override
    public void removeFromCart(SearchUser user, Collection collection, URI uri) {
        // Go through the primary key so that associated metadata
        // get deleted in cascade
        SessionResultPK pk = new SessionResultPK(user.getId(), collection.getId(), uri.toString());
        CartResultDBModel cr = em.find(CartResultDBModel.class, pk);
        if (cr != null) {
            em.remove(cr);
        }
    }

    @Override
    public void clearCart(SearchUser user, Collection collection) {
        // CHECKSTYLE:OFF
        List<CartResultDBModel> cart = em.createQuery("from "+CartResultDBModel.TABLE_NAME
            + " where userId = :userId"
            + " and collection = :collectionId", CartResultDBModel.class)
            .setParameter("userId", user.getId())
            .setParameter("collectionId", collection.getId())
            .getResultList();
        // CHECKSTYLE:ON
        
        for (CartResultDBModel cr: cart) {
            em.remove(cr);
        }
        
    }

    @Override
    public List<CartResult> getCart(SearchUser user, Collection collection) {
        // CHECKSTYLE:OFF
        return em.createQuery("from "+CartResultDBModel.TABLE_NAME
            + " where userId = :userId"
            + " and collection = :collectionId"
            + " order by addedDate desc", CartResultDBModel.class)
            .setParameter("userId", user.getId())
            .setParameter("collectionId", collection.getId())
            .getResultList()
            .stream()
            .map(dbmodel -> convertFromDB(dbmodel, collection))
            .collect(Collectors.toList());
        // CHECKSTYLE:ON
    }
    
    public CartResult convertFromDB(CartResultDBModel dbmodel, Collection collection) {
        CartResult result = CartResult.from(dbmodel);
        // If we don't know the collection from which the result was made 
        // (result was added before the upgrade) we just assume the collection
        // on which the search is run.
        if(result.getCollection() == null) {
            result.setCollection(collection.getId());
        }
        return result;
    }
    
    @Override
    public int purgeCartResults(int daysToKeep) {
        Calendar expiredDate = Calendar.getInstance();
        expiredDate.add(Calendar.DAY_OF_MONTH, -daysToKeep);

        AtomicInteger removed = new AtomicInteger(0);
        
        em.createQuery("from " + CartResultDBModel.TABLE_NAME
            + " where addedDate < :date", CartResultDBModel.class)
            .setParameter("date", expiredDate.getTime())
            .getResultList()
            .stream()
            .forEach(result -> { 
                em.remove(result);
                removed.incrementAndGet();
            });
        
        log.debug("Purged {} carts results older than {}", removed.get(), new SimpleDateFormat().format(expiredDate.getTime()));
        
        return removed.get();
    };
}
