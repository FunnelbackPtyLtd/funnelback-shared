package com.funnelback.publicui.search.lifecycle.output.processors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lombok.Setter;
import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.BestBet;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.auth.AuthTokenManager;
import com.funnelback.publicui.utils.MapUtils;

/**
 * Apply transformation to the click URLs (Results, BestBets...) like
 * adding click tracking URLs.
 */
@Component("fixCacheAndClickLinks")
public class FixCacheAndClickLinks extends AbstractOutputProcessor {

    @Autowired @Setter
    private AuthTokenManager authTokenManager;
    
    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && SearchTransactionUtils.hasResults(searchTransaction)) {
            
            // FUN-5038: We must use the full query expression here, not just
            // the user-entered one
            String q = new PadreQueryStringBuilder(searchTransaction.getQuestion(), true).buildCompleteQuery();
            if (q.length() > 0) {
            
                for (Result r: searchTransaction.getResponse().getResultPacket().getResults()) {
                    if (searchTransaction.getQuestion().getCollection().getConfiguration().valueAsBoolean(Keys.CLICK_TRACKING)) {
                        r.setClickTrackingUrl(buildClickTrackingUrl(searchTransaction.getQuestion(), q, r));
                    } else {
                        r.setClickTrackingUrl(r.getLiveUrl());
                    }
                }
                
                if (searchTransaction.getQuestion().getCollection().getConfiguration().valueAsBoolean(Keys.CLICK_TRACKING)) {
                    // Apply click tracking to best bets links
                    for (BestBet bb : searchTransaction.getResponse().getResultPacket().getBestBets()) {
                        bb.setClickTrackingUrl(buildClickTrackingUrl(searchTransaction.getQuestion(), bb));
                    }
                }
            }
        }
    }
    
    /**
     * Generates a click tracking URL with all the required parameters for a result.
     * @param question
     * @param r
     * @return
     */
    @SneakyThrows(UnsupportedEncodingException.class)
    private String buildClickTrackingUrl(SearchQuestion question, String queryExpr, final Result r) {
        final StringBuffer out = new StringBuffer()
            .append(question.getCollection().getConfiguration().value(Keys.ModernUI.CLICK_LINK)).append("?")
            .append("rank=").append(r.getRank().toString())
            .append("&").append(RequestParameters.COLLECTION).append("=").append(question.getCollection().getId())
            .append("&").append(RequestParameters.Click.URL).append("=").append(URLEncoder.encode(r.getLiveUrl(), "UTF-8"))
            .append("&").append(RequestParameters.Click.INDEX_URL).append("=").append(URLEncoder.encode(r.getIndexUrl(), "UTF-8"))
            .append("&").append(RequestParameters.Click.AUTH).append("=").append(URLEncoder.encode(authTokenManager.getToken(r.getLiveUrl(),question.getCollection().getConfiguration().value(Keys.SERVER_SECRET)), "UTF-8"))
            .append("&").append(RequestParameters.QUERY).append("=").append(queryExpr);
        
        if (question.getProfile() != null) {
            out.append("&").append(RequestParameters.PROFILE).append("=").append(question.getProfile());
        }
        
        if (question.getRawInputParameters().get(PassThroughEnvironmentVariables.Keys.HTTP_REFERER.toString()) != null) {
            out.append("&")
                .append(RequestParameters.Click.SEARCH_REFERER)
                .append("=")
                .append(URLEncoder.encode(MapUtils.getFirstString(question.getRawInputParameters(), PassThroughEnvironmentVariables.Keys.HTTP_REFERER.toString(), null), "UTF-8"));
        }
        
        if (question.getCollection().getConfiguration().valueAsBoolean(
                Keys.ModernUI.SESSION, DefaultValues.ModernUI.SESSION)) {
            // Add parameters to build a Result object that will be saved in the
            // user click history. Only add fields we need.
            out.append("&").append(RequestParameters.Click.Result.INDEX_URL)
                .append("=").append(URLEncoder.encode(r.getIndexUrl(), "UTF-8"))
                .append("&").append(RequestParameters.Click.Result.LIVE_URL)
                .append("=").append(URLEncoder.encode(r.getLiveUrl(), "UTF-8"))
                .append("&").append(RequestParameters.Click.Result.TITLE)
                .append("=").append(URLEncoder.encode(r.getTitle(), "UTF-8"))
                .append("&").append(RequestParameters.Click.Result.SUMMARY)
                .append("=").append(URLEncoder.encode(r.getSummary(), "UTF-8"));
        }
        
        return out.toString();
    }
    
    /**
     * Builds a click-tracking URL for a best bet
     * @param question
     * @param bb
     * @return
     */
    @SneakyThrows(UnsupportedEncodingException.class)
    private String buildClickTrackingUrl(SearchQuestion question, BestBet bb) {
        StringBuffer out = new StringBuffer()
        .append(question.getCollection().getConfiguration().value(Keys.ModernUI.CLICK_LINK)).append("?")
        .append("&").append(RequestParameters.COLLECTION).append("=").append(question.getCollection().getId())
        .append("&").append(RequestParameters.Click.URL).append("=").append(URLEncoder.encode(bb.getLink(), "UTF-8"))
        .append("&").append(RequestParameters.Click.AUTH).append("=").append(URLEncoder.encode(authTokenManager.getToken(bb.getLink(),question.getCollection().getConfiguration().value(Keys.SERVER_SECRET)), "UTF-8"))
        .append("&").append(RequestParameters.Click.TYPE).append("=").append(RequestParameters.Click.TYPE_FP);

        if (question.getProfile() != null) {
            out.append("&").append(RequestParameters.PROFILE).append("=").append(question.getProfile());
        }
    
        return out.toString();
    }

}

