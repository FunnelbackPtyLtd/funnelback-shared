package com.funnelback.publicui.search.model.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.funnelback.publicui.web.utils.QueryStringUtils;

public class ParameterTransformation {

	/**
	 * Tranform rule syntax is:
	 * replaced_param=value => insert_param1=value&insert_param2=value
	 * param=value => -remove_param1
	 * ...
	 * 
	 * @see Original Perl code
	 */
	private static final Pattern RULE_PATTERN = Pattern.compile("^\\s*([^\\s]+?)\\s*=>\\s*([^\\s]+?)\\s*$");
	private static final Pattern FROM_PATTERN = Pattern.compile("^\\s*([^=]+)(\\s*=\\s*(.*))?\\s*");
	private static final Pattern TO_PATTERN = Pattern.compile("^\\s*(\\-)?(.+)?\\s*");

	private ArrayList<Rule> transformRules = new ArrayList<Rule>();
	
	public void initRules(String[] rules) {
		if (rules != null) {
			for (String rule : rules) {
				Matcher m = RULE_PATTERN.matcher(rule);
				if (m.find()) {
					String from = m.group(1);
					String to = m.group(2);

					Matcher fromMatcher = FROM_PATTERN.matcher(from);
					Matcher toMatcher = TO_PATTERN.matcher(to);
					
					if (fromMatcher.matches() && toMatcher.matches()) {
						Criteria c;
											
						String fromParamName = fromMatcher.group(1);
						String fromParamValue = fromMatcher.group(3);
						
						if (fromParamValue != null && ! "".equals(fromParamValue)) {
							c = new ParameterMatchesValueCriteria(fromParamName, fromParamValue);
						} else {
							c = new ParameterPresentCriteria(fromParamName);
						}
						
						String toParams = toMatcher.group(2);
						ArrayList<Operation> operations = new ArrayList<Operation>();
						boolean remove = toMatcher.group(1) != null;
						if (remove) {
							Map<String, List<String>> params = QueryStringUtils.toMap(toParams, false);
							for (String name : params.keySet()) {
								if (params.get(name) != null && params.get(name).size() > 0) {
									operations.add(new RemoveSpecificValuesOperation(name, params.get(name)));
								} else {
									operations.add(new RemoveAllValuesOperation(name));
								}
							}
						} else {
							Map<String, List<String>> params = QueryStringUtils.toMap(toParams, false);
							for (String name : params.keySet()) {
								operations.add(new AddParameterOperation(name, params.get(name)));
							}
							
						}
						
						transformRules.add(new Rule(c, operations));
					}
				}
			}
		}
	}

	public void apply(Map<String, String[]> parameters) {
		for (Rule rule: transformRules) {
			if (rule.getCriteria().matches(parameters)) {
				for (Operation o: rule.getOperations()) {
					o.apply(parameters);
				}
			}
		}
	}
	
	@RequiredArgsConstructor
	private class Rule {
		@Getter private final Criteria criteria;
		@Getter private final List<Operation> operations;
		
		@Override
			public String toString() {
				StringBuffer out = new StringBuffer();
				out.append("\n-> IF ").append(criteria.toString()).append("\n");
				out.append("APPLY \n");
				for (Operation o: operations) {
					out.append("\t").append(o.toString()).append("\n");
				}
				out.append("<-\n");
				return out.toString();
			}
	}
	
	
	// ----------------------------------------------------------------------
	private interface Operation {
		public void apply(Map<String, String[]> parameters);
	}
	
	private class RemoveAllValuesOperation implements Operation {
		private String parameterName;
		public RemoveAllValuesOperation(String parameterName) {
			this.parameterName = parameterName;
		}
		
		@Override
		public void apply(Map<String, String[]> parameters) {
			parameters.remove(parameterName);
		}
		
		@Override
		public String toString() {
			return "Remove all values of '" + parameterName + "'";
		}
	}
	
	private class RemoveSpecificValuesOperation implements Operation {
		private String parameterName;
		private List<String> parameterValues;
		public RemoveSpecificValuesOperation(String parameterName, List<String> parameterValues) {
			this.parameterName = parameterName;
			this.parameterValues = parameterValues;
		}
		@Override
		public void apply(Map<String, String[]> parameters) {
			if (parameters.containsKey(parameterName)) {
				String values[] = parameters.get(parameterName);
				ArrayList<String> newValues = new ArrayList<String>();
				for (String value: values) {
					if (! parameterValues.contains(value)) {
						newValues.add(value);
					}
				}
				parameters.put(parameterName, newValues.toArray(new String[0]));
			}
		}
		
		@Override
		public String toString() {
			return "Remove following values of '" + parameterName + "' : " + StringUtils.join(parameterValues, ",");
		}
	}
	
	private class AddParameterOperation implements Operation {
		private String parameterName;
		private String[] parameterValues;
		public AddParameterOperation(String parameterName, List<String> parameterValues) {
			this.parameterName = parameterName;
			this.parameterValues = parameterValues.toArray(new String[0]);
		}
		
		@Override
		public void apply(Map<String, String[]> parameters) {
			if (parameters.containsKey(parameterName)) {
				String values[] = parameters.get(parameterName);
				parameters.put(parameterName, (String[]) ArrayUtils.addAll(values, parameterValues));
			} else {
				parameters.put(parameterName, parameterValues);
			}
		}
		
		@Override
		public String toString() {
			return "Add parameter '" + parameterName +"' = '" + StringUtils.join(parameterValues, ",") + "'";
		}
		
	}
	
	// -----------------------------------------------------------------------------------------------
	
	private interface Criteria {
		public boolean matches(Map<String, String[]> parameters);
	}
	
	private class ParameterPresentCriteria implements Criteria {
		private String parameterName;
		public ParameterPresentCriteria(String parameterName) {
			this.parameterName = parameterName;
		}
		@Override
		public boolean matches(Map<String, String[]> parameters) {
			return parameters.containsKey(parameterName);
		}
		
		@Override
		public String toString() {
			return "Parameter '" + parameterName + "' is present";
		}
	}
	
	private class ParameterMatchesValueCriteria implements Criteria {
		private String parameterName;
		private String parameterValue;
		public ParameterMatchesValueCriteria(String parameterName, String parameterValue) {
			this.parameterName = parameterName;
			this.parameterValue = parameterValue;
		}
		
		@Override
		public boolean matches(Map<String, String[]> parameters) {
			if (parameters.containsKey(parameterName)) {
				String[] values = parameters.get(parameterName);
				return ArrayUtils.contains(values, parameterValue);
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "Parameter '" + parameterName + "' is present and has value '" + parameterValue  + "'";
		}
	}
	
}
