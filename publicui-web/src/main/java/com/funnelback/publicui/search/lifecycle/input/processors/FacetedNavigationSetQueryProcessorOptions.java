package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.qpo.QueryProcessorOptionReducers;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * <p>Set the relevant query processor options depending on the faceted navigation, and current
 * facet selection.</p>
 * 
 * <p>Needs to run after {@link FacetedNavigation} as it expects
 * <code>question.selectedFacets</code> to be populated.</p>
 * 
 * @author nguillaumin@funnelback.com
 * @since 15.8
 *
 */
@Log4j2
@Component("facetedNavigationSetQueryProcessorOptionsInputProcessor")
public class FacetedNavigationSetQueryProcessorOptions extends AbstractInputProcessor {

    /** Access to the QPO reducers */
    @Setter
    private QueryProcessorOptionReducers reducers = new QueryProcessorOptionReducers();

    /** Provides the faceted navigation config, from a question. Used for testing */
    @Setter
    private Function<SearchQuestion, FacetedNavigationConfig> facetedNavigationConfigProvider = (question) ->
        FacetedNavigationUtils.selectConfiguration(question.getCollection(), question.getProfile());

    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)) {
            FacetedNavigationConfig config = facetedNavigationConfigProvider.apply(searchTransaction.getQuestion());

            // Fetch all QPOs set by all the category definitions
            List<QueryProcessorOption<?>> options = config.getFacetDefinitions()
                .stream()
                .flatMap(facetDefinition -> getAllQueryProcessorOptions(searchTransaction.getQuestion(),
                    facetDefinition.getCategoryDefinitions()).stream())
                .collect(Collectors.toList());

            // Merge options as they may be duplicates
            List<String> mergedOptions = getMergedOptions(options);

            // Add options to the list to pass to PADRE
            mergedOptions
                .stream()
                .forEach(s -> searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(s));

            if (!options.isEmpty()) {
                log.debug("Merged query processor options from '{}' to '{}'", options, mergedOptions);
            }
        }
    }

    /**
     * Recursively get all the query processor options set by the category definitions
     * 
     * @param question Search question to get QPOs for
     * @param categoryDefinitions Category definitions to extract the QPOs from, including
     *        sub-categories
     * @return List of QP options
     */
    private List<QueryProcessorOption<?>> getAllQueryProcessorOptions(SearchQuestion question,
        List<CategoryDefinition> categoryDefinitions) {
        return categoryDefinitions.stream()
            .flatMap(categoryDefinition -> {
                return Stream.concat(
                    // QPOs for the current category
                    categoryDefinition.getQueryProcessorOptions(question).stream(),
                    // QPOs for the child categories, recursively
                    getAllQueryProcessorOptions(question, categoryDefinition.getSubCategories()).stream());
            })
            .collect(Collectors.toList());
    }

    /**
     * <p>Merge a list of query processor options. and convert them in a form suitable for passing
     * to PADRE as command-line options</p>
     * 
     * @param options List of options pairs to merge
     * @return Merged options, in a form to use with PADRE (e.g <code>-rmcf=[a,b]</code>)
     */
    private List<String> getMergedOptions(List<QueryProcessorOption<?>> options) {
        return reducers.reduceAllQueryProcessorOptions(options)
            .stream()
            .map(option -> String.format("-%s=%s", option.getLeft(), option.getRight()))
            .collect(Collectors.toList());
    }
}
