package com.funnelback.publicui.search.service;

import com.funnelback.common.profile.ProfileAndView;
import com.funnelback.common.profile.ProfileId;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.utils.web.ProfilePicker;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Log4j2
public class DefaultSampleCollectionUrlService implements SampleCollectionUrlService {

    @Autowired
    @Setter(AccessLevel.PACKAGE)
    SearchTransactionProcessor searchTransactionProcessor;

    @Override public String getSampleUrl(Collection collection, ProfileAndView profileAndView) throws CouldNotFindAnyUrlException {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(collection);
        question.setProfile(profileAndView.asFolderName());
        question.setCurrentProfile(profileAndView.asFolderName());
        question.setQuery("!" + DefaultSampleCollectionUrlService.class.getSimpleName() + "NullQuery");
        question.getDynamicQueryProcessorOptions().add("-num_ranks=1");
        question.getDynamicQueryProcessorOptions().add("-daat=1");
        question.setLogQuery(Optional.ofNullable(false));

        SearchUser user = new SearchUser( DefaultSampleCollectionUrlService.class.getSimpleName());
        SearchTransaction transaction = searchTransactionProcessor.process(question, user, Optional.empty());

        if (!SearchTransactionUtils.hasResults(transaction)) {
            String errorMessage = " - No error message available";
            if (transaction.getError() != null) {
                errorMessage = " - " + transaction.getError().getReason().toString();
            }
            throw new CouldNotFindAnyUrlException("Unable to find sample URL - transaction was missing results" + errorMessage);
        }

        List<Result> results = transaction.getResponse().getResultPacket().getResults();

        if (results.size() < 1) {
            throw new CouldNotFindAnyUrlException("Unable to find sample URL - Zero results found, maybe the collection is empty.");
        }

        return results.get(0).getLiveUrl();
    }
}