/*
 * COPYRIGHT (C) 2012 Funnelback
 */
package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;


import com.funnelback.jetty.jaas.session.UserKeys;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import java.security.Principal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.security.auth.Subject;
import org.eclipse.jetty.plus.jaas.JAASUserPrincipal;

/**
 * Novell key mapper.  Returns set of user and group keys read from Novell
 * eDirectory server.
 * 
 * @author cliff
 *
 * $Id:$
 */
public class NovellMapper implements UserKeysMapper {
    
    public NovellMapper() {
    }
    
    /**
     * Get Novell eDirectory user and group keys for current transaction.
     * 
     * @returns List of user and group keys formatted for padre key/lock processing.
     *          If none, returns an empty list. 
     */
    @Override
    public List<String> getUserKeys(SearchTransaction transaction) {
        Principal principal = transaction.getQuestion().getPrincipal();
        List<String> keys = new LinkedList<>();
        if (null == principal) {
            throw new NullPointerException("Null principal in search transaction: "+
                    transaction.toString());
        }
        
        if (principal.getClass().equals(JAASUserPrincipal.class)) {
            JAASUserPrincipal jp = (JAASUserPrincipal) principal;
            Subject subject = jp.getSubject();
            if (null == subject) {
                throw new NullPointerException("No Subject in JAASUserPrincipal for user: "+
                        principal.getName());
            }
            
            Iterator keyLists = subject.getPrivateCredentials(UserKeys.class).iterator();
            while (keyLists.hasNext()) {
                keys.addAll(((UserKeys) keyLists.next()).getKeys());
            }   
        }
        
        /*
         * uncomment to confirm correct user id and groups supplied
         */
//        Iterator key = keys.iterator();
//        while( key.hasNext()) {
//            System.out.println("Novell user key: "+key.next());
//        }

        return keys;
    }

}
