package com.funnelback.publicui.search.lifecycle.output.processors;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.CollectionId;
import com.funnelback.config.configtypes.mix.ProfileAndCollectionConfigOption;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.keys.Keys;
import com.funnelback.config.keys.types.RelatedDocumentFetchConfig;
import com.funnelback.config.keys.types.RelatedDocumentFetchConfig.RelatedDocumentFetchSourceType;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoResult;
import com.funnelback.publicui.recommender.dataapi.DataAPIConnectorPADRE;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.related.RelatedDocument;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.IndexRepository;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import lombok.Data;
import lombok.Setter;

/**
 * Fetches related documents from the index based on URLs in the
 * metadata of the primary result.
 * 
 * This is more efficient than using extra-searches to achieve the same goal,
 * and easier (and most space efficient) than totally denormalising the 
 * documents before indexing.
 * 
 * TODO
 * - Need to determine how the configuration for this will be exposed
 * - Need to determine how to represent this within the SearchTransaction
 *     (Not use customData, use a type which had the info we want and
 *     not stuff we don't)
 */
@Component("relatedDocumentFetcherOutputProcessor")
public class RelatedDocumentFetcher extends AbstractOutputProcessor {
    
    @Autowired
    DataAPIConnectorPADRE dataAPIConnectorPADRE;
    
    @Autowired
    @Setter private IndexRepository indexRepository;
    
    // TODO - The classes/interface below should eventually move out of home
    @Data
    private class RelationToExpand {
        private final RelationSource relationSource;
        private final String relationTargetKey;
    }
    
    private interface RelationSource {
        public Set<String> getValuesForResult(Result result);
    }
    
    @Data
    private class MetadataRelationSource implements RelationSource {

        private final String metadataClassName;
        
        @Override
        public Set<String> getValuesForResult(Result result) {
            if (result.getMetaData().containsKey(metadataClassName)) {
                return Sets.newHashSet(result.getMetaData().get(metadataClassName));
            } else {
                return Sets.newHashSet();
            }
        }
    }

    @Data
    private class RelatedDataRelationSource implements RelationSource {

        private final String relatedDataKey;
        private final String metadataClassName;
        
        @Override
        public Set<String> getValuesForResult(Result result) {
            if (result.getRelatedDocuments().containsKey(relatedDataKey)) {
                return result.getRelatedDocuments().get(relatedDataKey)
                    .stream()
                    .map((relatedDocument) -> relatedDocument.getMetadata())
                    .filter((metadata) -> metadata.containsKey(metadataClassName))
                    .map((metadata) -> metadata.get(metadataClassName))
                    .filter((value) -> value != null)
                    .collect(Collectors.toSet());
            }
            return Sets.newHashSet();
        }
    }

    @Data
    private class RelatedDataTarget {
        private final Result result;
        private final String relationTargetKey;
    }
    
    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (SearchTransactionUtils.hasResults(searchTransaction)) {
            List<RelationToExpand> relationsToExpand = new ArrayList<>();
            
            ServiceConfigReadOnly config = searchTransaction.getQuestion().getCurrentProfileConfig();

            // First work out which relatedDocumentKey names which are used in the config
            String prefix = Keys.FrontEndKeys.UI.Modern.RELATED_DOCUMENT_FETCH_CONFIG_PREFIX;
            Set<String> relatedDocumentKeys = config.getRawKeys().stream()
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
            
            for (String relatedDocumentKey : relatedDocumentKeys) {
                ProfileAndCollectionConfigOption<RelatedDocumentFetchConfig> key = Keys.FrontEndKeys.UI.Modern.getRelatedDocumentFetchConfigForKey(relatedDocumentKey);
                RelatedDocumentFetchConfig rdfc = config.get(key);
                
                if (rdfc == null) {
                    Log.warn("Missing expected related document fetch config at " + key.getKey() + ". Other settings for this relatedDocumentKey will be ignored.");
                    continue;
                }
                
                if (rdfc.getRelatedDocumentFetchSourceType().equals(RelatedDocumentFetchSourceType.METADATA)) {
                    relationsToExpand.add(new RelationToExpand(new MetadataRelationSource(rdfc.getMetadataKey()), relatedDocumentKey));
                } else if (rdfc.getRelatedDocumentFetchSourceType().equals(RelatedDocumentFetchSourceType.RELATED)) {
                    relationsToExpand.add(new RelationToExpand(new RelatedDataRelationSource(rdfc.getRelatedKey().get(), rdfc.getMetadataKey()), relatedDocumentKey));
                } else {
                    throw new RuntimeException("Unknown type of related document fetching requested - " + rdfc.getRelatedDocumentFetchSourceType());
                }
            }
                    
            SetMultimap<URI, RelatedDataTarget> actionsForThisPass;
            List<Result> results = searchTransaction.getResponse().getResultPacket().getResults();
            
            actionsForThisPass = createActionsForThisPass(results, relationsToExpand);
            
            while (!actionsForThisPass.isEmpty()) {
                List<String> urlsForPadre = actionsForThisPass.keySet().stream().map((uri) -> uri.toString()).collect(Collectors.toList());
                
                DocInfoResult docInfoResult = dataAPIConnectorPADRE.getDocInfoResult(urlsForPadre, searchTransaction.getQuestion().getCollection().getConfiguration());
                Map<URI, DocInfo> relatedData = docInfoResult.asMap();
                
                for (Entry<URI, RelatedDataTarget> action : actionsForThisPass.entries()) {
                    DocInfo relatedDocInfo = relatedData.get(action.getKey());
                    
                    if (relatedDocInfo != null) {
                        if (!action.getValue().getResult().getRelatedDocuments().containsKey(action.getValue().getRelationTargetKey())) {
                            action.getValue().getResult().getRelatedDocuments().put(action.getValue().getRelationTargetKey(), new HashSet<>());
                        }
                        
                        // FIXME - we can't currently tell which component the document comes from in
                        // a meta collection case (i4u doesn't provide that info?) so we'll assume it's
                        // the same as the result, but we need to fix that. See FUN-11445
                        String documentsComponentCollection = action.getValue().getResult().getCollection();
                        
                        action.getValue().getResult().getRelatedDocuments().get(action.getValue().getRelationTargetKey()).add(
                            new RelatedDocument(relatedDocInfo.getUri(), documentsComponentCollection, relatedDocInfo.getMetaData()));
                    }
                }
                
                // Prepare the next pass
                actionsForThisPass = createActionsForThisPass(results, relationsToExpand);
            }
            
            return;
        }
    }

    private SetMultimap<URI, RelatedDataTarget> createActionsForThisPass(List<Result> results,
        List<RelationToExpand> relationsToExpand) {
        SetMultimap<URI, RelatedDataTarget> actionsForThisPass = MultimapBuilder.hashKeys().hashSetValues().build();

        try {
            for(Result r: results) {
                for (RelationToExpand relationToExpand : relationsToExpand) {
                    if (!r.getRelatedDocuments().containsKey(relationToExpand.getRelationTargetKey())) {
                        Set<String> relationSourceValues = relationToExpand.getRelationSource().getValuesForResult(r);
                        for (String relationSourceValue : relationSourceValues) {
                            // AutoRefreshLocalIndexRepository caches these, so I think it's ok to fetch again every time
                            // (we need to because different components of a meta collection may have different seperators)
                            String metadataSeparator = indexRepository.getBuildInfoValue(r.getCollection(), "facet_item_sepchars");
                            
                            String[] urls = relationSourceValue.split(Pattern.quote(metadataSeparator));
                            for (String url : urls) {
                                actionsForThisPass.put(new URI(url), new RelatedDataTarget(r, relationToExpand.getRelationTargetKey()));
                            }
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
