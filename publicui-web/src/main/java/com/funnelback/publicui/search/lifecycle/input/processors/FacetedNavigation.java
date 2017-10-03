package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.BinaryOperator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.gscope.GscopeName;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.GScopeBasedCategory;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.CollectionFill;
import com.funnelback.publicui.search.model.facetednavigation.FacetSelectedDetails;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * <p>Sets the <em>-rmcf</em> query processor option on the fly if faceted navigation
 * is enabled on this collection.</p>
 * 
 * <p>Checks for any selected category(ies) and adds corresponding constraints
 * to the search question (gscopes or query expressions).</p>
 */
@Log4j2
@Component("facetedNavigationInputProcessor")
public class FacetedNavigation extends AbstractInputProcessor {
    
    @Override
    public void processInput(SearchTransaction searchTransaction) {
        if (SearchTransactionUtils.hasCollection(searchTransaction)) {
            
            FacetedNavigationConfig config = FacetedNavigationUtils.selectConfiguration(searchTransaction.getQuestion().getCollection(), searchTransaction.getQuestion().getProfile());
            
            if (config != null) {
                
                Map<String, FacetDefinition> facetConfigs = getFacetDefinitions(config);
                
                // Global set of constraints. A Set per Facet
                
                SetMultimap<String, String> gscope1Constraints = SetMultimapBuilder.hashKeys().hashSetValues().build();
                SetMultimap<String, String> queryConstraints = SetMultimapBuilder.hashKeys().hashSetValues().build();
                SetMultimap<String, Set<String>> cliveConstraints = SetMultimapBuilder.hashKeys().hashSetValues().build();
                
                
                List<FacetSelectedDetails> facetParamaters = FacetedNavigationUtils.getFacetSelectedDetails(searchTransaction.getQuestion());
                if (facetParamaters.size() > 0) {
                    for (final FacetSelectedDetails facetParameter : facetParamaters) {
                        // Find corresponding facet in config
                        FacetDefinition f = facetConfigs.get(facetParameter.getFacetName());
                        
                        if(f != null && f.getConstraintJoin() == FacetConstraintJoin.LEGACY) {
                            continue;
                        }
                    
                        if (f != null) {
                            searchTransaction.getQuestion().getSelectedFacets().add(f.getName());
                            
                            
                        
                            // Find corresponding category type, for the value
                            String value = facetParameter.getValue();
                            {
                                // Find category or subcategory
                                CategoryDefinition ct = findCategoryType(f.getCategoryDefinitions(), value, facetParameter.getExtraParameter());
                                
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
                                        gscope1Constraints.put(f.getName(), type.getGScope1Constraint());
                                    } else if (ct instanceof MetadataBasedCategory) {
                                        MetadataBasedCategory type = (MetadataBasedCategory) ct;
                                        queryConstraints.put(f.getName(), type.getQueryConstraint(value));
                                    } else if(ct instanceof CollectionFill) {
                                        CollectionFill collectionFill = (CollectionFill) ct;
                                        cliveConstraints.put(f.getName(), new HashSet<>(collectionFill.getCollections()));
                                    }
                                }
                            }
                        }
                        
                    }
                    
                    if (gscope1Constraints.size() > 0) {
                        searchTransaction.getQuestion().setFacetsGScopeConstraints(getGScope1Parameters(gscope1Constraints, facetConfigs));
                        log.debug("Added gscope1 constraints '" + searchTransaction.getQuestion().getFacetsGScopeConstraints());
                    }
                    
                    if (queryConstraints.size() > 0) {
                        searchTransaction.getQuestion().getFacetsQueryConstraints().addAll(getQueryConstraints(queryConstraints, facetConfigs));
                        log.debug("Added query constraints '" + StringUtils.join(searchTransaction.getQuestion().getFacetsQueryConstraints(), ",") + "'");
                    }
                    
                    if(cliveConstraints.size() > 0) {
                        searchTransaction.getQuestion().setFacetCollectionConstraints(Optional.of(getCliveConstraints(cliveConstraints, facetConfigs)));
                    }
                }
            }
        }
    }
    
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class FacetNameAndConstraints {
        @Getter private final String facetName;
        @Getter private final Set<String> constraintsToOr;
    }
    
    Map<String, Set<FacetNameAndConstraints>> organiseFacetQueryConstraints(Set<FacetNameAndConstraints> constraints) {
        Map<String, Set<FacetNameAndConstraints>> map = new HashMap<>();
        constraints.stream().map(FacetNameAndConstraints::getFacetName).forEach(f -> map.put(f, new HashSet<>()));
        constraints.stream().forEach(c -> map.get(c.getFacetName()).add(c));
        return map;
    }
    
    
    public Map<String, FacetDefinition> getFacetDefinitions(FacetedNavigationConfig config) {
        Map<String, FacetDefinition> facetConfigs = new HashMap<>();
        for(FacetDefinition facet: Optional.ofNullable(config.getFacetDefinitions()).orElse(Collections.emptyList())) {
            facetConfigs.putIfAbsent(facet.getName(), facet);
        }
        return facetConfigs;
    }

    /**
     * Get updated gscope1 parameters by taking into account any existing gscope1 parameter
     * and a set of constraints from the faceted navigation.
     * Each facet constraints will be combined with and AND, and each category constraint will be
     * combined with either an OR or AND depending on the constraint join of the facet.
     * @param gscope1Constraints
     * @param existingGScope1Parameters
     * @return
     */
    private String getGScope1Parameters(SetMultimap<String, String> gscope1ConstraintsByFacet,
        Map<String, FacetDefinition> facetDefinitions) {
        Stack<String> out = new Stack<String>();
        
        for(Map.Entry<String, Collection<String>> e : gscope1ConstraintsByFacet.asMap().entrySet()) {
            FacetDefinition f = facetDefinitions.get(e.getKey());
            Collection<String> constraints = e.getValue();
            
            
            // Add each constraint, followed by corresponding numbers of OR operators
            out.addAll(constraints);
            for(int i=0; i<constraints.size()-1; i++) {
                if(f.getConstraintJoin() == FacetConstraintJoin.AND) {
                    out.push("+");
                } else {
                    out.push("|");
                }
            }
        }
        
        // For each facet constraint, add an AND operator
        for (int i=0; i<gscope1ConstraintsByFacet.keySet().size() -1; i++) {
                out.push("+");
        }
        
        return serializeRPN(out);
    }
    
    
    private List<String> getCliveConstraints(SetMultimap<String, Set<String>> cliveConstraintsByFacet,
        Map<String, FacetDefinition> facetDefinitions) {
        List<Set<String>> facetConstraintsToJoin = new ArrayList<>();
        for(Map.Entry<String, Collection<Set<String>>> e : cliveConstraintsByFacet.asMap().entrySet()) {
            FacetDefinition f = facetDefinitions.get(e.getKey());
            Collection<Set<String>> toJoin = e.getValue();
            
            toJoin.stream()
                    .reduce(getReducerFromConstraintJoin(f.getConstraintJoin()))
                    .ifPresent(facetConstraintsToJoin::add);
            
        }
        
        Set<String> allowedCollections = facetConstraintsToJoin.stream()
                    .reduce(Sets::intersection)
                    .orElse(Collections.emptySet());
        
        return new ArrayList<>(allowedCollections);
    }
    
    private <E> BinaryOperator<Set<E>> getReducerFromConstraintJoin(FacetConstraintJoin join) {
        if(join == FacetConstraintJoin.OR) {
            return Sets::union;
        }
        if(join == FacetConstraintJoin.AND) {
            return Sets::intersection;
        }
        throw new RuntimeException("Can not reduce for constraint type: " + join);
    }
    
    
    
    
    /**
     * Gets a query expression composed by query constraints coming from faceted navigation.
     * Each facet constraints will be combined with an AND, and each category constraint will be
     * combined with an OR.
     * @param queryConstraints
     * @return
     */
    private List<String> getQueryConstraints(SetMultimap<String, String> queryConstraintsByFacet,
        Map<String, FacetDefinition> facetDefinitions) {
        List<String> out = new ArrayList<String>();

        
            
        for(Map.Entry<String, Collection<String>> e : queryConstraintsByFacet.asMap().entrySet()) {
            
            FacetDefinition f = facetDefinitions.get(e.getKey());
            Collection<String> queryConstraint = e.getValue();
            if (queryConstraint.size() == 1) {
                out.add("|" + queryConstraint.iterator().next());
            } else if (queryConstraint.size() > 1) {
                if(f.getConstraintJoin() == FacetConstraintJoin.OR) {
                    StringBuffer newConstraints = new StringBuffer();
                    newConstraints.append("|[");
                    for (Iterator<String> it = queryConstraint.iterator(); it.hasNext();) {
                        newConstraints.append(it.next());
                        if (it.hasNext()) {
                            newConstraints.append(" ");
                        }
                    }
                    newConstraints.append("]");
                    out.add(newConstraints.toString());
                } else {
                    // Each one of these can be a seperate constraint so lets do that.
                    for (Iterator<String> it = queryConstraint.iterator(); it.hasNext();) {
                        out.add("|" + it.next());
                    } 
                }
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
    public String serializeRPN(Stack<String> rpn) {
        StringBuffer out = new StringBuffer();
        for (int i=0; i<rpn.size(); i++) {
            out.append(rpn.get(i));
            if (i+1 < rpn.size() && GscopeName.isValidGscope(rpn.get(i)) && GscopeName.isValidGscope(rpn.get(i+1))) {
                out.append(",");
            }
        }
        log.debug("Serialized '" + StringUtils.join(rpn, ",") + "' to '" + out.toString() + "'");
        return out.toString();
    }
    
}
