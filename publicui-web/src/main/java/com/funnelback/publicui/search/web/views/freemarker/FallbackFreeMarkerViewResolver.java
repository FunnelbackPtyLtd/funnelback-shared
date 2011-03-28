package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Locale;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

/**
 * Convenience subclass of {@link FreeMarkerViewResolver} that falls back to
 * a default view if the requested view is not found, instead of returning a null
 * view.
 * 
 * It's used when a form file cannot be found to return a decent error page instead
 * of throwing an error 500.
 *
 */
public class FallbackFreeMarkerViewResolver extends FreeMarkerViewResolver {

	private View fallbackView;
	
	public FallbackFreeMarkerViewResolver(View fallbackView) {
		this.fallbackView = fallbackView;
	}
	
	@Override
	public View resolveViewName(String viewName, Locale locale) throws Exception {
		View v = super.resolveViewName(viewName, locale);
		if (v == null) {
			return fallbackView;
		} else {
			return v;
		}
	}
	
}
