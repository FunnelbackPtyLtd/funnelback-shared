package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
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
				&& model != null
				&& model.containsKey(SearchController.ModelAttributes.question.toString())) {
			SearchQuestion q = (SearchQuestion) model.get(SearchController.ModelAttributes.question.toString());
			if (q.getCollection() != null && q.getCollection().getConfiguration() != null) {
				Config config = q.getCollection().getConfiguration();
				
				// Find the form name by inspecting the last part of the URL,
				// like 'csv' in 'conf/<collection/<profile/csv.ftl'
				String name = getUrl().substring(
						getUrl().lastIndexOf('/')+1,
						getUrl().lastIndexOf(DefaultConfigRepository.FTL_SUFFIX));
				
				setCustomHeaders(name, config, response);
				setCustomContentType(name, config, response);				
			}
		}
		
		super.render(model, request, response);
	}
	
	/**
	 * Sets custom HTTP headers, if configured in the collection's configuration.
	 * @param tplName Name of the current form, without extension.
	 * @param config Collection configuration.
	 * @param response
	 */
	protected void setCustomHeaders(String tplName, Config config, HttpServletResponse response) {
		// Search for custom headers
		String propertyPrefix = Keys.ModernUI.FORM_PREFIX+"."+tplName;
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
	 * @param tplName Name fo the current form, without extension.
	 * @param config Collection configuration.
	 * @param response
	 */
	protected void setCustomContentType(String tplName, Config config, HttpServletResponse response) {
		// Search for a custom content type
		String propertyPrefix = Keys.ModernUI.FORM_PREFIX+"."+tplName;
		if (config.hasValue(propertyPrefix+"."+Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX)) {
			// setContentType(config.value(propertyPrefix+".content_type"));
			response.setContentType(config.value(propertyPrefix+"."+Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX));
			log.debug("Set custom Content Type '"
					+ config.value(propertyPrefix+"."+Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX)
					+ "' for form '"+tplName+"'");
		}

	}
}
