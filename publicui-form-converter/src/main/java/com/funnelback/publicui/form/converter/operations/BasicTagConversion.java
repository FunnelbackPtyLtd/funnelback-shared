package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Basic 1 for 1 replacement of tags that have their exact
 * counter-part in new syntax.
 */
@Log
public class BasicTagConversion implements Operation {

	private static final String[] TAGS = {
		"AfterSearchOnly", "InitialFormOnly", "cfg", "OpenSearch",
		"PrevNext", "cgi", "CheckSpelling", "BestBets", "Results",
		"cut", "Explore", "Quicklinks", "QuickRepeat", "FacetedSearch", "ContextualNavigation",
		"NoClustersFound", "ClusterLayout", "Category", "Clusters", "ShowMoreClusters",
		"ShowFewerClusters"		
	};
	
	@Override
	public String process(final String in) {
		String out = in;
		for(String tag: TAGS) {
			Pattern p = Pattern.compile("<(/?)s:" + tag + "([^>]*)>");
			Matcher m = p.matcher(out);
			if (m.find()) {
				log.info("Processing tag '" + tag + "'");
				out = m.replaceAll("<$1@s." + tag + "$2>");
			} else {
				log.info("No tag '" + tag + "' found.");
			}
		}
	
		return out;		
	}

}
