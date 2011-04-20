package com.funnelback.publicui.form.converter.operations;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts "old old" tags, as a pre-process step
 */
public class LegacyTagsConversion implements Operation {

	private static final String[] FROM = {
		"s:TierBarFeaturedPages",
		"s:FeaturedPages",
		"s:Fluster",
		"res\\{(.*?)\\}",
		"(?s)resif\\{(.*?)\\}\\{(.*?)\\}",
		"(?s)resifnot\\{(.*?)\\}\\{(.*?)\\}",
		"(?s)resifcollection\\{(.*?)\\}\\{(.*?)\\}",
		"(?s)resifnotcollection\\{(.*?)\\}\\{(.*?)\\}"
	};
	
	private static final String[] TO = {
		"s:TierBarBestBets",
		"s:BestBets",
		"s:ContextualNavigation",
		"<s:Res>$1</s:Res>",
		"<s:ResIf name=\"$1\">$2</s:ResIf>",
		"<s:ResIfNot name=\"$1\">$2</s:ResIfNot>",
		"<s:ResIfCollection name=\"$1\">$2</s:ResIfCollection>",
		"<s:ResIfNotCollection name=\"$1\">$2</s:ResIfNotCollection>"
	};
	
	@Override
	public String process(String in) {
		String out = in;
		
		for (int i=0; i<FROM.length; i++) {
			out = out.replaceAll(FROM[i], TO[i]);
		}
		
		return out;
	}

}
