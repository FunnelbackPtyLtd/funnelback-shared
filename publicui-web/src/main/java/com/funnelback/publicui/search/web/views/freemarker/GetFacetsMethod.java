package com.funnelback.publicui.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.funnelback.common.function.StreamUtils;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchResponse;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.utility.DeepUnwrap;
import static com.funnelback.common.function.Predicates.not;

/**
 * Returns facets with the given names in the order given, If the second argument is missing
 * or the empty string all facets are returned.
 * 
 * The first argument is the 'response' the second argument is a optional list of comma separated
 * names of facets that should be shown. If this list is "," no facets will be returned, this
 * is to get around freemarker not permitting null in macro context.
 */
public class GetFacetsMethod extends AbstractTemplateMethod {
    
    public static final String NAME = "getFacets";

    public GetFacetsMethod() {
        super(1, 1, false);
    }

    @Override
    public List<Facet> execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        SearchResponse st = (SearchResponse) unwrapArgument(arguments.get(0));
        
        final List<Facet> facets = Optional.ofNullable(st)
            //.map(SearchTransaction::getResponse)
            .map(SearchResponse::getFacets)
            .map(Collections::unmodifiableList)
            .orElse(Collections.emptyList());
        
        if(arguments.size() == 1) {
            return facets;
        }
        
        String facetsWantedString = ((TemplateScalarModel) arguments.get(1)).getAsString();
        if("".equals(facetsWantedString)) {
            return facets;
        }
        
        List<String> wantedFacetNames = StreamUtils.ofNullable(facetsWantedString.split(","))
                .map(String::trim)
                .filter(not(String::isEmpty))
                .collect(Collectors.toList());
        
        List<Facet> wantedFacets = new ArrayList<>();
        
        for(String facetName : wantedFacetNames) {
            facets.stream()
                .filter(f -> facetName.equals(f.getName()))
                .forEach(wantedFacets::add);
        }
        
        return Collections.unmodifiableList(wantedFacets);
    }
    
    protected Object unwrapArgument(Object arg) throws TemplateModelException {
        return DeepUnwrap.permissiveUnwrap((TemplateModel) arg);
    }

}
