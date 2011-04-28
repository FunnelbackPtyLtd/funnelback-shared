package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts s:Compare tags.
 * 
 * Assumes that previous conversions already took place.
 */
@Log
public class CompareConversion implements Operation {

	@Override
	public String process(String in) {
		
		String out = in;
		
		if (out.contains("<s:Compare")) {
			log.info("Processing <s:Compare> tags");
			
			// FIRST: Process tags that compares with == or !=:
			// <s:Compare ${...} == "abc"> or <s:Compare ${...} == 42>

			// Process number comparison
			// <s:Compare ${...} == 42> becomes <s:Compare ... == 42>
			Matcher m = Pattern.compile("<s:Compare\\s+\\$\\{([^\\}]*)\\}\\s+(==|!=)\\s+(\\d+)\\s*>(.*?)</s:Compare>", Pattern.MULTILINE | Pattern.DOTALL).matcher(out);
			if (m.find()) {
				out = m.replaceAll("<#if $1 $2 $3>$4</#if>");
			}

			// Process string comparison
			// <s:Compare ${...} == abc> becomes <s:Compare ... == "abc">
			m = Pattern.compile("<s:Compare\\s+\\$\\{([^\\}]*)\\}\\s+(==|!=)\\s+([^>]+)\\s*>(.*?)</s:Compare>", Pattern.MULTILINE | Pattern.DOTALL).matcher(out);
			if (m.find()) {
				out = m.replaceAll("<#if $1 $2 \"$3\">$4</#if>");
			}
			
			// SECOND: Process regex tags
			// <s:Compare ${...} =~ regex> becomes <#if ...?matches("regex", "r")>
			m = Pattern.compile("<s:Compare\\s+\\$\\{([^\\}]*)\\}\\s+=~\\s+([^>]+)\\s*>(.*?)</s:Compare>", Pattern.MULTILINE | Pattern.DOTALL).matcher(out);
			if (m.find()) {
				out = m.replaceAll("<#if $1?matches(\"$2\", \"r\")>$3</#if>");
			}
			m = Pattern.compile("<s:Compare\\s+\\$\\{([^\\}]*)\\}\\s+!~\\s+([^>]+)\\s*>(.*?)</s:Compare>", Pattern.MULTILINE | Pattern.DOTALL).matcher(out);
			if (m.find()) {
				out = m.replaceAll("<#if ! $1?matches(\"$2\", \"r\")>$3</#if>");
			}
			
			// Fix regexs: Previous perl \b...\b regex must becode \\b...\\b
			out = out.replaceAll("\\\\(\\w)", "\\\\\\\\$1");
			
			// Converts <s:Compare tags that contains <@s.cfg> tags
			m = Pattern.compile("<s:Compare\\s+<@s.cfg>\\s*([^>]*)\\s*</@s.cfg>\\s+(==|!=)\\s+(\\d+)\\s*>(.*?)</s:Compare>", Pattern.MULTILINE | Pattern.DOTALL).matcher(out);
			if (m.find()) {
				out = m.replaceAll("<#if input.collection.configuration.value(\"$1\")?exists && input.collection.configuration.value(\"$1\") $2 $3>$4</#if>");
			}
			m = Pattern.compile("<s:Compare\\s+<@s.cfg>\\s*([^>]*)\\s*</@s.cfg>\\s+(==|!=)\\s+([^>]+)\\s*>(.*?)</s:Compare>", Pattern.MULTILINE | Pattern.DOTALL).matcher(out);
			if (m.find()) {
				out = m.replaceAll("<#if input.collection.configuration.value(\"$1\")?exists && input.collection.configuration.value(\"$1\") $2 \"$3\">$4</#if>");
			}

			m = Pattern.compile("<s:Compare\\s+<@s.cfg>\\s*([^>]*)\\s*</@s.cfg>\\s+=~\\s+([^>]+)\\s*>(.*?)</s:Compare>", Pattern.MULTILINE | Pattern.DOTALL).matcher(out);
			if (m.find()) {
				out = m.replaceAll("<#if input.collection.configuration.value(\"$1\")?exists && input.collection.configuration.value(\"$1\")?matches(\"$2\", \"r\")>$3</#if>");
			}
			m = Pattern.compile("<s:Compare\\s+<@s.cfg>\\s*([^>]*)\\s*</@s.cfg>\\s+!~\\s+([^>]+)\\s*>(.*?)</s:Compare>", Pattern.MULTILINE | Pattern.DOTALL).matcher(out);
			if (m.find()) {
				out = m.replaceAll("<#if input.collection.configuration.value(\"$1\")?exists && ! input.collection.configuration.value(\"$1\")?matches(\"$2\", \"r\")>$3</#if>");
			}



		}
		
		return out;
	}

}
