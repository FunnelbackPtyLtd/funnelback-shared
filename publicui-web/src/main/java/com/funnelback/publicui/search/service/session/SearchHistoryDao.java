package com.funnelback.publicui.search.service.session;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

@Repository
public class SearchHistoryDao {

    @PersistenceContext
    private EntityManager em;
    
    public List<SearchHistory> getForUser(SearchUser user) {
        return em.createQuery("from "+SearchHistory.class.getSimpleName()+" where user.id = :user_id",
            SearchHistory.class)
            .setParameter("user_id", user.getId())
            .getResultList();
    }
    
}
