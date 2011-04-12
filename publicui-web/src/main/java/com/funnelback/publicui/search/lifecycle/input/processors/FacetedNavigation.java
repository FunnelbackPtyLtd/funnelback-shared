package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.apachecommons.Log;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryType;
import com.funnelback.publicui.search.model.collection.facetednavigation.Facet;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetedNavigationUtils;
import com.funnelback.publicui.search.model.collection.facetednavigation.GScopeBasedType;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedType;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.web.utils.MapKeyFilter;

/**
 * Sets the "rmcf" query processor option on the fly if faceted navigation
 * is enabled on this collection.
 * 
 * Checks for any selected category(ies) and adds corresponding constraints
 * to the search question (gscopes or query expressions)
 */
@Log
@Component("facetedNavigationInputProcessor")
public class FacetedNavigation implements InputProcessor {

	private final Pattern FACET_PARAM_PATTERN = Pattern.compile("^" + RequestParameters.FACET_PREFIX.replaceAll("\\.", "\\\\.") + "([^\\|]+)(\\|(.*))?");
	
	@Override
	public void processInput(SearchTransaction searchTransaction) {
		if (SearchTransactionUtils.hasCollection(searchTransaction)) {
			
			FacetedNavigationConfig config = FacetedNavigationUtils.selectConfiguration(searchTransaction.getQuestion().getCollection(), searchTransaction.getQuestion().getProfile());
			
			if (config != null) {
				if (config.getQpOptions() != null && ! "".equals(config.getQpOptions())) {
					// Query Processor options are embedded in the faceted_navigation.cfg file
					searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(config.getQpOptions());
					log.debug("Setting dynamic query processor option '" + config.getQpOptions() + "'");
				}
				
				// Global set of constraints. A Set per Facet
				Set<Set<String>> gscope1Constraints = new HashSet<Set<String>>();
				Set<Set<String>> queryConstraints = new HashSet<Set<String>>();
				
				MapKeyFilter filter = new MapKeyFilter(searchTransaction.getQuestion().getInputParameterMap());
				String[] selectedFacetsParams = filter.filter(FACET_PARAM_PATTERN);
				
				if (selectedFacetsParams.length > 0) {
					for (final String selectedFacetParam: selectedFacetsParams) {
						log.debug("Found facet parameter '" + selectedFacetParam + "'");
						Matcher m = FACET_PARAM_PATTERN.matcher(selectedFacetParam);
						m.find();
						
						final String facetName = m.group(1);
						final String extraParam = m.group(3);
						final String values[] = searchTransaction.getQuestion().getInputParameterMap().get(selectedFacetParam);
						log.debug("Fond facet name '" + facetName + "' and extra parameter '" + extraParam + "'");
						
						// Find corresponding facet in config
						Facet f = (Facet) CollectionUtils.find(config.getFacets(), new Predicate() {
							@Override
							public boolean evaluate(Object o) {
								return ((Facet) o).getName().equals(facetName);
							}
						});
						
						if (f != null) {
							// Set of constraints for this specific facet
							Set<String> gscope1FacetConstraints = new HashSet<String>();
							Set<String> queryFacetConstraints = new HashSet<String>();
						
							// Find corresponding category type, for each value
							for(final String value: values) {
								// Find category or subcategory
								CategoryType ct = findCategoryType(f.getCategoryTypes(), value, extraParam);
								
								if (ct != null) {
									List<String> selectedCategoriesValues = searchTransaction.getQuestion().getSelectedCategories().get(ct.getUrlParamName());
									if (selectedCategoriesValues == null) {
										selectedCategoriesValues = new ArrayList<String>();
									}
									// Put this category in the list of the selected ones
									selectedCategoriesValues.add(value);
									searchTransaction.getQuestion().getSelectedCategories().put(ct.getUrlParamName(), selectedCategoriesValues);
									
									// Add constraints for this category
									if (ct instanceof GScopeBasedType) {
										GScopeBasedType type = (GScopeBasedType) ct;
										gscope1FacetConstraints.add(type.getGScope1Constraint());
										
									} else if (ct instanceof MetadataBasedType) {
										MetadataBasedType type = (MetadataBasedType) ct;
										queryFacetConstraints.add(type.getQueryConstraint(value));
									}
								}
							}
							
							gscope1Constraints.add(gscope1FacetConstraints);
							queryConstraints.add(queryFacetConstraints);
						}
						
					}
					
					String existingGScope1Parameters = "";
					if (searchTransaction.getQuestion().getAdditionalParameters().get(RequestParameters.GSCOPE1) != null) {
						// Only one value is relevant
						existingGScope1Parameters = searchTransaction.getQuestion().getAdditionalParameters().get(RequestParameters.GSCOPE1)[0];
					}
					
					String updatedParameters = getGScope1Parameters(gscope1Constraints, existingGScope1Parameters);
					searchTransaction.getQuestion().getAdditionalParameters().put(RequestParameters.GSCOPE1, new String[] {updatedParameters.replace("+", "%2B")});
					log.debug("Updated gscope1 constraints from '" + existingGScope1Parameters + "' to '" + updatedParameters + "'");
					
					
					List<String> additionalQueryConstraints = getQueryConstraints(queryConstraints);
					searchTransaction.getQuestion().getQueryExpressions().addAll(additionalQueryConstraints);
					log.debug("Added query constraints '" + StringUtils.join(additionalQueryConstraints, ",") + "'");
					}
			}
		}
	}

	/**
	 * Get updated gscope1 parameters by taking into account any existing gscop1 parameter
	 * and a set of constraints from the faceted navigation.
	 * Each facet constraints will be combined with and AND, and each category constraint will be
	 * combined with an OR.
	 * @param gscope1Constraints
	 * @param existingGScope1Parameters
	 * @return
	 */
	private String getGScope1Parameters(Set<Set<String>> gscope1Constraints, String existingGScope1Parameters) {
		String updated = "";
		if (gscope1Constraints.size() > 0) {
			Stack<String> out = new Stack<String>();
			// Add each constraing, followed by corresponding numbers of OR operators
			for (Set<String> constraints: gscope1Constraints) {
				out.addAll(constraints);
				if (constraints.size() > 1) {
					for(int i=0; i<constraints.size()-1; i++) {
						out.push("|");
					}
				}
			}
			
			// For each facet constraint, add an AND operator
			for (int i=0; i<gscope1Constraints.size() -1; i++) {
				out.push("+");
			}
			
			updated = serializeRPN(out);

			// Add existing constraints and an AND operator
			if (existingGScope1Parameters != null && !"".equals(existingGScope1Parameters)) {
				updated += existingGScope1Parameters + "+";
			}
		}
		return updated;
	}
	
	/**
	 * Gets a query expression composed by query constraints coming from faceted navigation.
	 * Each facet constraints will be combined with and AND, and each category constraint will be
	 * combined with an OR.
	 * @param queryConstraints
	 * @return
	 */
	private List<String> getQueryConstraints(Set<Set<String>> queryConstraints) {
		List<String> out = new ArrayList<String>();

		if (queryConstraints.size() > 0) {
			
			for (Set<String> queryConstraint: queryConstraints) {
				StringBuffer newConstraints = new StringBuffer();
				
				if (queryConstraint.size() == 1) {
					newConstraints.append("|" + queryConstraint.iterator().next());
				} else if (queryConstraint.size() > 1) {
					newConstraints.append("|[");
					for (String constraint: queryConstraint) {
						newConstraints.append(" ").append(constraint);
					}
					newConstraints.append("]");
				}
				
				out.add(newConstraints.toString());
			}
		}
		
		return out;
	}

	/**
	 * Recursively find a category type matching the value and extra param.
	 * @param cts
	 * @param value
	 * @param extraParam
	 * @return
	 */
	private CategoryType findCategoryType(List<CategoryType> cts, String value, String extraParam) {
		for (CategoryType ct: cts) {
			if (ct.matches(value, extraParam)) {
				return ct;
			} else {
				CategoryType sub = findCategoryType(ct.getSubCategories(), value, extraParam);
				if (sub != null) {
					return sub;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Serializes a stack of RPN gscope operations in a PADRE format.
	 * @param rpn
	 * @return
	 */
	private String serializeRPN(Stack<String> rpn) {
		StringBuffer out = new StringBuffer();
		for (int i=0; i<rpn.size(); i++) {
			out.append(rpn.get(i));
			if (i+1 < rpn.size() && StringUtils.isNumeric(rpn.get(i)) && StringUtils.isNumeric(rpn.get(i+1))) {
				out.append(",");
			}
		}
		log.debug("Serialized '" + StringUtils.join(rpn, ",") + "' to '" + out.toString() + "'");
		return out.toString();
	}
	
}
