package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.apachecommons.Log;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.web.utils.RequestParametersFilter;

/**
 * Transforms meta_* parameters into query expression.
 * 
 * Doesn't handle date-related ones, they're handled by another
 * {@link InputProcessor}
 *
 */
@Log
@Component("metaParametersInputProcessor")
public class MetaParameters implements InputProcessor {

	/**
	 * Valid types of operators in meta_* parameters.
	 */
	public static enum Operators {
		trunc, orplus, orsand, phrase, prox, or, and, sand, not;
			
		public boolean isPresentIn(String name) {
			return name.contains("_" + toString());
		}
	}
	
	/** Prefix for meta_* parameters */
	private static final String META_PREFIX = "meta_";
	/** Prefix for query_* parameters */
	private static final String QUERY_PREFIX = "query_";
	
	private static final Pattern META_QUERY_PATTERN = Pattern.compile("^(" + META_PREFIX + "|" + QUERY_PREFIX + ").*");

	private static final Pattern META_DATE_PATTERN = Pattern.compile("^" + META_PREFIX + "d(1|2|3|4|)(day|month|year|)$");
	private static final Pattern META_EVENT_PATTERN = Pattern.compile("^" + META_PREFIX + "[wxyz](day|month|year|)$");
	
	/**
	 * Pattern used to extract the metadata class
	 */
	private static final Pattern META_CLASS_PATTERN = Pattern.compile("^" + META_PREFIX + "(.)($|_)");
	
	/**
	 * Pattern used to prefix a value with the metadata class. Taken from the Perl UI:
	 * # This regex has 3 parts.  The first one turns "qwer qwer"
	 * # into a:"qwer qwer".  The second one turns `qwer qwer` into
     * # a:`qwer qwer`.  The third turns qwer into a:qwer
     * # (i.e. single words).
     * # Convert "qwer qwer" to a:"qwer qwer"
	 */
	private static final Pattern ADD_META_CLASS_PATTERN = Pattern.compile("(\"[^\"]*\"?|`[^`]+`?|[a-z0-9\\$]\\S*)", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Used to add non encapsulating type operators (+, |, -)
	 */
	private static final Pattern NON_ENCAPSULATING_OPERATORS_PATTERN = Pattern.compile("([a-z0-9]:)?(\"[^\"]+\"|`[^`]+`|[a-z0-9\\$\\*]\\S+)", Pattern.CASE_INSENSITIVE);
	
	@Override
	public void process(SearchTransaction searchTransaction, HttpServletRequest request) {
		if (searchTransaction != null && searchTransaction.getQuestion() != null && request != null) {
			RequestParametersFilter filter = new RequestParametersFilter(request);
			String[] parameterNames = filter.filter(META_QUERY_PATTERN);

			for (String name: parameterNames) {
				
				// Skip the date components (they are dealt with specially
		        // later: a form may pass through independent
		        // meta_d1_day / meta_d2_month / etc paramaters, to indicate
		        // the start / end day / month / year)
				if (META_DATE_PATTERN.matcher(name).matches()
						|| META_EVENT_PATTERN.matcher(name).matches()) {
					log.debug("Skipping date-related meta parameter '" + name + "'");
					continue;
				}
				
				if (request.getParameterValues(name) != null) {				
					
					// Gather all parameter values
					//     &meta_x=first value&meta_x=second value
					//  => { "first", "value", "second", "value" }
					String stringValues = StringUtils.join(request.getParameterValues(name), " ");
					if ("".equals(stringValues)) {
						// No value for this parameter
						continue;
					}
					
					// Trunc operator (abc def => *abc* *def*)
					if (Operators.trunc.isPresentIn(name)) {
						stringValues = stringValues.replaceAll("(\\S+)", "*$1*");
					}
					
					// Encapsulating operators (" ", [ ], ` `)
					if (Operators.orplus.isPresentIn(name)) {
						stringValues = "+[" + stringValues + "]";
					} else if (Operators.orsand.isPresentIn(name)) {
						stringValues = "|[" + stringValues + "]";
					} else if (Operators.phrase.isPresentIn(name)) {
						stringValues = stringValues.replace("\"", "");
						stringValues = "\"" + stringValues + "\"";
					} else if (Operators.prox.isPresentIn(name)) {
						stringValues = "`" + stringValues + "`";
					} else if (Operators.or.isPresentIn(name)) {
						stringValues = "[" + stringValues + "]";
					}
					
					// Add metadata class
					Matcher m = META_CLASS_PATTERN.matcher(name);
					if (m.find()) {
						String metadataClass = m.group(1);
						stringValues = ADD_META_CLASS_PATTERN.matcher(stringValues).replaceAll(metadataClass + ":$1");
					}
					
					// Non encapsulating operators (+, |, -)
					if (Operators.and.isPresentIn(name)) {
						stringValues = addNonEncapsulatingOperator("+", stringValues);
					} else if (Operators.sand.isPresentIn(name)) {
						stringValues = addNonEncapsulatingOperator("|", stringValues);
					} else if (Operators.not.isPresentIn(name)) {
						stringValues = addNonEncapsulatingOperator("-", stringValues);
					}
					
					searchTransaction.getQuestion().getMetaParameters().add(stringValues);

					// Remove the parameter from the list that will be passed to PADRE if
					// we successfully processed it
					searchTransaction.getQuestion().getAdditionalParameters().remove(name);
					
					log.debug("Processed parameter '" + name + "=" + StringUtils.join(request.getParameterValues(name), " ") + "' "
							+ "Transformed as '" + stringValues + "'");
					
				}
			}
		}
	}

	private String addNonEncapsulatingOperator(String operator, String value) {
		Matcher m = NON_ENCAPSULATING_OPERATORS_PATTERN.matcher(value);
		StringBuffer out = new StringBuffer();
		while (m.find()) {
			out.append(operator);
			if (m.group(1) != null) {
				out.append(m.group(1));
			}
			out.append(m.group(2));
			if (! m.hitEnd()) {
				out.append(" ");
			}
		}
		return out.toString().trim();
	}
	
}
