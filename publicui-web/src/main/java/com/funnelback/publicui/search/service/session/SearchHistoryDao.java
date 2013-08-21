package com.funnelback.publicui.search.service.session;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.ClickHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

/**
 * JDBC search history repository
 * 
 * @since 12.5
 */
@Repository
@Transactional
public class SearchHistoryDao implements SearchHistoryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void saveSearch(SearchHistory h) {
        // Only insert new entries
        // CHECKSTYLE:OFF
        try {
            SearchHistory existing = em.createQuery("from "+SearchHistory.class.getSimpleName()
                + " where searchUrlSignature = :signature"
                + " and collection = :collectionId"
                + " and userId = :userId", SearchHistory.class)
                .setParameter("signature", h.getSearchUrlSignature())
                .setParameter("collectionId", h.getCollection())
                .setParameter("userId", h.getUserId())
                .getSingleResult();
            existing.setSearchDate(h.getSearchDate());
            em.persist(existing);
        } catch (NoResultException nre) {
            em.persist(h);
        }
        // CHECKSTYLE:ON
    }

    @Override
    public List<SearchHistory> getSearchHistory(SearchUser u, Collection c, int maxEntries) {
        // CHECKSTYLE:OFF
        return em.createQuery("from "+SearchHistory.class.getSimpleName()
            + " where userId = :userId"
            + " and collection = :collectionId"
            + " order by searchDate desc", SearchHistory.class)
            .setParameter("userId", u.getId())
            .setParameter("collectionId", c.getId())
            .setMaxResults(maxEntries)
            .getResultList();
        // CHECKSTYLE:ON
    }

    @Override
    public void clearSearchHistory(SearchUser user, Collection c) {
        // CHECKSTYLE:OFF
        em.createQuery("delete from "+SearchHistory.class.getSimpleName()
            + " where userId = :userId"
            + " and collection = :collectionId")
            .setParameter("userId", user.getId())
            .setParameter("collectionId", c.getId())
            .executeUpdate();
        // CHECKSTYLE:ON
    }

    @Override
    public void saveClick(ClickHistory h) {
        em.persist(h);
    }

    @Override
    public List<ClickHistory> getClickHistory(SearchUser u, Collection c, int maxEntries) {
        // CHECKSTYLE:OFF
        return em.createQuery("from "+ClickHistory.class.getSimpleName()
            + " where userId = :userId"
            + " and collection = :collectionId"
            + " order by clickDate desc", ClickHistory.class)
            .setParameter("userId", u.getId())
            .setParameter("collectionId", c.getId())
            .setMaxResults(maxEntries)
            .getResultList();
        // CHECKSTYLE:ON
    }

    @Override
    public void clearClickHistory(SearchUser u, Collection c) {
        // CHECKSTYLE:OFF
        em.createQuery("delete from "+ClickHistory.class.getSimpleName()
            + " where userId = :userId"
            + " and collection = :collectionId")
            .setParameter("userId", u.getId())
            .setParameter("collectionId", c.getId())
            .executeUpdate();
        // CHECKSTYLE:ON       
    }
    
}
