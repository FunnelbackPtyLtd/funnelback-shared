package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.apachecommons.Log;

import com.funnelback.publicui.form.converter.Operation;

@Log
public class ResultsTierBarsConversion implements Operation {

	private static final String DEFAULT_TABLE_ATTR = "'bgcolor=\"#CCCCCC\" summary=\"Result header\" width=\"100%\" cellpadding=4 cellspacing=0";
	private static final String DEFAULT_FULLY_HTML = "<p><table "+DEFAULT_TABLE_ATTR+"><tr><td><b>Fully matching documents</b></td></tr></table></p>";
	private static final String DEFAULT_PARTIALLY_HTML = "<p><table "+DEFAULT_TABLE_ATTR+"><tr><td><b>Documents matching ${s.result.matched} out of ${s.result.outOf} search constraints</b></td></tr></table></p>";
	
	@Override
	public String process(String in) {
		String out = in;

		out = out.replace("tier{num_constraints}", "${s.result.matched}");
		out = out.replace("tier{constraints_matching}", "${s.result.outOf}");
		
		// Try to capture TierBar HTML fragments
		String fullyMatchingHtml = DEFAULT_FULLY_HTML;
		Matcher m = Pattern.compile("<s:TierBarFullyMatching>(.*?)</s:TierBarFullyMatching>", Pattern.DOTALL).matcher(out);
		if (m.find()) {
			fullyMatchingHtml = m.group(1);	
			// Remove old tier bar tag
			out = m.replaceAll("");
		}

		String partiallyMatchingHtml = DEFAULT_PARTIALLY_HTML;
		m = Pattern.compile("<s:TierBarPartiallyMatching>(.*?)</s:TierBarPartiallyMatching>", Pattern.DOTALL).matcher(out);
		if (m.find()) {
			partiallyMatchingHtml = m.group(1);	
			// Remove old tier bar tag
			out = m.replaceAll("");
		}
		
		if (in.contains("<s:Results>")) {
			log.info("Processing <s:Results> tags");
			
			// The first group tries to preserve any indentation
			out = out.replaceAll("\\n?([\\s\\t]*)?<s:Results>", "\n$1<@s.Results>\n"
					+ "$1<#if s.result.class.simpleName == \"TierBar\">\n"
					+ "$1\t<#if s.result.matched == s.result.outOf>\n"
					+ "$1\t\t" + fullyMatchingHtml.replace("$", "\\$") + "\n"
					+ "$1\t<#else>\n"
					+ "$1\t\t" + partiallyMatchingHtml.replace("$", "\\$") + "\n"
					+ "$1\t</#if>\n"
					+ "$1<#else>\n");
			
			out = out.replaceAll("\\n?([\\s\\t]*)?</s:Results>", "\n$1</#if>\n$1</@s.Results>");			
		}
		
		return out;
	}

}
