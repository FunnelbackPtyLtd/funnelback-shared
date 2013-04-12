/*
 * COPYRIGHT (C) 2012 Funnelback
 */
package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.security.Principal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.Subject;

import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.plus.jaas.JAASUserPrincipal;

import com.funnelback.jetty.jaas.session.UserKeys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Novell key mapper. Returns set of user and group keys read from Novell
 * eDirectory server.
 *
 * @author cliff
 *
 * $Id:$
 */
@Log4j
public class NovellMapper implements UserKeysMapper {

    /**
     * Get Novell eDirectory user and group keys for current transaction.
     *
     * @param transaction Current search
     * @return List of user and group keys formatted for padre key/lock
     * processing. If none, returns an empty list.
     */
    @Override
    public List<String> getUserKeys(Collection collection, SearchTransaction transaction) {
        List<String> keys = new LinkedList<>();

        Principal principal = transaction.getQuestion().getPrincipal();
        
        /*
         * if principal present extract the user's keys
         */
        if (null != principal) {
            if (principal.getClass().equals(JAASUserPrincipal.class)) {
                JAASUserPrincipal jp = (JAASUserPrincipal) principal;
                Subject subject = jp.getSubject();
                if (null == subject) {
                    throw new NullPointerException("No Subject in JAASUserPrincipal for user: "
                            + principal.getName());
                }

                Iterator<UserKeys> keyLists = subject.getPrivateCredentials(UserKeys.class).iterator();
                while (keyLists.hasNext()) {
                    keys.addAll(keyLists.next().getKeys());
                }
            }

            log.debug("Mapped Novell user '"+principal.getName()+"' with keys '"+StringUtils.join(keys, ","));
        }

        return keys;
    }
}
