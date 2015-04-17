package com.funnelback.publicui.search.service.session;

import java.net.URI;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.model.transaction.session.SessionResultPK;
import com.funnelback.publicui.search.service.ResultsCartRepository;

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
    public void addToCart(CartResult result) {
        SessionResultPK pk = new SessionResultPK(result.getUserId(),
            result.getCollection(), result.getIndexUrl().toString());
        if (em.find(CartResult.class, pk) == null) {
            em.persist(result);
            log.debug("Saved item with URL" +result.getIndexUrl());
        }
    }

    @Override
    public void removeFromCart(SearchUser user, Collection collection, URI uri) {
        // Go through the primary key so that associated metadata
        // get deleted in cascade
        SessionResultPK pk = new SessionResultPK(user.getId(), collection.getId(), uri.toString());
        CartResult cr = em.find(CartResult.class, pk);
        if (cr != null) {
            em.remove(cr);
        }
    }

    @Override
    public void clearCart(SearchUser user, Collection collection) {
        // CHECKSTYLE:OFF
        List<CartResult> cart = em.createQuery("from "+CartResult.class.getSimpleName()
            + " where userId = :userId"
            + " and collection = :collectionId", CartResult.class)
            .setParameter("userId", user.getId())
            .setParameter("collectionId", collection.getId())
            .getResultList();
        // CHECKSTYLE:ON
        
        for (CartResult cr: cart) {
            em.remove(cr);
        }
        
    }

    @Override
    public List<CartResult> getCart(SearchUser user, Collection collection) {
        // CHECKSTYLE:OFF
        return em.createQuery("from "+CartResult.class.getSimpleName()
            + " where userId = :userId"
            + " and collection = :collectionId"
            + " order by addedDate desc", CartResult.class)
            .setParameter("userId", user.getId())
            .setParameter("collectionId", collection.getId())
            .getResultList();
        // CHECKSTYLE:ON
    }

}
