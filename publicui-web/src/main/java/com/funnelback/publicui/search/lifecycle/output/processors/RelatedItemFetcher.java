package com.funnelback.publicui.search.lifecycle.output.processors;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoResult;
import com.funnelback.publicui.recommender.dataapi.DataAPIConnectorPADRE;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.IndexRepository;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

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
@Component("relatedItemFetcherOutputProcessor")
public class RelatedItemFetcher extends AbstractOutputProcessor {
    
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
        public String getValueForResult(Result result);
    };
    
    @Data
    private class MetadataRelationSource implements RelationSource {

        private final String metadataClassName;
        
        @Override
        public String getValueForResult(Result result) {
            return result.getMetaData().get(metadataClassName);
        }
    }

    @Data
    private class RelatedDataRelationSource implements RelationSource {

        private final String relatedDataKey;
        private final String metadataClassName;
        
        @Override
        public String getValueForResult(Result result) {
            if (result.getCustomData().containsKey(relatedDataKey) && result.getCustomData().get(relatedDataKey) instanceof DocInfo) {
                Map<String, String> metadata = ((DocInfo)result.getCustomData().get(relatedDataKey)).getMetaData();
                if (metadata.containsKey(metadataClassName)) {
                    return metadata.get(metadataClassName);
                }
            }
            return null;
        }
    }

    @Data
    private class RelatedDataTarget {
        private final Result result;
        private final String relationTargetKey;
    }
    
    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        List<RelationToExpand> relationsToExpand = new ArrayList<>();
        
        relationsToExpand.add(new RelationToExpand(new MetadataRelationSource("previousMessageUrl"), "previousMessage"));
        relationsToExpand.add(new RelationToExpand(new RelatedDataRelationSource("previousMessage", "previousMessageUrl"), "previousPreviousMessage"));
        
        if (SearchTransactionUtils.hasResults(searchTransaction)) {
            String metadataSeparator = indexRepository.getBuildInfoValue(searchTransaction.getQuestion().getCollection().getId(), "facet_item_sepchars");

            SetMultimap<URI, RelatedDataTarget> actionsForThisPass;
            List<Result> results = searchTransaction.getResponse().getResultPacket().getResults();
            
            actionsForThisPass = createActionsForThisPass(results, relationsToExpand, metadataSeparator);
            
            while (!actionsForThisPass.isEmpty()) {
                List<String> urlsForPadre = actionsForThisPass.keySet().stream().map((uri) -> uri.toString()).collect(Collectors.toList());
                
                DocInfoResult docInfoResult = dataAPIConnectorPADRE.getDocInfoResult(urlsForPadre, searchTransaction.getQuestion().getCollection().getConfiguration());
                Map<URI, DocInfo> relatedData = docInfoResult.asMap();
                
                for (Entry<URI, RelatedDataTarget> action : actionsForThisPass.entries()) {
                    DocInfo relatedDocInfo = relatedData.get(action.getKey());
                    
                    if (relatedDocInfo != null) {
                        // TODO - We ought to add a specific field to put these in, not use customData
                        // TODO - We probably ought to have a specific type instead of DocInfo.
                        //        I did try re-using Result, but has a lot of irrelevant fields, so that's
                        //        not ideal either.
                        //
                        // TODO - Riaan did tell me it would be helpful to him if we included a click-tracking URL
                        action.getValue().getResult().getCustomData().put(action.getValue().getRelationTargetKey(), relatedDocInfo);
                    }
                }
                
                // Prepare the next pass
                actionsForThisPass = createActionsForThisPass(results, relationsToExpand, metadataSeparator);
            }
            
            return;
        }
    }

    private SetMultimap<URI, RelatedDataTarget> createActionsForThisPass(List<Result> results,
        List<RelationToExpand> relationsToExpand, String metadataSeparator) {
        SetMultimap<URI, RelatedDataTarget> actionsForThisPass = MultimapBuilder.hashKeys().hashSetValues().build();

        try {
            for(Result r: results) {
                for (RelationToExpand relationToExpand : relationsToExpand) {
                    if (!r.getCustomData().containsKey(relationToExpand.getRelationTargetKey())) {
                        String relationSourceValue = relationToExpand.getRelationSource().getValueForResult(r);
                        if (relationSourceValue != null) {
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
