package com.funnelback.publicui.search.model.collection.paramtransform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.apachecommons.Log;

import com.funnelback.publicui.search.model.collection.paramtransform.criteria.Criteria;
import com.funnelback.publicui.search.model.collection.paramtransform.criteria.ParameterMatchesValueCriteria;
import com.funnelback.publicui.search.model.collection.paramtransform.criteria.ParameterPresentCriteria;
import com.funnelback.publicui.search.model.collection.paramtransform.operation.AddParameterOperation;
import com.funnelback.publicui.search.model.collection.paramtransform.operation.Operation;
import com.funnelback.publicui.search.model.collection.paramtransform.operation.RemoveAllValuesOperation;
import com.funnelback.publicui.search.model.collection.paramtransform.operation.RemoveSpecificValuesOperation;
import com.funnelback.publicui.utils.QueryStringUtils;

/**
 * Builds a list of parameters transformation rules by
 * parsing a list of text rules (extracted from cgi_transform.cfg.
 * 
 * @see TransformRule
 */
@Log
public class ParamTransformRuleFactory {

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
	
	/**
	 * Builds a list of {@link TransformRule} by parsing textual rules.
	 * @param rules Textual rules, as in cgi_transform.cfg
	 * @return Parsed {@link TransformRule}s
	 */
	public static List<TransformRule> buildRules(String[] rules) {
		ArrayList<TransformRule> transformRules = new ArrayList<TransformRule>();
		
		if (rules != null) {
			
			for (String rule : rules) {
				
				Matcher m = RULE_PATTERN.matcher(rule);
				if (m.find()) {
					String from = m.group(1);
					String to = m.group(2);

					Matcher fromMatcher = FROM_PATTERN.matcher(from);
					Matcher toMatcher = TO_PATTERN.matcher(to);
					
					if (fromMatcher.matches() && toMatcher.matches()) {
						// Rule is valid

						// Find criteria (left end operator)
						String fromParamName = fromMatcher.group(1);
						String fromParamValue = fromMatcher.group(3);
						Criteria c = buildCriteria(fromParamName, fromParamValue);

						// Find operation(s) (right end operator)
						boolean remove = toMatcher.group(1) != null;
						String toParams = toMatcher.group(2);
						List<Operation> operations = buildOperations(remove, toParams);
						
						// Build rule
						TransformRule r = new TransformRule(c, operations);
						transformRules.add(r);
						log.debug("Built following rule from '" + rule + "' : " + r);
					}
				}
			}
		}
		return transformRules;
	}

	/**
	 * Builds a criteria.
	 * @param fromParamName
	 * @param fromParamValue
	 * @return
	 */
	private static Criteria buildCriteria(String fromParamName, String fromParamValue) {
		if (fromParamValue != null && ! "".equals(fromParamValue)) {
			return new ParameterMatchesValueCriteria(fromParamName, fromParamValue);
		} else {
			return new ParameterPresentCriteria(fromParamName);
		}
	}
	
	/**
	 * Builds a list of operations
	 * @param remove
	 * @param paramString
	 * @return
	 */
	private static List<Operation> buildOperations(boolean remove, String paramString) {
		ArrayList<Operation> operations = new ArrayList<Operation>();
		
		if (remove) {
			Map<String, List<String>> params = QueryStringUtils.toMap(paramString, false);
			for (String name : params.keySet()) {
				if (params.get(name) != null && params.get(name).size() > 0) {
					operations.add(new RemoveSpecificValuesOperation(name, params.get(name)));
				} else {
					operations.add(new RemoveAllValuesOperation(name));
				}
			}
		} else {
			Map<String, List<String>> params = QueryStringUtils.toMap(paramString, false);
			for (String name : params.keySet()) {
				operations.add(new AddParameterOperation(name, params.get(name)));
			}
		}
		return operations;
	}
	
}
