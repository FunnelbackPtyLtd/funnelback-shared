package com.funnelback.publicui.search.service;

import com.funnelback.common.profile.ProfileId;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Log4j2
public class DefaultSampleCollectionUrlService implements SampleCollectionUrlService {

    @Autowired
    SearchTransactionProcessor searchTransactionProcessor;

    @Override public String getSampleUrl(Collection collection, ProfileId profile) throws CouldNotFindAnyUrlException {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(collection);
        question.setCurrentProfile(profile.getId());
        question.setQuery("!" + DefaultSampleCollectionUrlService.class.getSimpleName() + "NullQuery");
        question.getDynamicQueryProcessorOptions().add("-num_ranks=1");
        question.getDynamicQueryProcessorOptions().add("-daat=1");
        question.setLogQuery(Optional.ofNullable(false));

        SearchUser user = new SearchUser( DefaultSampleCollectionUrlService.class.getSimpleName());
        SearchTransaction transaction = searchTransactionProcessor.process(question, user, Optional.empty());

        if (!SearchTransactionUtils.hasResults(transaction)) {
            throw new CouldNotFindAnyUrlException("Unable to find sample URL - " +  transaction.getError().getReason());
        }

        List<Result> results = transaction.getResponse().getResultPacket().getResults();

        if (results.size() < 1) {
            throw new CouldNotFindAnyUrlException("Unable to find sample URL - Zero results found, maybe the collection is empty.");
        }

        return results.get(0).getLiveUrl();
    }
}
