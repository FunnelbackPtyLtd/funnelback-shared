package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.time.FastDateFormat;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Parses a relative date expression like &quot;CURRENT_DATE - 1D&quot;
 *
 */
@Log4j
public class ParseRelativeDateMethod extends AbstractTemplateMethod {

	public static final String NAME = "parseRelativeDate";
	
	private static final String CURRENT_DATE = "CURRENT_DATE";
	
	private static final Pattern CURRENT_DATE_PATTERN = Pattern.compile("\\s*"+CURRENT_DATE+"\\s*-\\s*(\\d{1,4})([DMY])");
	
	private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("ddMMMyyyy");
	
	public ParseRelativeDateMethod() {
		super(1, 0, false);
	}
	
	@Override
	public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		String range = ((TemplateScalarModel) arguments.get(0)).getAsString();
		String out = range;
		
		Matcher m = CURRENT_DATE_PATTERN.matcher(range);
		if (m.find()) {
			Calendar c = Calendar.getInstance();
			int multiplier = Integer.parseInt(m.group(1));
			char quantifier = m.group(2).charAt(0);
			switch (quantifier) {
			case 'Y':
				c.add(Calendar.YEAR, -multiplier);
				break;
			case 'M':
				c.add(Calendar.MONTH, -multiplier);
				break;
			case 'D':
				c.add(Calendar.DAY_OF_MONTH, -multiplier);
				break;
			}
			out = m.replaceAll(DATE_FORMAT.format(c));			
		}
		
		if (out.contains(CURRENT_DATE)) {
			out = out.replace(CURRENT_DATE, DATE_FORMAT.format(new Date()));
		}

		if (! out.equals(range)) {
			log.debug("Parsed input range '" + range + "' to '" + out + "'");
		}
		return out;
	}

}
