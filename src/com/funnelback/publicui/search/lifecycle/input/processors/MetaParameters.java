package com.funnelback.publicui.search.lifecycle.input.processors;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.apachecommons.Log;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

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
		addmeta, trunc, orplus, orsand, phrase, prox, or, and, sand, not;
		
		/**
		 * @param operation
		 * @return true if the operation is valid (known), false otherwise.
		 */
		public static boolean isValid(String operator) {
			for (Operators op : Operators.values()) {
				if (op.toString().equals(operator)) {
					return true;
				}
			}
			return false;
		}
	}
	
	/** Prefix for meta_* parameters */
	private static final String META_PREFIX = "meta_";
	/** Prefix for query_* parameters */
	private static final String QUERY_PREFIX = "query_";
	
	/**
	 * Internal operation name to use when the parameter is a simple
	 * meta_x=<value>, in order to use the same logic as meta_x_<operation>=value.
	 * 
	 * In this case we just add a 'x:<value>' expression.
	 */
	private static final String INTERNAL_OPERATOR_ADDMETA = Operators.addmeta.toString();
	
	private static final Pattern META_ID_PATTERN = Pattern.compile(META_PREFIX + "(\\w)_(.*)");
	
	@Override
	public void process(SearchTransaction searchTransaction, HttpServletRequest request) {
		if (searchTransaction != null && searchTransaction.getQuestion() != null && request != null) {
			@SuppressWarnings("unchecked")
			Enumeration<String> names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				if (request.getParameterValues(name) != null
						&& (name.startsWith(META_PREFIX) || name.startsWith(QUERY_PREFIX))) {				
					
					// Gather all parameter values
					//     &meta_x=first value&meta_x=second value
					//  => { "first", "value", "second", "value" }
					String stringValues = StringUtils.join(request.getParameterValues(name), " ");
					if ("".equals(stringValues)) {
						// No value for this parameter
						continue;
					}
					String[] values = stringValues.split("\\s");
					log.debug("Processing parameter '" + name + "=" + Arrays.toString(values) + "'");
					
					String operator = null;	// operation (orsand, trunc, phrase ...)
					String md = null;		// metadata class (a, t, x,...). null for "query_*" operators
	
					if (name.startsWith(META_PREFIX)) {
						
						// Find the metadata class
						Matcher m = META_ID_PATTERN.matcher(name);
						if (m.find()) {
							md = m.group(1);
							operator = m.group(2);
						} else {
							// Possibly a simple "meta_x=value"
							md = name.substring(name.indexOf("_")+1);
							if (!"".equals(md)) {	// In case it's a "meta_=abc"
								operator = INTERNAL_OPERATOR_ADDMETA;
							}
						}
					} else if (name.startsWith(QUERY_PREFIX) && name.length() > QUERY_PREFIX.length()) {
						operator = name.substring(name.indexOf("_")+1);
					}
					
					if (operator != null && operator.length() > 0 && Operators.isValid(operator)) {
						try {
							// Find operator method by reflection
							Method operationMethod = this.getClass().getMethod(operator, new Class[] {String.class, String[].class});
							String newValue = (String) operationMethod.invoke(this, new Object[] {md, values});
							log.debug("Applied operation '" + operator + "' to value '"+Arrays.toString(values)+"'. New value is '" + newValue + "'");
							searchTransaction.getQuestion().getMetaParameters().put(name, newValue);
							
							// Remove the parameter from the list that will be passed to PADRE if
							// we succesfully processed it
							searchTransaction.getQuestion().getPassThroughParameters().remove(name);
						} catch (Exception ex) {
							log.warn("Error while invoking operation '" + operator + "' from parameter '" + name + "'", ex);
						}
					} else {
						log.warn("Invalid operator '"+operator+"' for parameter '" + name + "'"); 
					}
				}
			}
		}
	}
	
	
	public String trunc(final String md, final String values[]) {
		String[] data = values;
		if (md != null) {
			data = prefixStrings(values, md+":");
		}
		return "*" + StringUtils.join(data, "* *") + "*";
	}
	
	public String orplus(final String md, final String values[]) {
		String[] data = values;
		if (md != null) {
			data = prefixStrings(values, md+":");
		}
		return "+[" + StringUtils.join(data, " ") + "]";
	}

	public String orsand(final String md, final String values[]) {
		String[] data = values;
		if (md != null) {
			data = prefixStrings(values, md+":");
		}	
		return "|[" + StringUtils.join(data, " ") + "]";
	}
	
	public String or(final String md, final String values[]) {
		String[] data = values;
		if (md != null) {
			data = prefixStrings(values, md+":");
		}	
		return "[" + StringUtils.join(data, " ") + "]";
	}
	
	public String phrase(final String md, final String values[]) {
		// Remove any double quote before re-quoting
		String out = "\"" + StringUtils.join(values, " ").replace("\"", "") + "\"";
		if (md == null) {
			return out;
		} else {
			return md + ":" + out; 
		}
	} 
	
	public String prox(final String md, final String values[]) {
		String out = "`" + StringUtils.join(values, " ") + "`";
		if (md == null) {
			return out;
		} else {
			return md + ":" + out; 
		}
	}
	
	public String and(final String md, final String values[]) {
		String[] data = values;
		if (md != null) {
			data = prefixStrings(values, md+":");
		}	
		return StringUtils.join(prefixStrings(data, "+"), " ");
	}

	public String sand(final String md, final String values[]) {
		String[] data = values;
		if (md != null) {
			data = prefixStrings(values, md+":");
		}	
		return StringUtils.join(prefixStrings(data, "|"), " ");		
	}

	public String not(final String md, final String values[]) {
		String[] data = values;
		if (md != null) {
			data = prefixStrings(values, md+":");
		}	
		return StringUtils.join(prefixStrings(data, "-"), " ");
	}

	

	/**
	 * Special internal operation for parameters like meta_x=value. In this case
	 * we just return the value which has already been transformed as "x:<value>"
	 * @param value Ex: x:value
	 * @return the value
	 */
	public String addmeta(final String md, final String values[]) {
		if (md != null) {
			StringBuffer out = new StringBuffer();
			for(int i=0; i<values.length; i++) {
				out.append(md+":"+values[i]);
				if (i+1<values.length) {
					out.append(" ");
				}
			}
			return out.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * Prefixes an array of String with a prefix
	 * @param data Array of String to prefix
	 * @param prefix The prefix string
	 * @return An array with each String prefixed
	 */
	private String[] prefixStrings(final String[] data, String prefix) {
		String[] out = new String[data.length];
		for (int i=0;i<data.length;i++) {
			out[i] = prefix + data[i]; 
		}
		return out;
	}
	
}
