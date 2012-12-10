package com.funnelback.publicui.search.web.controllers;

import groovy.xml.XmlUtil;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.Xml;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.common.io.store.Store.View;
import com.funnelback.common.io.store.StoreType;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.binding.CollectionEditor;

/**
 * Deal with cached copies
 */
@Controller
@Log4j
public class CacheController {

	@Autowired
	private DataRepository dataRepository;
	
	@Autowired
	private ConfigRepository configRepository;
	
	@InitBinder
	public void initBinder(DataBinder binder) {
		binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
	}

	@RequestMapping(value="/cache", method=RequestMethod.GET)
	public ModelAndView cache(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(RequestParameters.COLLECTION) Collection collection,
			@RequestParam String url) throws Exception {
		
		if (cachedCopiesDisabled(collection.getConfiguration()) || url == null) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			
			return new ModelAndView(DefaultValues.FOLDER_WEB+"/"
					+DefaultValues.FOLDER_TEMPLATES+"/"
					+DefaultValues.FOLDER_MODERNUI+"/cached-copy-unavailable",
					new HashMap<String, Object>());
		} else {
			Store<? extends Record> store = StoreType.getStore(collection.getConfiguration(), View.live);
			try {
				store.open();
				Record r = store.get(url);
				
				if (r != null) {
					if (r instanceof RawBytesRecord) {
						RawBytesRecord bytesRecord = (RawBytesRecord) r;
						response.getOutputStream().write(bytesRecord.getContent());
						return null;
					} else if (r instanceof XmlRecord) {
						XmlRecord xmlRecord = (XmlRecord) r;
						response.getWriter().write(Xml.toString(xmlRecord.getContent()));
						return null;
					} else {
						throw new UnsupportedOperationException("Unknown record type '"+r.getClass()+"'");
					}
					
				} else {
					// Record not found
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return null;
				}
			} finally {
				store.close();
			}
		}
	}
	
	/**
	 * Test if cached copies are available or not depending of the
	 * collection configuration
	 * @param c
	 * @return
	 */
	private boolean cachedCopiesDisabled(Config c) {
		return c.valueAsBoolean(Keys.UI_CACHE_DISABLED)
				|| c.hasValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER)
				|| ( c.hasValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE)
						&& Config.isTrue(c.value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE)));
	}
	
}
