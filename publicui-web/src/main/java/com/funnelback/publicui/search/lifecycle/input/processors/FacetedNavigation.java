package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;

import lombok.extern.log4j.Log4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.GScopeBasedCategory;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.funnelback.publicui.utils.MapKeyFilter;

/**
 * <p>Sets the <em>-rmcf</em> query processor option on the fly if faceted navigation
 * is enabled on this collection.</p>
 * 
 * <p>Checks for any selected category(ies) and adds corresponding constraints
 * to the search question (gscopes or query expressions).</p>
 */
@Log4j
@Component("facetedNavigationInputProcessor")
public class FacetedNavigation extends AbstractInputProcessor {
    
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
                
                // Read facet names from the 'raw' parameters since they can
                // be multi-valued (Multiple categories selected for a single facet)
                MapKeyFilter filter = new MapKeyFilter(searchTransaction.getQuestion().getRawInputParameters());
                String[] selectedFacetsParams = filter.filter(RequestParameters.FACET_PARAM_PATTERN);
                
                if (selectedFacetsParams.length > 0) {
                    for (final String selectedFacetParam: selectedFacetsParams) {
                        log.debug("Found facet parameter '" + selectedFacetParam + "'");
                        Matcher m = RequestParameters.FACET_PARAM_PATTERN.matcher(selectedFacetParam);
                        m.find();
                        
                        final String facetName = m.group(1);
                        final String extraParam = m.group(3);
                        log.debug("Found facet name '" + facetName + "' and extra parameter '" + extraParam + "'");
                        
                        // Find corresponding facet in config
                        FacetDefinition f = (FacetDefinition) CollectionUtils.find(config.getFacetDefinitions(), new Predicate() {
                            @Override
                            public boolean evaluate(Object o) {
                                return ((FacetDefinition) o).getName().equals(facetName);
                            }
                        });
                        
                        final String values[] = searchTransaction.getQuestion().getRawInputParameters().get(selectedFacetParam);
                    
                        if (f != null && values != null) {
                            searchTransaction.getQuestion().getSelectedFacets().add(f.getName());
                            
                            // Set of constraints for this specific facet
                            Set<String> gscope1FacetConstraints = new HashSet<String>();
                            Set<String> queryFacetConstraints = new HashSet<String>();
                        
                            // Find corresponding category type, for each value
                            for(final String value: values) {
                                if ("".equals(value)) {
                                    // Skip empty strings
                                    continue;
                                }
                                
                                // Find category or subcategory
                                CategoryDefinition ct = findCategoryType(f.getCategoryDefinitions(), value, extraParam);
                                
                                if (ct != null) {
                                    List<String> selectedCategoriesValues = searchTransaction.getQuestion().getSelectedCategoryValues().get(ct.getQueryStringParamName());
                                    if (selectedCategoriesValues == null) {
                                        selectedCategoriesValues = new ArrayList<String>();
                                    }
                                    // Put this category in the list of the selected ones
                                    selectedCategoriesValues.add(value);
                                    searchTransaction.getQuestion().getSelectedCategoryValues().put(ct.getQueryStringParamName(), selectedCategoriesValues);
                                    
                                    // Add constraints for this category
                                    if (ct instanceof GScopeBasedCategory) {
                                        GScopeBasedCategory type = (GScopeBasedCategory) ct;
                                        gscope1FacetConstraints.add(type.getGScope1Constraint());
                                        
                                    } else if (ct instanceof MetadataBasedCategory) {
                                        MetadataBasedCategory type = (MetadataBasedCategory) ct;
                                        queryFacetConstraints.add(type.getQueryConstraint(value));
                                    }
                                }
                            }
                            
                            if (gscope1FacetConstraints.size() > 0) {
                                gscope1Constraints.add(gscope1FacetConstraints);
                            }
                            if (queryFacetConstraints.size() > 0 ) {
                                queryConstraints.add(queryFacetConstraints);
                            }
                        }
                        
                    }
                    
                    if (gscope1Constraints.size() > 0) {
                        searchTransaction.getQuestion().setFacetsGScopeConstraints(getGScope1Parameters(gscope1Constraints));
                        log.debug("Added gscope1 constraints '" + searchTransaction.getQuestion().getFacetsGScopeConstraints());
                    }
                    
                    if (queryConstraints.size() > 0) {
                        searchTransaction.getQuestion().getFacetsQueryConstraints().addAll(getQueryConstraints(queryConstraints));
                        log.debug("Added query constraints '" + StringUtils.join(searchTransaction.getQuestion().getFacetsQueryConstraints(), ",") + "'");
                    }
                }
            }
        }
    }

    /**
     * Get updated gscope1 parameters by taking into account any existing gscope1 parameter
     * and a set of constraints from the faceted navigation.
     * Each facet constraints will be combined with and AND, and each category constraint will be
     * combined with an OR.
     * @param gscope1Constraints
     * @param existingGScope1Parameters
     * @return
     */
    private String getGScope1Parameters(Set<Set<String>> gscope1Constraints) {
        String updated = "";
        if (gscope1Constraints.size() > 0) {
            Stack<String> out = new Stack<String>();
            // Add each constraint, followed by corresponding numbers of OR operators
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
            // if (existingGScope1Parameters != null && !"".equals(existingGScope1Parameters)) {
            //     updated += existingGScope1Parameters + "+";
            // }
        }
        return updated;
    }
    
    /**
     * Gets a query expression composed by query constraints coming from faceted navigation.
     * Each facet constraints will be combined with an AND, and each category constraint will be
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
                    for (Iterator<String> it = queryConstraint.iterator(); it.hasNext();) {
                        newConstraints.append(it.next());
                        if (it.hasNext()) {
                            newConstraints.append(" ");
                        }
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
    private CategoryDefinition findCategoryType(List<CategoryDefinition> cts, String value, String extraParam) {
        for (CategoryDefinition ct: cts) {
            if (ct.matches(value, extraParam)) {
                return ct;
            } else {
                CategoryDefinition sub = findCategoryType(ct.getSubCategories(), value, extraParam);
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
