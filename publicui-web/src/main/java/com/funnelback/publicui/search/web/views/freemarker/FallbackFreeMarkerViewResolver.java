package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Locale;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

/**
 * <p>Convenience subclass of {@link FreeMarkerViewResolver} that falls back to
 * a default view if the requested view is not found, instead of returning a null
 * view.</p>
 * 
 * <p>Before falling back to the default view it's also able to lookup the view
 * from a global "top level" folder, such as <tt>SEARCH_HOME/conf</tt></p>
 * 
 * <p>It's used when a form file cannot be found to return a decent error page instead
 * of throwing an error 500.</p>
 * 
 *
 */
public class FallbackFreeMarkerViewResolver extends FreeMarkerViewResolver {

	/** URL of the fallback view */
	private final String fallbackViewUrl;
	
	/** Default suffix to try to fallback to */
	private final String defaultSuffix;
	
	/** A global path to search, before falling back to the default view */
	private final String globalPath;
	
	public FallbackFreeMarkerViewResolver(String fallbackViewUrl, String defaultSuffix, String globalPath) {
		this.fallbackViewUrl = fallbackViewUrl;
		this.defaultSuffix = defaultSuffix;
		this.globalPath = globalPath;
	}
	
	@Override
	public View resolveViewName(String viewName, Locale locale) throws Exception {
		View v = super.resolveViewName(viewName, locale);
		if (v == null) {
			v = super.resolveViewName(viewName+defaultSuffix, locale);
			if (v == null) {
				// Try to resolve under the global path
				String inConfUrl = globalPath + viewName.substring(viewName.lastIndexOf('/'));
				v = super.resolveViewName(inConfUrl, locale);
				if (v == null) {
					v = super.resolveViewName(fallbackViewUrl, locale);
					if (v == null) {
						v = super.resolveViewName(fallbackViewUrl+defaultSuffix, locale);
					}
				}
			}
		}

		return v;
	}
	
}
