package com.funnelback.publicui.search.service.session;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.net.URI;
import java.util.List;

/**
 * JDBC repository for results cart
 * 
 * @since 12.5
 */
@Repository
@Transactional
@Log4j
public class ResultsCartDao implements ResultsCartRepository {

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public void addToCart(CartResult result) {
        try {
            CartResult existing = em.createQuery("from "+CartResult.class.getSimpleName()
                    + " where indexUrl = :uri"
                    + " and user.id = :userId"
                    + " and collection = :collectionId", CartResult.class)
                    .setParameter("uri", result.getIndexUrl().toString())
                    .setParameter("userId", result.getUser().getId())
                    .setParameter("collectionId", result.getCollection())
                    .getSingleResult();

            // Do nothing, result already in cart
            log.debug("Existing item" + existing);
        } catch (NoResultException nre) {
            em.persist(result);
            log.debug("Saved item with URL" +result.getIndexUrl());
        }
    }

    @Override
    public void removeFromCart(SearchUser user, Collection collection, URI uri) {
        // CHECKSTYLE:OFF
        Query q = em.createQuery("delete from " + CartResult.class.getSimpleName()
                + " where indexUrl = :uri"
                + " and user.id = :userId"
                + " and collection = :collectionId")
            .setParameter("uri", uri.toString())
            .setParameter("userId", user.getId())
            .setParameter("collectionId", collection.getId());

        log.debug("Query: " + q);
        int deleted = q.executeUpdate();
        log.debug(deleted + " rows deleted for URI " + uri);
        // CHECKSTYLE:ON
    }

    @Override
    public void clearCart(SearchUser user, Collection collection) {
        // CHECKSTYLE:OFF
        em.createQuery("delete from "+CartResult.class.getSimpleName()
            + " where user.id = :userId"
            + " and collection = :collectionId")
            .setParameter("userId", user.getId())
            .setParameter("collectionId", collection.getId())
            .executeUpdate();
        // CHECKSTYLE:ON
    }

    @Override
    public List<CartResult> getCart(SearchUser user, Collection collection) {
        // CHECKSTYLE:OFF
        return em.createQuery("from "+CartResult.class.getSimpleName()
            + " where user.id = :userId"
            + " and collection = :collectionId"
            + " order by addedDate desc", CartResult.class)
            .setParameter("userId", user.getId())
            .setParameter("collectionId", collection.getId())
            .getResultList();
        // CHECKSTYLE:ON
    }

}
