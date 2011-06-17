package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.Log;

import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.common.config.Config;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.config.AbstractLocalConfigRepository;
import com.funnelback.publicui.search.web.controllers.SearchController;

/**
 * <p>{@link FreeMarkerView} that allows Content Type and
 * Headers customisation based on the collection configuration.</p>
 */
@Log
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
						getUrl().lastIndexOf(AbstractLocalConfigRepository.FTL_SUFFIX));
				
				// Search for custom headers
				String propertyPrefix = "publicui.form."+name;
				if (config.hasValue(propertyPrefix+".headers.count")) {
					int nbHeaders = config.valueAsInt(propertyPrefix+".headers.count", 0);
					int sent = 0;
					for (int i=0; i<nbHeaders; i++) {
						String header = config.value(propertyPrefix+".headers."+i, null);
						if (header != null && header.contains(":")) {
							String[] kv = header.split(":");
							response.setHeader(kv[0], kv[1]);
							sent++;
						}
					}
					log.debug("Added " + sent + " custom headers for form '"+name+"'");
				}
				
				// Search for a custom content type
				if (config.hasValue(propertyPrefix+".content_type")) {
					// setContentType(config.value(propertyPrefix+".content_type"));
					response.setContentType(config.value(propertyPrefix+".content_type"));
					log.debug("Set custom Content Type '" + config.value(propertyPrefix+".content_type") + "' for form '"+name+"'");
				}
			}
		}
		
		super.render(model, request, response);
	}	
}
