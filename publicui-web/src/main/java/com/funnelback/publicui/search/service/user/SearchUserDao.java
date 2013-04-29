package com.funnelback.publicui.search.service.user;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.sqlite.JDBC;

import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.SearchUserRepository;

/**
 * JDBC repository for search users.
 */
@Repository
@Transactional
public class SearchUserDao implements SearchUserRepository {

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public SearchUser getSearchUser(String userId) {
        return em.find(SearchUser.class, userId);
    }

    @Override
    public void createSearchUser(SearchUser user) {
        em.persist(user);
    }

}
