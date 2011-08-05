package com.funnelback.publicui.search.web.views.json;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

/**
 * <p>Subclass of {@link MappingJacksonJsonView} that implements support
 * for <a href="http://en.wikipedia.org/wiki/JSONP">JSON-P</a> padding.</p>
 * 
 * <p>The callback name should be provided as a request parameter.</p>
 */
public class MappingJacksonJsonpView extends MappingJacksonJsonView {

	@Setter private String callbackParameterName = "callback";
	
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		String callback = request.getParameter(callbackParameterName);
		if (callback != null && ! "".equals(callback)) {
			response.getOutputStream().write(callback.getBytes());
			response.getOutputStream().write("(".getBytes());
		}
		
		super.renderMergedOutputModel(model, request, response);
		
		if (callback != null && ! "".equals(callback)) {
			response.getOutputStream().write(")".getBytes());
		}
	}
}
