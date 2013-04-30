package com.funnelback.publicui.search.service.session;

import java.net.URI;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ResultsCartRepository;

/**
 * JDBC repository for results cart
 * 
 * @since 12.5
 */
@Repository
@Transactional
public class ResultsCartDao implements ResultsCartRepository {

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public void addToCart(CartResult result) {
        em.persist(result);
    }

    @Override
    public void removeFromCart(SearchUser user, Collection collection, URI uri) {
        // CHECKSTYLE:OFF
        em.createQuery("delete from "+CartResult.class.getSimpleName()
            + " where indexUrl = :uri"
            + " and user.id = :userId"
            + " and collection = :collectionId")
            .setParameter("uri", uri)
            .setParameter("userId", user.getId())
            .setParameter("collectionId", collection.getId())
            .executeUpdate();
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
