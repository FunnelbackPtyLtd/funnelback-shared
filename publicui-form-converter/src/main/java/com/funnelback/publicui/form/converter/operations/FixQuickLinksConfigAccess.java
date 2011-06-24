package com.funnelback.publicui.form.converter.operations;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * <p>Try to fix access to quick links configuration.</p>
 * 
 * <p>The conversion transform collection configuration access in
 * a generic fashion, like
 * <code>question.collection.configuration.value("quicklinks...")</code>,
 * however the quicklinks configuration is in a different object:
 * <code>question.collection.quickLinksConfiguration</code> which
 * is a {@link Map} and not a {@link com.funnelback.common.config.Config}</p>
 *
 */
@Log
public class FixQuickLinksConfigAccess implements Operation {

	public static final Pattern CONFIG_PATTERN = Pattern.compile("question.collection.configuration.value\\(\"quicklinks\\.(.*?)\"\\)");
	
	@Override
	public String process(String in) {
		String out = in;
		
		Matcher m = CONFIG_PATTERN.matcher(out);
		if (m.find()) {
			log.info("Fixing quick links configuration accessors");
			out = m.replaceAll("question.collection.quickLinksConfiguration[\"quicklinks.$1\"]");
		}
		
		return out;
	}

}
