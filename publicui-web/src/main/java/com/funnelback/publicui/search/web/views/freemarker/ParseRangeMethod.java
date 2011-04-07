package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.apachecommons.Log;

import com.funnelback.publicui.i18n.I18n;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Parses a range expression (used to build <select>)
 */
@Log
public class ParseRangeMethod implements TemplateMethodModel, TemplateMethodModelEx {

	public static final String NAME = "parseRange";
	
	public static final String START = "start";
	public static final String END = "end";
	
	private static final String CURRENT_YEAR = "CURRENT_YEAR";

	private static final Pattern RANGE_PATTERN = Pattern.compile("^\\s*(\\d+)\\s*\\.\\.\\s*(\\d+)\\s*$");
	private static final Pattern CURRENT_YEAR_OP_PATTERN = Pattern.compile(CURRENT_YEAR + "\\s*(\\+|\\-)\\s*(\\d+)");
	
	
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 1) {
			throw new TemplateModelException(I18n.i18n().tr("This method takes 1 argument: The range expression."));
		}

		HashMap<String, SimpleNumber> map = new HashMap<String, SimpleNumber>();
		String range = ((SimpleScalar) arguments.get(0)).getAsString();
	
		log.debug("Incoming range is '" + range + "'");
		
		int year = Calendar.getInstance().get(Calendar.YEAR);
		Matcher m = CURRENT_YEAR_OP_PATTERN.matcher(range);
		StringBuffer buf = new StringBuffer();
		while (m.find()) {
			if ("+".equals(m.group(1))) {
				m.appendReplacement(buf, Integer.toString(year+Integer.parseInt(m.group(2))));
			} else if ("-".equals(m.group(1))) {
				m.appendReplacement(buf, Integer.toString(year-Integer.parseInt(m.group(2))));
			}
		}
		m.appendTail(buf);
		range = buf.toString();
		
		range = range.replace(CURRENT_YEAR, Integer.toString(year));
		
		log.debug("After " + CURRENT_YEAR + " processing: '" + range + "'");
		
		m = RANGE_PATTERN.matcher(range);
		if (m.find()) {	
			map.put(START, new SimpleNumber(Integer.parseInt(m.group(1))));
			map.put(END, new SimpleNumber(Integer.parseInt(m.group(2))));
		}
		return new SimpleHash(map);
	}

}
