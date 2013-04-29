package com.funnelback.publicui.search.service.session;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

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
        em.persist(h);
    }

    @Override
    public List<SearchHistory> getSearchHistory(SearchUser u, Collection c, int maxEntries) {
        return em.createQuery("from "+SearchHistory.class.getSimpleName()
            + " where user.id = :userId"
            + " and collection = :collectionId"
            + " order by searchDate desc", SearchHistory.class)
            .setParameter("userId", u.getId())
            .setParameter("collectionId", c.getId())
            .setMaxResults(maxEntries)
            .getResultList();
    }

    @Override
    public void clearSearchHistory(SearchUser user, Collection c) {
        em.createQuery("delete from "+SearchHistory.class.getSimpleName()
            + " where user.id = :userId"
            + " and collection = :collectionId")
            .setParameter("userId", user.getId())
            .setParameter("collectionId", c.getId())
            .executeUpdate();
    }

    @Override
    public void saveClick(ClickHistory h) {
        em.persist(h);
    }

    @Override
    public List<ClickHistory> getClickHistory(SearchUser u, Collection c, int maxEntries) {
        return em.createQuery("from "+ClickHistory.class.getSimpleName()
            + " where user.id = :userId"
            + " and collection = :collectionId"
            + " order by clickDate desc", ClickHistory.class)
            .setParameter("userId", u.getId())
            .setParameter("collectionId", c.getId())
            .setMaxResults(maxEntries)
            .getResultList();
    }

    @Override
    public void clearClickHistory(SearchUser u, Collection c) {
        em.createQuery("delete from "+ClickHistory.class.getSimpleName()
            + " where user.id = :userId"
            + " and collection = :collectionId")
            .setParameter("userId", u.getId())
            .setParameter("collectionId", c.getId())
            .executeUpdate();
        
    }
    
}
