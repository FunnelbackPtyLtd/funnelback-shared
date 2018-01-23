package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.CharUtils;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.function.StreamUtils;
import com.funnelback.common.gscope.GscopeName;
import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.utils.MapUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Helper to build a query string that will be passed to PADRE. 
 */
@RequiredArgsConstructor
public class PadreQueryStringBuilder {

    private static final String DELIMITER = "&";
    
    private final SearchQuestion question;

    /**
     * Whether to apply faceted navigation constraints where
     * building the query string or not
     */
    private final boolean withFacetConstraints;
    
    public String buildQueryString() {
        Map<String, String[]> qs = new TreeMap<>();
        
        // Add any additional parameter
        qs.putAll(question.getAdditionalParameters());
        
        // Then craft our owns (Their value will overwrite any existing one in the additional set)
        qs.put(Parameters.collection.toString(), new String[] {question.getCollection().getId()});
        qs.put(Parameters.profile.toString(), new String[] {question.getProfile()});

        // User entered query
        String q = buildUserQuery();
        if (! q.isEmpty()) {
            qs.put(Parameters.query.toString(), new String[] {buildUserQuery()});
        }
        
        // System generated query
        String s = buildGeneratedQuery();
        
        
        String gscope1 = buildGScope1();
        if (gscope1 != null && ! "".equals(gscope1)) {
            qs.put(Parameters.gscope1.toString(), new String[] {buildGScope1()});
        }
        
        // handle clive constraints if any.
        if(withFacetConstraints) {
            if(question.getFacetCollectionConstraints().isPresent()) {
                qs.remove(QueryProcessorOptionKeys.CLIVE);
                
                List<String> collectionsToRestrictTo = 
                    filterToComponentCollections(collectionsRestrictedByClive(question.getFacetCollectionConstraints().get()));
                    
                // If no collections return then the intersect of user wanted colls and 
                // facet wanted calls is no collection this causes a error page rather than 
                // a zero result page. By setting a query that will never match we get
                // a zero result page.
                if(collectionsToRestrictTo.isEmpty()) {
                    s += " |FunDoesNotExist:searchdisabled |FunDoesNotExist:noCollsLive ";
                } else {
                    qs.put(QueryProcessorOptionKeys.CLIVE, collectionsToRestrictTo.toArray(new String[0]));
                }
            }
        }
            
        // Add the system query last.
        if (! s.isEmpty()) {
            qs.put(Parameters.s.toString(), new String[] {s});
        }
        
        // Remove from query string any parameter that will be passed as an environment variable
        for (String key : question.getEnvironmentVariables().keySet()) {
            // I think it would make more sense to AND clive values rather than have it overwritten.
            // which really means it gets ORed.
            if(QueryProcessorOptionKeys.CLIVE.equals(key)) {
                continue;
            }
            qs.remove(key);
        }
        
        return toQueryString(qs);
        
    }
    
    @SneakyThrows(UnsupportedEncodingException.class)
    private static String toQueryString(Map<String, String[]> map) {
        StringBuffer out = new StringBuffer();
        for(Iterator<Entry<String, String[]>> it = map.entrySet().iterator(); it.hasNext();) {

            Entry<String, String[]> entry = it.next();
            if (entry.getValue() != null) {
                String[] values = entry.getValue();
                for (int i=0; i<values.length; i++) {
                    out.append(entry.getKey()).append("=").append(URLEncoder.encode(values[i], "UTF-8"));
                    if (i+1<values.length) {
                        out.append("&");
                    }
                }
            } else {
                out.append(entry.getKey()).append("=");
            }
        
            if (it.hasNext()) {
                out.append(DELIMITER);
            }
        }
        return out.toString();
    }
    
    /**
     * @return Whether there is a query expression or not (either
     * the user entered query, or the generated query constraints due
     * to faceted nav., etc.) 
     */
    public boolean hasQuery() {
        return buildUserQuery().length() > 0
                || buildGeneratedQuery().length() > 0;
    }
    
    /**
     * Builds the complete query expression, both from the user entered
     * query and the system generated query
     * @return
     */
    public String buildCompleteQuery() {
        String userQuery = buildUserQuery();
        String generatedQuery = buildGeneratedQuery();
        StringBuilder out = new StringBuilder();
        if (! userQuery.isEmpty()) {
            out.append(userQuery);
        }
        
        if (! userQuery.isEmpty() && ! generatedQuery.isEmpty()) {
            out.append(" ");
        }
        
        if (!generatedQuery.isEmpty()) {
            out.append(generatedQuery);
        }
        
        return out.toString();
    }
    
    /**
     * Builds the user entered query terms, coming either from
     * <code>meta_*</code>, <code>query_*</code> parameters.
     * 
     * @return
     */
    public String buildUserQuery() {
        StringBuffer query = new StringBuffer();
        
        if (question.getQuery() != null) {
            query.append(question.getQuery());
        }
        
        if (question.getMetaParameters().size() > 0) {
            // Add meta_* parameters transformed as query expressions
            for (String value : question.getMetaParameters()) {
                query.append(" ").append(value);
            }
        }

        return query.toString().trim();
    }
    
    /**
     * Builds the automatically generated query from various sources
     * like <code>smeta_</code> / <code>squery_*</code> parameters,
     * faceted navigation constraints, etc.
     * 
     * @return
     */
    public String buildGeneratedQuery() {
        StringBuffer query = new StringBuffer();

        if (question.getSystemMetaParameters().size() > 0) {
            // Add smeta_* parameters transformed as query expressions
            for (String value : question.getSystemMetaParameters()) {
                query.append(" ").append(value);
            }
        }
        
        if (withFacetConstraints && question.getFacetsQueryConstraints().size() > 0) {
            // Add query constraints for faceted navigation
            for (String value: question.getFacetsQueryConstraints()) {
                query.append(" ").append(value);
            }
        }
        
        if (question.getRawInputParameters().containsKey(Parameters.s.toString())
            && question.getRawInputParameters().get(Parameters.s.toString()) != null) {
            // Append user-entered system query, if any
            // Concatenate multiple values with space
            for (String value: question.getRawInputParameters().get(Parameters.s.toString())) {
                if (value != null) {
                    query.append(" ").append(value.trim());
                }
            }
        }
        
        return query.toString().trim();
    }
    
    
    private Set<String> collectionsRestrictedByClive(List<String> facetCollectionRestriction) {
        Set<String> collections = new HashSet<>(facetCollectionRestriction);
        Set<String> otherCollectionsToRestrictTo = new HashSet<>();
        
        // We will ensure that the result set is restricted to the intersection of the collections
        // the user wants to restrict to and to the set of the collections the facets want to restrict
        // to.
        
        // The doco for this says that getAdditionalParameters() wont be processed by the modern UI.
        // but it is processed in buildGScope1() below.
        StreamUtils.ofNullable(question.getAdditionalParameters().get(QueryProcessorOptionKeys.CLIVE))
            .forEach(otherCollectionsToRestrictTo::add);
        
        // I think it makes sense to also process the raw input params.
        // in case something sets it here I suspect they intended to reduce to this set.
        StreamUtils.ofNullable(question.getRawInputParameters().get(QueryProcessorOptionKeys.CLIVE))
            .forEach(otherCollectionsToRestrictTo::add);
        
        if(otherCollectionsToRestrictTo.isEmpty()) {
            return collections;
        } else {
            return Sets.intersection(collections, otherCollectionsToRestrictTo);
        }
    }
    
    private List<String> filterToComponentCollections(Set<String> collections) {
        return new ArrayList<>(Sets.intersection(getComponentCollections(), collections));
    }
    
    
    
    public Set<String> getComponentCollections() {
        if(question.getCollection().getConfiguration().getCollectionType() == Type.meta) {
            return new HashSet<>(Arrays.asList(question.getCollection().getMetaComponents()));
        }
        return ImmutableSet.of(question.getCollection().getConfiguration().getCollectionName());
    }
    
    /**
     * Builds <code>gscope1</code> parameter using any existing input parameter combined
     * with faceted navigation gscope constraints.
     */
    private String buildGScope1() {
        String facetGscopeConstraints = question.getFacetsGScopeConstraints();
        // Do we have gscope constraints due to faceted navigation ?
        if ( withFacetConstraints && facetGscopeConstraints!= null && ! "".equals(facetGscopeConstraints)) {
            
            // Do we have other gscope constraints (coming from query string)
            if (question.getAdditionalParameters().get(RequestParameters.GSCOPE1) != null) {
                // Combine them
                return question.getFacetsGScopeConstraints()
                    // Only the [0] value is relevant
                    + (GscopeName.isValidGscope(((Character) facetGscopeConstraints.charAt(facetGscopeConstraints.length()-1)).toString())
                            ? ","
                            : "")
                    + MapUtils.getFirstString(question.getAdditionalParameters(), RequestParameters.GSCOPE1, null)
                    + "+";
            } else {
                return question.getFacetsGScopeConstraints();
            }
        } else {
            if (question.getAdditionalParameters().get(RequestParameters.GSCOPE1) != null) {
                return MapUtils.getFirstString(question.getAdditionalParameters(), RequestParameters.GSCOPE1, null);
            } else {
                return null;
            }
        }        
    }
    
    public static enum Parameters {
        collection,query,profile,gscope1,s;
    }
    
}
