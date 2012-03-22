package com.funnelback.publicui.search.lifecycle.input.processors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.MapKeyFilter;
import com.funnelback.publicui.utils.MapUtils;

/**
 * Processes meta_* parameters related to date:
 * meta_X_day, meta_X_month, meta_X_year ... with
 * X = d, d[1-4], w[1-2] ...
 */
@Component("metaDatesInputProcessor")
@Log4j
public class MetaDates implements InputProcessor {

	private static final String PREFIX = "meta_";
	private static final String YEAR = "year";
	private static final String MONTH = "month";
	private static final String DAY = "day";
	
	/** Using Apache Commons date formatter. It's thread safe */
	private static final FastDateFormat DATE_FORMATTER = FastDateFormat.getInstance("dMMMyyyy");
	
	/** Parameters for date search */
	private enum Dates {
		d("="), d1(">"), d2("<"), d3(">"), d4("<");

		@Getter
		private String op;

		private Dates(String op) {
			this.op = op;
		}
	}

	/** Parameters for event search */
	private enum EventSearch {
		w1("<"), w2(">");

		@Getter
		private String op;

		private EventSearch(String op) {
			this.op = op;
		}
	}

	private static final String EVENT_SEARCH_OPERATOR = "%";
	private static final String EVENT_SEARCH_MD = "w";

	/**
	 * Pattern used to check if the request parameters contains any parameters
	 * that we'll process
	 */
	private static final Pattern PARAMETERS_PATTERN = Pattern.compile("^" + PREFIX + "("
			+ Dates.d.toString() + "|"
			+ Dates.d1.toString() + "|"
			+ Dates.d2.toString() + "|"
			+ Dates.d3.toString() + "|"
			+ Dates.d4.toString() + "|"
			+ EventSearch.w1.toString() + "|"
			+ EventSearch.w2.toString()
			+ ").*");
	
	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		if (SearchTransactionUtils.hasQuestion(searchTransaction)) {
			Map<String, String> params = searchTransaction.getQuestion().getInputParameterMap();
			MapKeyFilter filter = new MapKeyFilter(params);
			if (filter.filter(PARAMETERS_PATTERN).length > 0) {
				
				for(Dates n: Dates.values()) {
					
					String date;
					if (params.get(PREFIX + n.toString()) != null) {
						// Direct date: meta_d1=20100101
						date = params.get(PREFIX + n.toString()).trim();
					} else {
						// Y/M/D split in 3 different parameters
						
						String year = MapUtils.getString(params, PREFIX + n.toString() + YEAR, "");
						String month = MapUtils.getString(params, PREFIX + n.toString() + MONTH, "");
						String day = MapUtils.getString(params, PREFIX + n.toString() + DAY, "");
						date = day + month + year;
					}
					
					if ("".equals(date)) {
						// No value
						continue;
					}

					StringBuffer extra = new StringBuffer();
					try {
						switch(n) {
						case d3:
							Calendar c = Calendar.getInstance();
							c.setTime(parseDate(date));
							c.add(Calendar.DAY_OF_MONTH, -1);
							date = DATE_FORMATTER.format(c);
							extra.append(Dates.d.toString()).append(n.getOp()).append(date);
							break;
						case d4:
							c = Calendar.getInstance();
							c.setTime(parseDate(date));
							c.add(Calendar.DAY_OF_MONTH, 1);
							date = DATE_FORMATTER.format(c);
							extra.append(Dates.d.toString()).append(n.getOp()).append(date);
							break;
						default:
							extra.append(Dates.d.toString()).append(n.getOp()).append(date);
						}
					} catch (Exception e) {
						log.warn("Unparseable date: '" + date + "'");
					}
					
					if (extra.length() > 0) {
						searchTransaction.getQuestion().getQueryExpressions().add(extra.toString());
						log.debug("Added additional date query '" + extra.toString() + "'");
					}
				}
				String eventSearchQueries = processEventSearch(params);
				if ( eventSearchQueries.length() > 0) {
					searchTransaction.getQuestion().getQueryExpressions().add(eventSearchQueries);
					log.debug("Added additional event search query '" + eventSearchQueries + "'");
				}
			}
		}

	}

	/**
	 * Processes event search (meta_w1 and meta_w2 parameters)
	 * @param request
	 * @return
	 */
	private String processEventSearch(Map<String, String> params) {
		String w1 = MapUtils.getString(params, PREFIX + EventSearch.w1, "");
		String w2 = MapUtils.getString(params, PREFIX + EventSearch.w2, "");
		
		StringBuffer out = new StringBuffer();
		if (!"".equals(w1+w2)) {
			out.append(EVENT_SEARCH_OPERATOR + " " + EVENT_SEARCH_MD);
			if (!"".equals(w1)) {
				out.append(">").append(w1);
			}
			
			if (!"".equals(w2)) {
				out.append("<").append(w2);
			}
		}
		
		return out.toString();
	}

	/**
	 * Parse a date by trying multiple pattern
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public Date parseDate(String date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.setLenient(false);
		for (String pattern: DATE_PATTERNS) {
			sdf.applyPattern(pattern);
			try {
				return sdf.parse(date);
			} catch (Throwable t) { }
		}
		
		throw new ParseException("None of the date patterns matched for '" + date + "'", 0);
	}
	
	/**
	 * List of patterns to use to try to parse dates.
	 * See Perl Date::Parse::str2time for more details.
	 * 
	 * If you add some, please consider the order: Add the more likely ones
	 * to occur at the top to match as soon as possible.
	 */
	private static final String[] DATE_PATTERNS = {
		"yyyyMMdd",
		"ddMMyyyy",
		"yyyy-MM-dd",
		"dd-MMM-yyyy", 
		"dd/MMM/yyyy",
		"ddMMMyy",
		"dd-MMM-yy",
		"dd MMM yyyy",
		
		"MMyyyy",
		"yyyyMM",
		
		"EEE, dd MMM yyyy HH:mm:ss zzz",
		"EEE MMM  d HH:mm:ss zzz yyyy",
		"EEE MMM  d HH:mm:ss yyyy",
		"EEEE, dd-MMM-yy HH:mm:ss zzz",
		"EEEE, dd-MMM-yyyy HH:mm:ss zzz",

		"dd/MMM/yyyy:HH:mm:ss ZZZZZ",
		"dd MMM yyyy HH:mm:ss zzz",
		"dd-MMM-yy HH:mm:ss zzz",
		"dd-MMM-yyyy HH:mm:ss zzz",

		"yyyy-MM-dd HH:mm:ss ZZZZZ", "yyyy-MM-dd HH:mm:ss",
		"yyyy-MM-dd'T'HH:mm:ss", "yyyyMMdd'T'HHmmssz",

		"MMM  d  yyyy", "MMM  d HH:mm",
		"MM-dd-yy  HH:mmaa"
	};	
}


