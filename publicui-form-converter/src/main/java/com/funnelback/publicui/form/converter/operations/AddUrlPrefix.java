package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Adds /search to URL prefixes, since the new UI doesn't
 * reside in the same context path.
 * Doesn't process absolute URLs (starting with '/'
 *
 */
@Log
public class AddUrlPrefix implements Operation {

	private static final String PREFIX = "/search/";
	
	@Override
	public String process(final String in) {
		log.warn("Using this operation is dangerous because it will hardcode very URL with a '/search' prefix");
		
		Matcher m = Pattern.compile("(href|src)=['\"]([^'\"]*)['\"]").matcher(in);
		if (m.find()) {
			String out = "";
			log.info("Prefixing URLs");
			int start = 0;

			do  {
				log.debug("Found " + m.group(2));

				if (m.group(2).startsWith("/")) {
					log.debug("Ignoring absolute URL '" + m.group(2) + "'");
				} else if (m.group(2).matches("\\w*://.*")) {
					log.debug("Ignoring URL with protocol '" + m.group(2) + "'");
				} else if (m.group(2).startsWith("<") || m.group(2).matches("[a-z_]+\\{[^\\}]*\\}")) {
					log.debug("Ignoring URL containing tag '" + m.group(2) + "'");
				} else {
					log.info("Prefixing URL '" + m.group(2) + "'");
					out += in.substring(start, m.start()) + m.group(1) + "=\"" + PREFIX + m.group(2) + "\"";
					start = m.end();
				}				
			} while	(m.find());
			// Append last part
			out += in.substring(start);
			return out; 
		} else {
			log.info("No URLs to prefix");
			return in;
		}
	}

}
