package com.funnelback.publicui.search.service;

import com.funnelback.common.profile.ProfileId;
import com.funnelback.publicui.search.model.collection.Collection;

public interface SampleCollectionUrlService {

    public String getSampleUrl(Collection collection, ProfileId profile) throws CouldNotFindAnyUrlException;

    public class CouldNotFindAnyUrlException extends Exception {
        public CouldNotFindAnyUrlException(String message) {
            super(message);
        }
    }

}
