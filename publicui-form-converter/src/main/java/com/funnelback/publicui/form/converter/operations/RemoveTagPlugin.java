package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Removes tagging plugins. Tagging is not supported (yet)
 * in the Java UI so tags are removed for now.
 */
@Slf4j
public class RemoveTagPlugin implements Operation {

	private static final Pattern TAG_LINK_PATTERN = Pattern.compile("([\\s\\t]*)?<s:ResPlugin\\s+name=['\"]ResultTagLink['\"]\\s*>.*?</s:ResPlugin>([\\s\\t]*)?", Pattern.DOTALL);
	private static final Pattern TAG_LIST_PATTERN = Pattern.compile("([\\s\\t]*)?<s:ResPlugin\\s+name=['\"]ResultTagList['\"]\\s*>.*?</s:ResPlugin>([\\s\\t]*)?", Pattern.DOTALL);
	
	@Override
	public String process(String in) {
		
		String out = in;

		Matcher m = TAG_LINK_PATTERN.matcher(out);
		if (m.find()) {
			out = m.replaceAll("");
			log.warn("Unsupported tagging link plugin tag(s) ('<s:ResultTagLink>') have been removed.");
		}

		m = TAG_LIST_PATTERN.matcher(out);
		if (m.find()) {
			out = m.replaceAll("");
			log.warn("Unsupported tagging list plugin tag(s) ('<s:ResultTagList>') have been removed.");
		}
				
		return out;
	}

}
