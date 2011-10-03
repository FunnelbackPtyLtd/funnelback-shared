package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts contexual navigation data tags:
 * contextual_navigation{...}
 *
 */
@Slf4j
public class ContextualNavigationConversion implements Operation {

	@Override
	public String process(String in) {
		String out = in;
		
		if (out.contains("contextual_navigation{")) {
			log.info("Processing Contextual Navigation data tags");
			
			// Basic data tags
			out = out.replaceAll("contextual_navigation\\{query\\}", "\\${s.contextualNavigation.searchTerm}");
			out = out.replaceAll("contextual_navigation\\{cluster_url\\}", "\\${s.cluster.href}");
			out = out.replaceAll("contextual_navigation\\{cluster_name\\}", "\\${s.cluster.label}");
			
			Matcher m = Pattern.compile("contextual_navigation\\{([^\\=]*)=(\\d+)\\}").matcher(out);
			if (m.find()) {
				out = m.replaceAll("\\${changeParam(s.category.moreLink, \"$1\", \"$2\")}");
			}
		}
		
		return out;
	}

}
