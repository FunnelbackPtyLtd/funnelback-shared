package com.funnelback.publicui.web.controllers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.log.LogUtils;
import com.funnelback.publicui.log.service.LogService;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.web.binding.CollectionEditor;

/**
 * Click tracking controller
 */
@Controller
@Log
public class ClickController {

	@Autowired
	private LogService logService;
	
	@Autowired
	private ConfigRepository configRepository;
	
	@InitBinder
	public void initBinder(DataBinder binder) {
		binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
	}
	
	@RequestMapping(value="/click", method=RequestMethod.GET)
	public void click(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(RequestParameters.COLLECTION) Collection collection,
			@RequestParam(required=false,defaultValue="CLICK") ClickLog.Type type,
			Integer rank,
			@RequestParam(required=false) String profile,
			@RequestParam(RequestParameters.Cache.INDEX_URL) URI indexUrl,
			@RequestParam(value=RequestParameters.Cache.NOATTACHMENT, required=false) String noAttachment) throws IOException {
		
		if (collection != null) {

			String userId = LogUtils.USERID_NOTHING;
			try {
				userId = LogUtils.getUserIdentifier(
					InetAddress.getByName(request.getRemoteAddr()),
					DefaultValues.UserIdToLog.valueOf(collection.getConfiguration().value(Keys.USERID_TO_LOG)));
			} catch (Exception ex) {
				log.warn("Unable to get a user id from adress '"+request.getRemoteAddr()+"', for mode '" + collection.getConfiguration().value(Keys.USERID_TO_LOG) + "'", ex);
			}
			
			URL referer = null;
			if (request.getHeader("referer") != null) {
				try {
					referer = new URL(request.getHeader("referer"));
				} catch (MalformedURLException mue) {
					log.warn("Unable to parse referer '" + request.getHeader("referer") + "'", mue);
				}
			}
			
			logService.logClick(new ClickLog(new Date(), collection, collection.getProfiles().get(profile), userId, referer, rank, indexUrl, type));
			
			response.sendRedirect(indexUrl.toString());
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	// TODO remove
	@ExceptionHandler(Exception.class)
	public void handleException(Exception ex) {
		log.error(ex);
	}
	
}
