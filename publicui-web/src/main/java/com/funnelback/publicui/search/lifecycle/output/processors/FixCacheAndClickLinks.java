package com.funnelback.publicui.search.lifecycle.output.processors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Setter;
import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.curator.Curator;
import com.funnelback.publicui.search.model.curator.Exhibit;
import com.funnelback.publicui.search.model.curator.data.UrlAdvert;
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
            }
        }

        // Apply click tracking to best bets links, even if there are no results
        if (SearchTransactionUtils.hasCollection(searchTransaction) && SearchTransactionUtils.hasResultPacket(searchTransaction)) {
            if (searchTransaction.getQuestion().getCollection().getConfiguration().valueAsBoolean(Keys.CLICK_TRACKING)) {
                for (BestBet bb : searchTransaction.getResponse().getResultPacket().getBestBets()) {
                    bb.setClickTrackingUrl(buildClickTrackingUrl(searchTransaction.getQuestion(), bb));
                }
            }
        }

        // Apply click tracking to curator UrlAdvert's, even if there are no results
        if (SearchTransactionUtils.hasCollection(searchTransaction) && SearchTransactionUtils.hasResultPacket(searchTransaction)) {
            if (searchTransaction.getQuestion().getCollection().getConfiguration().valueAsBoolean(Keys.CLICK_TRACKING)) {
                //Curator is shared between all request, we must create a new Curator if we wish to edit any entry.
                List<Exhibit> exhibits = new ArrayList<>();
                for (Exhibit exhibit : searchTransaction.getResponse().getCurator().getExhibits()) {
                    if (exhibit instanceof UrlAdvert) {
                        UrlAdvert advert = (UrlAdvert) exhibit;
                        String newLink = buildClickTrackingUrl(searchTransaction.getQuestion(), advert);
                        //Create a new UrlAdvert as they are shared.
                        exhibits.add(new UrlAdvert(advert.getTitleHtml(), 
                                                        advert.getDisplayUrl(), 
                                                        newLink, 
                                                        advert.getDescriptionHtml(), 
                                                        new HashMap<>(advert.getAdditionalProperties()), 
                                                        advert.getCategory()));
                    } else {
                        exhibits.add(exhibit);
                    }
                }
                Curator curator = new Curator();
                curator.setExhibits(exhibits);
                searchTransaction.getResponse().setCurator(curator);
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
        final StringBuffer out = buildGenericClickTrackingUrl(question, r.getLiveUrl(), r.getIndexUrl());

        out.append("&rank=").append(r.getRank().toString())
            .append("&").append(RequestParameters.QUERY).append("=").append(URLEncoder.encode(queryExpr, "UTF-8"));
        
        if (question.getRawInputParameters().get(PassThroughEnvironmentVariables.Keys.HTTP_REFERER.toString()) != null) {
            out.append("&")
                .append(RequestParameters.Click.SEARCH_REFERER)
                .append("=")
                .append(URLEncoder.encode(MapUtils.getFirstString(question.getRawInputParameters(), PassThroughEnvironmentVariables.Keys.HTTP_REFERER.toString(), null), "UTF-8"));
        }
        
        return out.toString();
    }
    
    /**
     * Builds a click-tracking URL for a best bet
     * @param question
     * @param bb
     * @return
     */
    private String buildClickTrackingUrl(SearchQuestion question, BestBet bb) {
        return buildGenericClickTrackingUrl(question, bb.getLink(), bb.getLink())
            .append("&").append(RequestParameters.Click.TYPE).append("=").append(RequestParameters.Click.TYPE_FP)
            .toString();
    }

    /**
     * Builds a click-tracking URL for a curator UrlAdvert
     */
    private String buildClickTrackingUrl(SearchQuestion question, UrlAdvert advert) {
        return buildGenericClickTrackingUrl(question, advert.getLinkUrl(), advert.getLinkUrl())
            .append("&").append(RequestParameters.Click.TYPE).append("=").append(RequestParameters.Click.TYPE_FP)
            .toString();
    }

    /**
     * Builds a click-tracking URL for a given url, which does not include type-specific parameters (like rank, query and type)
     */
    @SneakyThrows(UnsupportedEncodingException.class)
    private StringBuffer buildGenericClickTrackingUrl(SearchQuestion question, String url, String indexUrl) {
        StringBuffer out = new StringBuffer()
        .append(question.getCollection().getConfiguration().value(Keys.ModernUI.CLICK_LINK))
        .append("?").append(RequestParameters.COLLECTION).append("=").append(question.getCollection().getId())
        .append("&").append(RequestParameters.Click.URL).append("=").append(URLEncoder.encode(url, "UTF-8"))
        .append("&").append(RequestParameters.Click.INDEX_URL).append("=").append(URLEncoder.encode(indexUrl, "UTF-8"))
        .append("&").append(RequestParameters.Click.AUTH).append("=").append(URLEncoder.encode(authTokenManager.getToken(url,question.getCollection().getConfiguration().value(Keys.SERVER_SECRET)), "UTF-8"));

        if (question.getProfile() != null) {
            out.append("&").append(RequestParameters.PROFILE).append("=").append(question.getProfile());
        }
        return out;
    }

}

