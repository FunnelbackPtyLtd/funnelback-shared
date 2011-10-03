package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;


/**
 * Try to fix some <#if /> statements
 *
 */
@Slf4j
public class FixIfStatements implements Operation {

	@Override
	public String process(String in) {
	
		String out = in;
		Matcher m = Pattern.compile("<#if\\s+s\\.result\\.date\\?date([^\\s]+)").matcher(out);
		if (m.find()) {
			log.info("Fixing <#if /> statements with dates");
			out = m.replaceAll("<#if s.result.date?exists && s.result.date?date$1");
		}

		// <#if something!?exists>   TO   <#if something?exists>
		m = Pattern.compile("<#if\\s+(\\S+?)!\\?exists\\s*>").matcher(out);
		if (m.find()) {
			log.info("Fixing <#if /> statements with fallback values");
			out = m.replaceAll("<#if $1?exists>");
		}
		/*
		m = Pattern.compile("<#if\\s+([\\S]+?)!\"\"\\s+").matcher(out);
		if (m.find()) {
			log.info("Fixing <#if /> statements with fallback values");
			out = m.replaceAll("<#if $1 ");
		}
		*/
		
		return out;
	}

}
