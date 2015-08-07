package com.funnelback.publicui.search.lifecycle.input.processors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Keys;
import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.contentauditor.MapUtil;
import com.funnelback.publicui.contentauditor.MetadataMissingFill;
import com.funnelback.publicui.contentauditor.MissingMetadataFill;
import com.funnelback.publicui.contentauditor.UrlScopeFill;
import com.funnelback.publicui.contentauditor.YearOnlyDateFieldFill;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.GScopeItem;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.QueryItem;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.resource.impl.FacetedNavigationConfigResource;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.search.web.controllers.ContentAuditorController;
import com.funnelback.publicui.xml.FacetedNavigationConfigParser;
import com.funnelback.springmvc.service.resource.ResourceManager;

/**
 * This input processor customises the current request to suit content auditor (if content_auditor is being used).
 * 
 * Customisations performed include
 * <ul>
 *  <li>Running a null query if the query is empty</li>
 *  <li>Using a custom set of facets instead of the default ones</li>
 *  <li>Using a custom set of facets instead of the default ones</li>
 *  <li>Adding some custom metadata labels from the collection config to the response</li>
 *  <li>Adjusting the size and performance characteristics of the request</li>
 *  <li>Adding an extra search customised for generating a large set of collapsed duplicates for reporting</li>
 *  <li>Lots of other minor things...</li>
 * </ul>
 */
@Component("contentAuditorInputProcessor")
public class ContentAuditor extends AbstractInputProcessor {

    /** Maximum DAAT timeout value - 1 hour */
    private static final String DAAT_TIMEOUT_MAX_VALUE = "3600.0";

    /** Metadata field used for dates */
    private static final String DATE_METADATA_FIELD = "d";

    /** How much metadata content auditor will display - 20k ought be enough for anyone. */
    private static final int METADATA_BUFFER_LENGTH_VALUE = 1024 * 20;

    /** Query to run if no query is specified - should return all results */
    private static final String NULL_QUERY = "-FUN:showalldocuments";

    /** 
     * Custom URL parameter used for indicating the duplicate signature to be applied.
     * Keeping this as a unique param makes it easy to clear when needed.
     */
    private static final String DUPLICATE_SIGNATURE_URL_PARAMETER_NAME = "duplicate_signature";

    /** Custom data key used for metadata label information */
    private static final String DISPLAY_METADATA_KEY = "displayMetadata";

    /** Key by which the duplicates extra search is identified */
    private static final String DUPLICATES_EXTRA_SEARCH_KEY = "duplicates";

    /** Resource manger for reading (and caching) config files */
    @Autowired
    @Setter
    protected ResourceManager resourceManager;

    /** Parser for faceted_navigation.xml */
    @Autowired
    @Setter
    private FacetedNavigationConfigParser fnConfigParser;

    /** Reference to translations */
    @Autowired
    I18n i18n;

    /**
     * Customise the incoming request in the ways required for content auditor
     */
    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)
            && searchTransaction.hasQuestion()) {
            
            SearchQuestionType questionType = searchTransaction.getQuestion().getQuestionType();
            
            if (questionType.equals(SearchQuestion.SearchQuestionType.CONTENT_AUDITOR)) {
                SearchQuestion question = searchTransaction.getQuestion();
                customiseMainQuestion(question);

                // We run an extra search with a large num_ranks value to find duplicates
                searchTransaction.addExtraSearch(ContentAuditor.DUPLICATES_EXTRA_SEARCH_KEY, createExtraQuestion(question));
                
                // Add some custom display metadata labels to the data model
                searchTransaction.getResponse().getCustomData().put(ContentAuditor.DISPLAY_METADATA_KEY, readMetadataInfo(question, Keys.ModernUI.ContentAuditor.DISPLAY_METADATA));
            }
        }
    }

    /** Modify the given question to suit content auditor's needs 
     * @param request */
    private void customiseMainQuestion(SearchQuestion question) {
        Config config = question.getCollection().getConfiguration();
        
        // Manipulate the request to suit content auditor
        question.getAdditionalParameters().put(RequestParameters.FULL_MATCHES_ONLY, new String[] {"on"});
        question.getAdditionalParameters().put(RequestParameters.STEM, new String[] {"0"});
        question.getAdditionalParameters().put(RequestParameters.DAAT, new String[] {config.value(Keys.ModernUI.ContentAuditor.DAAT_LIMIT)});
        question.getAdditionalParameters().put(RequestParameters.METADATA_BUFFER_LENGTH, new String[] {Integer.toString(ContentAuditor.METADATA_BUFFER_LENGTH_VALUE)});
        question.getDynamicQueryProcessorOptions().add("-" + QueryProcessorOptionKeys.DAAT_TIMEOUT + "=" + ContentAuditor.DAAT_TIMEOUT_MAX_VALUE);
        question.getDynamicQueryProcessorOptions().add("-" + QueryProcessorOptionKeys.RMC_MAXPERFIELD + "=" + config.value(Keys.ModernUI.ContentAuditor.MAX_METADATA_FACET_CATEGORIES));

        if (question.getRawInputParameters().get(RequestParameters.NUM_RANKS) == null) {
            // Set a default from collection.cfg
            question.getAdditionalParameters().put(RequestParameters.NUM_RANKS, new String[] {config.value(Keys.ModernUI.ContentAuditor.NUM_RANKS)});
        }

        question.getAdditionalParameters().put(RequestParameters.COLLAPSING, new String[] {"off"});

        if (question.getRawInputParameters().get(ContentAuditor.DUPLICATE_SIGNATURE_URL_PARAMETER_NAME) != null) {

        	// User scoped to a duplicate group, so we want to show only that
        	question.getAdditionalParameters().put(
        			RequestParameters.COLLAPSING_SIGNATURE,
        			new String[] {question.getCollection().getConfiguration().value(Keys.ModernUI.ContentAuditor.COLLAPSING_SIGNATURE)});

            question.getRawInputParameters().put(RequestParameters.S, 
                ArrayUtils.add(
                    question.getRawInputParameters().get(RequestParameters.S),
                    question.getInputParameterMap().get(ContentAuditor.DUPLICATE_SIGNATURE_URL_PARAMETER_NAME)
                )
            );
        }

        // Metadata for displaying in the results view
        question.getAdditionalParameters().put(RequestParameters.SUMMARY_MODE, new String[] {"meta"});
        StringBuilder sfValue = new StringBuilder();
        for (Map.Entry<String, String> entry : readMetadataInfo(question, Keys.ModernUI.ContentAuditor.DISPLAY_METADATA).entrySet()) {
            sfValue.append("," + entry.getKey());
        }
        question.getAdditionalParameters().put(RequestParameters.SUMMARY_FIELDS, new String[] {"[" + sfValue.toString() + "]"});

        if (question.getQuery() == null || question.getQuery().length() < 1) {
            question.setQuery(ContentAuditor.NULL_QUERY);
        }

        question.getCollection().setFacetedNavigationLiveConfig(buildFacetConfig(question));
    }

    /** Overwrite the facet config with a custom one */
    private FacetedNavigationConfig buildFacetConfig(SearchQuestion question) {

        List<FacetDefinition> facetDefinitions = new ArrayList<FacetDefinition>();
        
        facetDefinitions.add(createUrlFacetDefinition(i18n.tr("label.uriFacet")));

        facetDefinitions.add(createDateFacetDefinition(i18n.tr("label.dateModifiedFacet")));

        facetDefinitions.add(createMissingMetadataFacetDefinition(i18n.tr("label.missingMetadataFacet")));

        StringBuilder rmcfValue = new StringBuilder();
        for (Map.Entry<String, String> entry : readMetadataInfo(question, Keys.ModernUI.ContentAuditor.FACET_METADATA).entrySet()) {
            facetDefinitions.add(createMetadataFacetDefinition(entry.getValue(), entry.getKey()));
            rmcfValue.append("," + entry.getKey());
        }
        
        String qpOptions = 
            " -" + QueryProcessorOptionKeys.RMCF + "=["+rmcfValue+"]"
            + " -" + QueryProcessorOptionKeys.COUNT_DATES + "=" + ContentAuditor.DATE_METADATA_FIELD
            + " -" + QueryProcessorOptionKeys.COUNT_URLS + "=" + question.getCollection().getConfiguration().value(Keys.ModernUI.ContentAuditor.COUNT_URLS) 
            + " -" + QueryProcessorOptionKeys.COUNTGBITS + "=" + "all";

        // Pull in any query based facets from the index's faceted_navigation.xml file
        if (!question.getInputParameterMap().containsKey(QueryProcessorOptionKeys.VIEW)) {
            question.getInputParameterMap().put(QueryProcessorOptionKeys.VIEW, "live");
        }
        String indexView = question.getInputParameterMap().get(QueryProcessorOptionKeys.VIEW);

        // Read the snapshot's faceted nav config and get any gscope based facets
        File fnConfig = new File(question.getCollection().getConfiguration().getCollectionRoot(), indexView
            + File.separator + DefaultValues.FOLDER_IDX + File.separator
            + Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME);
        try {
            FacetedNavigationConfig base = resourceManager.load(new FacetedNavigationConfigResource(fnConfig, fnConfigParser));
            if (base != null) {
                for (FacetDefinition fd : base.getFacetDefinitions()) {
                    for (CategoryDefinition cd : fd.getCategoryDefinitions()) {
                        if (cd instanceof GScopeItem || cd instanceof QueryItem) {
                            facetDefinitions.add(fd);
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        FacetedNavigationConfig facetedNavigationConfig = new FacetedNavigationConfig(qpOptions, facetDefinitions);
        return facetedNavigationConfig;
    }

    /** 
     * Read all the metadata config settings for a prefix. The are expected to be in the form...
     * 
     * (prefix).(metadataClassName)=(label)
     * 
     * and will be returned as a hash of className to label
     */
    private Map<String, String> readMetadataInfo(SearchQuestion question, String keyPrefix) {
        // Read in the configured metadata classes, sort by keys
        Map<String, String> metadataClassToLabel = new HashMap<>();

        Config config = question.getCollection().getConfiguration();
        for (String key : question.getCollection().getConfiguration().valueKeys()) {
            if (key.startsWith(keyPrefix)) {
                String className = key.substring(keyPrefix.length() + 1 /* Skip the '.' */);
                String label = config.value(key);
                
                if (label.length() > 0) {
                    metadataClassToLabel.put(className, label);
                }
            }
        }
        return MapUtil.sortByValue(metadataClassToLabel);
    }

    /**
     * Creates a date based facet definition with the given label
     */
    private FacetDefinition createDateFacetDefinition(String label) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        YearOnlyDateFieldFill fill = new YearOnlyDateFieldFill();
        fill.setData(ContentAuditor.DATE_METADATA_FIELD);
        fill.setLabel(label);
        fill.setFacetName(label);
        categoryDefinitions.add(fill);
        
        return new FacetDefinition(label, categoryDefinitions);
    }

    /**
     * Creates a date based facet definition with the given label
     */
    private FacetDefinition createMissingMetadataFacetDefinition(String label) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        MissingMetadataFill fill = new MissingMetadataFill();
        fill.setLabel(label);
        fill.setFacetName(label);
        categoryDefinitions.add(fill);
        
        return new FacetDefinition(label, categoryDefinitions);
    }

    /** Creates a URL scope based facet definition */
    private FacetDefinition createUrlFacetDefinition(String label) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        
        UrlScopeFill fill = new UrlScopeFill();
        fill.setLabel(label);
        fill.setFacetName(label);
        categoryDefinitions.add(fill);
        
        FacetDefinition result = new FacetDefinition(label, categoryDefinitions);
        return result;
    }

    /**
     * Creates a metadata field based facet definition with the given label, populated from the given metadataClass
     */
    private FacetDefinition createMetadataFacetDefinition(String label, String metadataClass) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        MetadataFieldFill fill = new MetadataFieldFill();
        fill.setData(metadataClass);
        fill.setLabel(label);
        fill.setFacetName(label);
        categoryDefinitions.add(fill);
        
//        MetadataMissingFill remainder = new MetadataMissingFill();
//        remainder.setData(metadataClass);
//        remainder.setLabel(label);
//        remainder.setFacetName(label);
//        categoryDefinitions.add(remainder);
        
        return new FacetDefinition(label, categoryDefinitions);
    }

    /**
     * Customise the question to suit getting a large number of results to find duplicates
     */
    private SearchQuestion createExtraQuestion(SearchQuestion originalQuestion) {
        SearchQuestion question = new SearchQuestion();
        SearchQuestionBinder.bind(originalQuestion, question);

        question.getAdditionalParameters().putAll(originalQuestion.getAdditionalParameters());

        question.setQuestionType(SearchQuestionType.CONTENT_AUDITOR_DUPLICATES);
        Config config = question.getCollection().getConfiguration();

        question.getAdditionalParameters().put(RequestParameters.COLLAPSING, new String[] {"on"});
        question.getAdditionalParameters().put(RequestParameters.COLLAPSING_SIGNATURE, new String[] {question.getCollection().getConfiguration().value(Keys.ModernUI.ContentAuditor.COLLAPSING_SIGNATURE)});

        question.getAdditionalParameters().put(RequestParameters.NUM_RANKS,
            new String[] { config.value(Keys.ModernUI.ContentAuditor.DUPLICATE_NUM_RANKS) });

        // Speedup settings
        question.getAdditionalParameters().put(RequestParameters.SUMMARY_FIELDS, new String[] {""});
        question.getAdditionalParameters().put(RequestParameters.METADATA_BUFFER_LENGTH, new String[] {"0"});
        question.getDynamicQueryProcessorOptions().add(
             "-" + QueryProcessorOptionKeys.VSIMPLE + "=" + "1" +
            " -" + QueryProcessorOptionKeys.CONTEXTUAL_NAVIGATION + "=" + "0" +
            " -" + QueryProcessorOptionKeys.SPELLING + "=" + "0" + 
            " -" + QueryProcessorOptionKeys.SQE + "=" + "1" +
            " -" + QueryProcessorOptionKeys.SBL + "=" + "1" + 
            " -" + QueryProcessorOptionKeys.SHLM + "=" + "0");
        
        return question;
    }
}
