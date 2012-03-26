package com.funnelback.publicui.search.web.controllers;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.binding.CollectionEditor;

/**
 * Deal with cached copies
 */
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
			String doc,
			String url) throws Exception {
		
		if (cachedCopiesDisabled(collection.getConfiguration()) || doc == null) {
			return new ModelAndView(DefaultValues.FOLDER_WEB+"/"
					+DefaultValues.FOLDER_TEMPLATES+"/"
					+DefaultValues.FOLDER_MODERNUI+"/cached-copy-unavailable",
					new HashMap<String, Object>());
		} else {		
			String content = dataRepository.getCachedDocument(collection, doc);
			if (content != null) {
				// TODO For now response as text/plain
				response.setContentType("text/plain");
				response.getWriter().write(content);
			} else {
				// Cached copy unavailable
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
			return null;
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
