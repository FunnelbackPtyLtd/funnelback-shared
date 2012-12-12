package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.search.web.controllers.SearchController;

/**
 * <p>{@link FreeMarkerView} that allows Content Type and
 * Headers customisation based on the collection configuration.</p>
 */
@Log4j
public class CustomisableFreeMarkerFormView extends FreeMarkerView {

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (getUrl() != null
				&& model != null) {

			customiseOutput(getUrl(), model, response);
		}

		super.render(model, request, response);
	}

	/**
	 * Customise the output by sending custom headers and/or content type for
	 * search or cache requests, depending of the collection configuration and the form
	 * requested.
	 * 
	 * @param url
	 * @param model
	 * @param response
	 */
	protected void customiseOutput(String url, Map<String, ?> model, HttpServletResponse response) {
		// Find the form name by inspecting the last part of the URL,
		// like 'csv' in 'conf/<collection/<profile/csv.ftl'
		String name = url.substring(
				url.lastIndexOf('/')+1,
				url.lastIndexOf(DefaultConfigRepository.FTL_SUFFIX));

		Config config = null;
		String settingPrefix = null;
		
		if (model.containsKey(SearchController.ModelAttributes.question.toString())) {
			// Model contains a SearchQuestion object, it's a search request
			SearchQuestion q = (SearchQuestion) model.get(SearchController.ModelAttributes.question.toString());
			if (q.getCollection() != null && q.getCollection().getConfiguration() != null) {
				config = q.getCollection().getConfiguration();
				settingPrefix = Keys.ModernUI.FORM_PREFIX;
			}
		} else if (model.containsKey(RequestParameters.COLLECTION)
				&& model.get(RequestParameters.COLLECTION) instanceof Collection) {
			// Model contains a Collection object, possibly a cache request
			Collection c = (Collection) model.get(RequestParameters.COLLECTION);
			if (c.getConfiguration() != null) {
				config = c.getConfiguration();
				settingPrefix = Keys.ModernUI.Cache.FORM_PREFIX;
				// Strip off ".cache" suffix from form name
				name = name.substring(0, name.lastIndexOf(DefaultValues.CACHE_FORM_SUFFIX));
			}
		}
		
		if (config != null && settingPrefix != null) {
			
			setCustomHeaders(settingPrefix, name, config, response);
			setCustomContentType(settingPrefix, name, config, response);
		}
	}
	
	/**
	 * Sets custom HTTP headers, if configured in the collection's configuration.
	 * @param settingPrefix Prefix to use to read the setting in collection.cfg
	 * @param tplName Name of the current form, without extension.
	 * @param config Collection configuration.
	 * @param response
	 */
	private void setCustomHeaders(String settingPrefix, String tplName, Config config, HttpServletResponse response) {
		// Search for custom headers
		String propertyPrefix = settingPrefix + "." + tplName;
		if (config.hasValue(propertyPrefix+"."+Keys.ModernUI.HEADERS_COUNT_SUFFIX)) {
			int nbHeaders = config.valueAsInt(propertyPrefix+"."+Keys.ModernUI.HEADERS_COUNT_SUFFIX, 0);
			int sent = 0;
			for (int i=0; i<nbHeaders; i++) {
				String header = config.value(propertyPrefix+"."+Keys.ModernUI.HEADERS_SUFFIX+"."+(i+1), null);
				if (header != null && header.contains(":")) {
					String[] kv = header.split(":");
					response.setHeader(kv[0].trim(), kv[1].trim());
					sent++;
				}
			}
			log.debug("Added " + sent + " custom headers for form '"+tplName+"'");
		}
	}
	
	/**
	 * Sets a custom content type, if configured in the collection's configuration
	 * @param settingPrefix Prefix to use to read the setting in collection.cfg
	 * @param tplName Name of the current form, without extension.
	 * @param config Collection configuration.
	 * @param response
	 */
	private void setCustomContentType(String settingPrefix, String tplName, Config config, HttpServletResponse response) {
		// Search for a custom content type
		String propertyPrefix = settingPrefix + "." + tplName;
		if (config.hasValue(propertyPrefix+"."+Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX)) {
			// setContentType(config.value(propertyPrefix+".content_type"));
			response.setContentType(config.value(propertyPrefix+"."+Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX));
			log.debug("Set custom Content Type '"
					+ config.value(propertyPrefix+"."+Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX)
					+ "' for form '"+tplName+"'");
		}

	}
}
