package com.funnelback.publicui.form.converter.operations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;


/**
 * Try to fix some <#if /> statements
 *
 */
@Log
public class FixIfStatements implements Operation {

	@Override
	public String process(String in) {
	
		String out = in;
		Matcher m = Pattern.compile("<#if\\s+s\\.result\\.date\\?date([^\\s]+)").matcher(in);
		if (m.find()) {
			log.info("Fixing <#if /> statements with dates");
			out = m.replaceAll("<#if s.result.date?exists && s.result.date?date$1");
		}
		
		return out;
	}

}
