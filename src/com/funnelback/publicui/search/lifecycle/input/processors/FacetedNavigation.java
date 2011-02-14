package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.apachecommons.Log;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
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
import com.funnelback.publicui.web.utils.RequestParametersFilter;

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
	public void process(SearchTransaction searchTransaction, final HttpServletRequest request) {
		if (SearchTransactionUtils.hasCollection(searchTransaction)) {
			
			FacetedNavigationConfig config = FacetedNavigationUtils.selectConfiguration(searchTransaction.getQuestion().getCollection(), searchTransaction.getQuestion().getProfile());
			
			if (config != null && config.getQpOptions() != null) {
				// Query Processor options are embedded in the faceted_navigation.cfg file:
				//
				// <Facets qpoptions=" -rmcfdt">
				//  <Data></Data>
				//  <Facet>
				//  ...
				//  </Facet>
				// </Facets>
				searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(config.getQpOptions());
				log.debug("Setting dynamic query processor option '" + config.getQpOptions() + "'");
				
				// Global set of constraints. A Set per Facet
				Set<Set<String>> gscope1Constraints = new HashSet<Set<String>>();
				Set<Set<String>> queryConstraints = new HashSet<Set<String>>();
				
				RequestParametersFilter filter = new RequestParametersFilter(request);
				String[] selectedFacetsParams = filter.filter(FACET_PARAM_PATTERN);
				
				for (final String selectedFacetParam: selectedFacetsParams) {
					log.debug("Found facet parameter '" + selectedFacetParam + "'");
					Matcher m = FACET_PARAM_PATTERN.matcher(selectedFacetParam);
					m.find();
					
					final String facetName = m.group(1);
					final String extraParam = m.group(3);
					final String values[] = request.getParameterValues(selectedFacetParam);
					log.debug("Fond facet name '" + facetName + "' and extra parameter '" + extraParam + "'");
					
					// Find corresponding facet in config
					Facet f = (Facet) CollectionUtils.find(config.getFacets(), new Predicate() {
						@Override
						public boolean evaluate(Object o) {
							Facet f = (Facet) o;
							return f.getName().equals(facetName);
						}
					});
					
					log.debug("Found facet " + f);
					
					
					// Set of constraints for this specific facet
					Set<String> gscope1FacetConstraints = new HashSet<String>();
					Set<String> queryFacetConstraints = new HashSet<String>();
					
					// Find corresponding category type, for each value
					for(final String value: values) {
						CategoryType ct = (CategoryType) CollectionUtils.find(f.getCategoryTypes(), new Predicate() {
							@Override
							public boolean evaluate(Object o) {
								CategoryType categoryType = (CategoryType) o;
								return categoryType.matches(value, extraParam);
							}
						});
						
						if (ct instanceof GScopeBasedType) {
							GScopeBasedType type = (GScopeBasedType) ct;
							gscope1FacetConstraints.add(type.getGScope1Constraint());
							
						} else if (ct instanceof MetadataBasedType) {
							MetadataBasedType type = (MetadataBasedType) ct;
							queryFacetConstraints.add(type.getQueryConstraint(value));
						}
					}
					
					gscope1Constraints.add(gscope1FacetConstraints);
					queryConstraints.add(queryFacetConstraints);
				}
				
				/*
				if (gscope1Constraints.size() > 0) {
					String[] existingConstraints = searchTransaction.getQuestion().getAdditionalParameters().get("gscope1");
					StringBuffer newConstraints = new StringBuffer();
					String additionalOperator = "";
					if (existingConstraints != null) {
						// It's assumed that there is only one gscope1 parameter (Doesn't make sense otherwise)
						newConstraints.append(existingConstraints[0]);
						if (! (existingConstraints[0].endsWith("+") || existingConstraints[0].endsWith("|") || existingConstraints[0].endsWith("!"))) {
							newConstraints.append(",");
						}
						additionalOperator = "+";
					}
					
					for (Iterator<String> it = gscope1Constraints.iterator(); it.hasNext();) {
						newConstraints.append(it.next());
						if (it.hasNext()) {
							newConstraints.append(",");
						}
					}
					if (gscope1Constraints.size() > 1) {
						newConstraints.append(StringUtils.repeat("|", gscope1Constraints.size()-1));
					}
					newConstraints.append(additionalOperator);
					
					log.debug("Updating gscope1 constraints from '"
							+ ((existingConstraints != null) ? existingConstraints[0] : "")
							+ "' to '" + newConstraints.toString() + "'");
					
					searchTransaction.getQuestion().getAdditionalParameters().put("gscope1", new String[] {newConstraints.toString().replace("+", "%2B")});	
				}
				*/
				
				// Do a OR between each category of a Facet
				// then a AND between each Facet
				if (queryConstraints.size() > 0) {
					String out = "";
					for (Set<String> queryConstraint: queryConstraints) {
						StringBuffer newConstraints = new StringBuffer(" |[");
						for (String constraint: queryConstraint) {
							newConstraints.append(" ").append(constraint);
						}
						newConstraints.append("]");
						
						out += newConstraints;
					}
					
					log.debug("Updating query constraints from '" + searchTransaction.getQuestion().getQuery()
							+ "' to '" + searchTransaction.getQuestion().getQuery() + out + "'");
					
					searchTransaction.getQuestion().setQuery(searchTransaction.getQuestion().getQuery() + out);
				}
			}
		}
	}

}
