package com.funnelback.publicui.search.lifecycle.output.processors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lombok.SneakyThrows;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.BestBet;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.MapUtils;

/**
 * Apply transformation to the click URLs (Results, BestBets...) like
 * adding click tracking URLs.
 */
@Component("fixCacheAndClickLinks")
public class FixCacheAndClickLinks implements OutputProcessor {

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
	private String buildClickTrackingUrl(SearchQuestion question, String queryExpr, Result r) {
		StringBuffer out = new StringBuffer()
			.append(question.getCollection().getConfiguration().value(Keys.ModernUI.CLICK_LINK)).append("?")
			.append("rank=").append(r.getRank().toString())
			.append("&").append(RequestParameters.COLLECTION).append("=").append(question.getCollection().getId())
			.append("&").append(RequestParameters.Click.URL).append("=").append(URLEncoder.encode(r.getLiveUrl(), "UTF-8"))
			.append("&").append(RequestParameters.Click.INDEX_URL).append("=").append(URLEncoder.encode(r.getIndexUrl(), "UTF-8"))
			.append("&").append(RequestParameters.Click.AUTH).append("=").append(URLEncoder.encode(getAuth(r.getLiveUrl(), question.getCollection().getConfiguration().value(Keys.SERVER_SECRET)), "UTF-8"))
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
		.append("&").append(RequestParameters.Click.AUTH).append("=").append(URLEncoder.encode(getAuth(bb.getLink(), question.getCollection().getConfiguration().value(Keys.SERVER_SECRET)), "UTF-8"))
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

