package com.funnelback.publicui.search.lifecycle.output.processors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lombok.SneakyThrows;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Apply transformation to the cache and click URLs.
 * <ul>
 * 	<li>Adds a prefix configured in the global app.properties (can be empty)</li>
 * 	<li>Generates click tracking URLs</li>
 * </ul>
 *
 */
@Component("fixCacheAndClickLinks")
public class FixCacheAndClickLinks implements OutputProcessor {

	@Value("#{appProperties['urls.search.prefix']}")
	private String searchUrlPrefix;
	
	@Override
	public void process(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasQueryAndCollection(searchTransaction)
				&& SearchTransactionUtils.hasResults(searchTransaction)) {
			for (Result r: searchTransaction.getResponse().getResultPacket().getResults()) {
				r.setCacheUrl(searchUrlPrefix + r.getCacheUrl());
				if (searchTransaction.getQuestion().getCollection().getConfiguration().valueAsBoolean(Keys.CLICK_TRACKING)) {
					r.setClickTrackingUrl(buildClickTrackingUrl(searchTransaction.getQuestion(), r));
				}
			}
		}
	}
	
	/**
	 * Generates a click tracking URL with all the required parameters.
	 * @param question
	 * @param r
	 * @return
	 */
	@SneakyThrows(UnsupportedEncodingException.class)
	private String buildClickTrackingUrl(SearchQuestion question, Result r) {
		StringBuffer out = new StringBuffer(searchUrlPrefix)
			.append(question.getCollection().getConfiguration().value(Keys.UI_CLICK_LINK)).append("?")
			.append("rank=").append(r.getRank().toString())
			.append("&collection=").append(r.getCollection())
			.append("&url=").append(URLEncoder.encode(r.getLiveUrl(), "UTF-8"))
			.append("&index_url=").append(URLEncoder.encode(r.getLiveUrl(), "UTF-8"))
			.append("&auth=").append(getAuth(r.getLiveUrl(), question.getCollection().getConfiguration().value(Keys.SERVER_SECRET)))
			.append("&search_referer=").append(URLEncoder.encode(question.getReferer(), "UTF-8"))
			.append("&query=").append(question.getQuery())
			.append("&profile=").append(question.getProfile());
		
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

