package com.funnelback.publicui.search.lifecycle.output.processors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lombok.Setter;
import lombok.SneakyThrows;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.BestBet;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

/**
 * Apply transformation to the cache and click URLs (Results, BestBets...)
 * <ul>
 * 	<li>Adds a prefix configured in the global app.properties (can be empty)</li>
 * 	<li>Generates click tracking URLs</li>
 * </ul>
 *
 */
@Component("fixCacheAndClickLinks")
public class FixCacheAndClickLinks implements OutputProcessor {

	@Value("#{appProperties['urls.search.prefix']}")
	@Setter private String searchUrlPrefix;
	
	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasQueryAndCollection(searchTransaction)
				&& SearchTransactionUtils.hasResults(searchTransaction)) {
			for (Result r: searchTransaction.getResponse().getResultPacket().getResults()) {
				r.setCacheUrl(searchUrlPrefix + r.getCacheUrl());
				if (searchTransaction.getQuestion().getCollection().getConfiguration().valueAsBoolean(Keys.CLICK_TRACKING)) {
					r.setClickTrackingUrl(buildClickTrackingUrl(searchTransaction.getQuestion(), r));
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
	
	/**
	 * Generates a click tracking URL with all the required parameters for a result.
	 * @param question
	 * @param r
	 * @return
	 */
	@SneakyThrows(UnsupportedEncodingException.class)
	private String buildClickTrackingUrl(SearchQuestion question, Result r) {
		StringBuffer out = new StringBuffer(searchUrlPrefix)
			.append(question.getCollection().getConfiguration().value(Keys.UI_CLICK_LINK)).append("?")
			.append("rank=").append(r.getRank().toString())
			.append("&").append(RequestParameters.COLLECTION).append("=").append(r.getCollection())
			.append("&").append(RequestParameters.Click.URL).append("=").append(URLEncoder.encode(r.getLiveUrl(), "UTF-8"))
			.append("&").append(RequestParameters.Click.INDEX_URL).append("=").append(URLEncoder.encode(r.getLiveUrl(), "UTF-8"))
			.append("&").append(RequestParameters.Click.AUTH).append("=").append(getAuth(r.getLiveUrl(), question.getCollection().getConfiguration().value(Keys.SERVER_SECRET)))
			.append("&").append(RequestParameters.QUERY).append("=").append(question.getQuery());
		
		if (question.getProfile() != null) {
			out.append("&").append(RequestParameters.PROFILE).append("=").append(question.getProfile());
		}
		
		if (question.getReferer() != null) {
			out.append("&").append(RequestParameters.Click.SEARCH_REFERER).append("=").append(URLEncoder.encode(question.getReferer(), "UTF-8"));
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
		StringBuffer out = new StringBuffer(searchUrlPrefix)
		.append(question.getCollection().getConfiguration().value(Keys.UI_CLICK_LINK)).append("?")
		.append("&").append(RequestParameters.COLLECTION).append("=").append(question.getCollection().getId())
		.append("&").append(RequestParameters.Click.URL).append("=").append(URLEncoder.encode(bb.getLink(), "UTF-8"))
		.append("&").append(RequestParameters.Click.AUTH).append("=").append(getAuth(bb.getLink(), question.getCollection().getConfiguration().value(Keys.SERVER_SECRET)))
		.append("&").append(RequestParameters.Click.TYPE).append("=").append(RequestParameters.Click.TYPE_FP);

		if (question.getProfile() != null) {
			out.append("&").append(RequestParameters.PROFILE).append("=").append(question.getProfile());
		}
	
		return out.toString();
	}
	
	/**
	 * Generates an auth String to prevent click URLs forgery.
	 * @param url
	 * @param serverSecret
	 * @return
	 */
	private String getAuth(String url, String serverSecret) {
		// FIXME The Perl UI expects non standard Base64 encoded
		// (22 bytes long instead of a multiple of 4, see the doco
		// for Perl Digest::MD5::md5_base64()
		return StringUtils.removeEnd(new String(Base64.encodeBase64(DigestUtils.md5(serverSecret+url))), "==");
	}

}

