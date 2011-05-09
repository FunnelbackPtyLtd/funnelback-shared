package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.apachecommons.Log;

import org.apache.commons.lang.time.FastDateFormat;

import com.funnelback.publicui.i18n.I18n;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Parses a relative date expression like &quot;CURRENT_DATE - 1D&quot;
 * @author Administrator
 *
 */
@Log
public class ParseRelativeDateMethod implements TemplateMethodModel, TemplateMethodModelEx {

	public static final String NAME = "parseRelativeDate";
	
	private static final String CURRENT_DATE = "CURRENT_DATE";
	
	private static final Pattern CURRENT_DATE_PATTERN = Pattern.compile("\\s*"+CURRENT_DATE+"\\s*-\\s*(\\d{1,4})([DMY])");
	
	private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("ddMMMyyyy");
	
	@Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		if (arguments.size() != 1) {
			throw new TemplateModelException(I18n.i18n().tr("This method takes 1 argument: The expression to parse."));
		}

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
