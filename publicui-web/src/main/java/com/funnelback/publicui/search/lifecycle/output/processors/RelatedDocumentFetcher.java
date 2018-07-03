package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.net.URI;
import java.net.URISyntaxException;

import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.config.configtypes.mix.ProfileAndCollectionConfigOption;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.keys.Keys;
import com.funnelback.config.keys.types.RelatedDocumentFetchConfig;
import com.funnelback.config.keys.types.RelatedDocumentFetchConfig.RelatedDocumentFetchSourceType;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoResult;
import com.funnelback.publicui.recommender.dataapi.DataAPIConnectorPADRE;
import com.funnelback.publicui.relateddocuments.MetadataRelationSource;
import com.funnelback.publicui.relateddocuments.RelatedDataRelationSource;
import com.funnelback.publicui.relateddocuments.RelatedDataTarget;
import com.funnelback.publicui.relateddocuments.RelationToExpand;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.related.RelatedDocument;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.IndexRepository;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Fetches related documents from the index based on URLs in the
 * metadata of the primary result.
 * 
 * This is more efficient than using extra-searches to achieve the same goal,
 * and easier (and most space efficient) than totally denormalising the 
 * documents before indexing.
 */
@Log4j2
@Component("relatedDocumentFetcherOutputProcessor")
public class RelatedDocumentFetcher extends AbstractOutputProcessor {
    
    @Autowired
    @Setter private DataAPIConnectorPADRE dataAPIConnectorPADRE;
    
    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        log.trace("Starting related document fetching");
        if (SearchTransactionUtils.hasResults(searchTransaction)) {
            ServiceConfigReadOnly config = searchTransaction.getQuestion().getCurrentProfileConfig();
            List<Result> results = searchTransaction.getResponse().getResultPacket().getResults();
            List<RelationToExpand> relationsToExpand = findRelationsToExpand(config);

            log.trace("Got the following relations to expand - " + relationsToExpand);

            SetMultimap<URI, RelatedDataTarget> actionsForThisPass = createActionsForThisPass(results, relationsToExpand);
            
            while (!actionsForThisPass.isEmpty()) {
                log.trace("About to perform the following related document fetching actions - " + actionsForThisPass);
                performActions(searchTransaction.getQuestion().getCollection().getConfiguration(), actionsForThisPass);
                
                // Prepare the next pass
                actionsForThisPass = createActionsForThisPass(results, relationsToExpand);
            }
            log.trace("No more related document fetching actions to perform");
            
            return;
        }
        log.trace("Not running related document fetching because there are no results to examine");
    }

    /**
     * Perform the given set of related-document-loading actions by calling padre-i4u, and then inserting
     * the documents it provides into the appropriate results.
     * 
     * This method forks padre-i4u once to load all the necessary related-document for this pass.
     */
    /* private if not for testing */ public void performActions(Config config, SetMultimap<URI, RelatedDataTarget> actionsForThisPass) {
        List<String> urlsForPadre = actionsForThisPass.keySet().stream().map((uri) -> uri.toString()).collect(Collectors.toList());
        
        // Load up all the URLs we need in a single call to padre-i4u
        // (since it will take multiple URL arguments, and forking it is expensive-ish)
        DocInfoResult docInfoResult = dataAPIConnectorPADRE.getDocInfoResult(urlsForPadre, config);
        Map<URI, DocInfo> relatedData = docInfoResult.asMap();
        
        for (Entry<URI, RelatedDataTarget> action : actionsForThisPass.entries()) {
            DocInfo relatedDocInfo = relatedData.get(action.getKey());
            
            if (relatedDocInfo != null) {
                // Ensure we have an empty mapp we can add to if this is the first we've seen.
                if (!action.getValue().getResult().getRelatedDocuments().containsKey(action.getValue().getRelationTargetKey())) {
                    action.getValue().getResult().getRelatedDocuments().put(action.getValue().getRelationTargetKey(), new HashSet<>());
                }
                
                // FIXME - we can't currently tell which component the document comes from in
                // a meta collection case (i4u doesn't provide that info?) so we'll assume it's
                // the same as the result, but we need to fix that. See FUN-11445
                String documentsComponentCollection = action.getValue().getResult().getCollection();
                
                action.getValue().getResult().getRelatedDocuments().get(action.getValue().getRelationTargetKey()).add(
                    new RelatedDocument(relatedDocInfo.getUri(), relatedDocInfo.getMetaData()));
            }
        }
    }

    /*
     * Load the configured list of relations needing expansion
     */
    /* private if not for testing */ public List<RelationToExpand> findRelationsToExpand(ServiceConfigReadOnly config) {
        List<RelationToExpand> relationsToExpand = new ArrayList<>();
        for (String relatedDocumentKey : findRelatedDocumentKeys(config)) {
            ProfileAndCollectionConfigOption<RelatedDocumentFetchConfig> key = Keys.FrontEndKeys.ModernUi.getRelatedDocumentFetchConfigForKey(relatedDocumentKey);
            RelatedDocumentFetchConfig rdfc = config.get(key);
            
            if (rdfc == null) {
                Log.warn("Missing expected related document fetch config at " + key.getKey() + ". Other settings for this relatedDocumentKey will be ignored.");
                continue;
            }
            
            if (rdfc.getRelatedDocumentFetchSourceType().equals(RelatedDocumentFetchSourceType.METADATA)) {
                relationsToExpand.add(new RelationToExpand(new MetadataRelationSource(rdfc.getMetadataKey()), relatedDocumentKey));
            } else if (rdfc.getRelatedDocumentFetchSourceType().equals(RelatedDocumentFetchSourceType.RELATED)) {
                Integer depth = config.get(Keys.FrontEndKeys.ModernUi.getRelatedDocumentFetchDepthForKey(relatedDocumentKey));
                
                if (depth != null && depth > 1) {
                    // They specified a depth for this config, so we expand that out into individual steps here
                    int count = 0;
                    
                    relationsToExpand.add(new RelationToExpand(new RelatedDataRelationSource(rdfc.getRelatedKey().get(), rdfc.getMetadataKey()), relatedDocumentKey + count));
                    while (count < depth) {
                        relationsToExpand.add(new RelationToExpand(new RelatedDataRelationSource(relatedDocumentKey + count, rdfc.getMetadataKey()), relatedDocumentKey + (count + 1)));
                        count++;
                    }
                } else {
                    relationsToExpand.add(new RelationToExpand(new RelatedDataRelationSource(rdfc.getRelatedKey().get(), rdfc.getMetadataKey()), relatedDocumentKey));                    
                }
            } else {
                throw new RuntimeException("Unknown type of related document fetching requested - " + rdfc.getRelatedDocumentFetchSourceType());
            }
        }
        return relationsToExpand;
    }

    /*
     * Return all the config keys with the RELATED_DOCUMENT_FETCH_CONFIG_PREFIX prefix
     */
    private Set<String> findRelatedDocumentKeys(ServiceConfigReadOnly config) {
        String prefix = Keys.FrontEndKeys.ModernUi.RELATED_DOCUMENT_FETCH_CONFIG_PREFIX;

        return config.getRawKeys().stream()
            .filter((key) -> key.startsWith(prefix))
            .map((key) -> key.substring(prefix.length()))
            .map((key) -> {
                // Strip any suffix
                if (key.indexOf('.') != -1) {
                    key = key.substring(0, key.indexOf('.'));
                }
                return key;
            })
            .collect(Collectors.toSet());
    }

    /**
     * Build a multi-map representing the metadata to fetch from padre-i4u (across the whole result set), and the location to 
     * put the returned metadata.
     * 
     * No action is performed if the target location in a result already contains a related document (which prevents any
     * possibility of infinite loops as long as the relationsToExpand are finite).
     */
    /* private if not for testing */ public  SetMultimap<URI, RelatedDataTarget> createActionsForThisPass(List<Result> results,
        List<RelationToExpand> relationsToExpand) {
        SetMultimap<URI, RelatedDataTarget> actionsForThisPass = MultimapBuilder.hashKeys().hashSetValues().build();

        try {
            for(Result r: results) {
                for (RelationToExpand relationToExpand : relationsToExpand) {
                    if (!r.getRelatedDocuments().containsKey(relationToExpand.getRelationTargetKey())) {
                        Set<String> relationSourceValues = relationToExpand.getRelationSource().getValuesForResult(r);
                        for (String url : relationSourceValues) {
                            actionsForThisPass.put(new URI(url), new RelatedDataTarget(r, relationToExpand.getRelationTargetKey()));
                        }
                    }
                }
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return actionsForThisPass;
    }
}
