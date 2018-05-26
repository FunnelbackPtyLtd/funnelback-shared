package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

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
import com.funnelback.publicui.search.lifecycle.output.processors.RelatedDocumentFetcher;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.related.RelatedDocument;
import com.funnelback.publicui.search.service.IndexRepository;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class RelatedDocumentFetcherTests {

    @Test
    public void testEmptyConfig() throws Exception {
        
        ServiceConfigReadOnly config = Mockito.mock(ServiceConfigReadOnly.class);
        Mockito.when(config.getRawKeys()).thenReturn(Sets.newHashSet());
        
        List<RelationToExpand> relationsToExpand = new RelatedDocumentFetcher().findRelationsToExpand(config);
        
        Assert.assertTrue(relationsToExpand.isEmpty());
    }

    @Test
    public void testConfigReading() throws Exception {
        
        ServiceConfigReadOnly config = Mockito.mock(ServiceConfigReadOnly.class);
        Mockito.when(config.getRawKeys()).thenReturn(Sets.newHashSet(
            "ui.modern.related-document-fetch.parent", 
            "ui.modern.related-document-fetch.peopleLikedByParent"
        ));
        
        Mockito.when(config.get(Keys.FrontEndKeys.UI.Modern.getRelatedDocumentFetchConfigForKey("parent")))
            .thenReturn(new RelatedDocumentFetchConfig(RelatedDocumentFetchSourceType.METADATA, "parent", Optional.empty()));
        Mockito.when(config.get(Keys.FrontEndKeys.UI.Modern.getRelatedDocumentFetchConfigForKey("peopleLikedByParent")))
            .thenReturn(new RelatedDocumentFetchConfig(RelatedDocumentFetchSourceType.RELATED, "likes", Optional.of("parent")));
        
        List<RelationToExpand> relationsToExpand = new RelatedDocumentFetcher().findRelationsToExpand(config);
        
        List<RelationToExpand> expectedRelations = Lists.newArrayList(
            new RelationToExpand(new MetadataRelationSource("parent"), "parent"),
            new RelationToExpand(new RelatedDataRelationSource("parent", "likes"), "peopleLikedByParent")
        );
        
        Assert.assertEquals(expectedRelations, relationsToExpand);
    }

    @Test
    public void testDepthArgument() throws Exception {
        
        ServiceConfigReadOnly config = Mockito.mock(ServiceConfigReadOnly.class);
        Mockito.when(config.getRawKeys()).thenReturn(Sets.newHashSet(
            "ui.modern.related-document-fetch.test", 
            "ui.modern.related-document-fetch.test.depth"
        ));
        
        Mockito.when(config.get(Keys.FrontEndKeys.UI.Modern.getRelatedDocumentFetchConfigForKey("test")))
            .thenReturn(new RelatedDocumentFetchConfig(RelatedDocumentFetchSourceType.RELATED, "metadataKey", Optional.of("relatedKey")));
        Mockito.when(config.get(Keys.FrontEndKeys.UI.Modern.getRelatedDocumentFetchDepthForKey("test")))
            .thenReturn(5);
        
        List<RelationToExpand> relationsToExpand = new RelatedDocumentFetcher().findRelationsToExpand(config);
        
        List<RelationToExpand> expectedRelations = Lists.newArrayList(
            new RelationToExpand(new RelatedDataRelationSource("relatedKey", "metadataKey"), "test0"),
            new RelationToExpand(new RelatedDataRelationSource("test0", "metadataKey"), "test1"),
            new RelationToExpand(new RelatedDataRelationSource("test1", "metadataKey"), "test2"),
            new RelationToExpand(new RelatedDataRelationSource("test2", "metadataKey"), "test3"),
            new RelationToExpand(new RelatedDataRelationSource("test3", "metadataKey"), "test4"),
            new RelationToExpand(new RelatedDataRelationSource("test4", "metadataKey"), "test5")
        );
        
        Assert.assertEquals(expectedRelations, relationsToExpand);
    }

    @Test
    public void testCreateActionsEmpty() throws Exception {
        List<RelationToExpand> relationsToExpand = Lists.newArrayList(
            new RelationToExpand(new MetadataRelationSource("parent"), "parent"),
            new RelationToExpand(new RelatedDataRelationSource("parent", "likes"), "peopleLikedByParent")
        );

        List<Result> results = Lists.newArrayList();
        
        SetMultimap<URI, RelatedDataTarget> actualResult = new RelatedDocumentFetcher().createActionsForThisPass(results, relationsToExpand);
        
        Assert.assertTrue(actualResult.isEmpty());
    }

    @Test
    public void testCreateActions() throws Exception {
        List<RelationToExpand> relationsToExpand = Lists.newArrayList(
            new RelationToExpand(new MetadataRelationSource("parent"), "parent"),
            new RelationToExpand(new RelatedDataRelationSource("cousin", "likes"), "peopleLikedByCousin")
        );

        Result result = new Result();
        result.setCollection("collection");
        result.getMetaData().put("parent", "http://example.com/1|http://example.com/2");

        Map<String, String> cousinMetadata = new HashMap<>();
        cousinMetadata.put("likes", "http://example.com/3");
        result.getRelatedDocuments().put("cousin", Sets.newHashSet(
            new RelatedDocument(
                new URI("http://other.example.com/1"),
                "cousinCollection",
                cousinMetadata
            )
        ));
        
        List<Result> results = Lists.newArrayList(result);
        
        RelatedDocumentFetcher rdf = new RelatedDocumentFetcher();
        
        IndexRepository indexRepository = Mockito.mock(IndexRepository.class);
        Mockito.when(indexRepository.getBuildInfoValue(Mockito.eq("collection"), Mockito.eq("facet_item_sepchars"))).thenReturn("|");
        rdf.setIndexRepository(indexRepository);
        
        SetMultimap<URI, RelatedDataTarget> actualResult = rdf.createActionsForThisPass(results, relationsToExpand);
        
        SetMultimap<URI, RelatedDataTarget> expected = MultimapBuilder.hashKeys().hashSetValues().build();
        expected.put(new URI("http://example.com/1"), new RelatedDataTarget(result, "parent"));
        expected.put(new URI("http://example.com/2"), new RelatedDataTarget(result, "parent"));
        expected.put(new URI("http://example.com/3"), new RelatedDataTarget(result, "peopleLikedByCousin"));
        
        Assert.assertEquals(actualResult, expected);
    }

    @Test
    public void testPerformActions() throws Exception {
        Result result = new Result();
        
        DocInfo mockDocInfo1 = Mockito.mock(DocInfo.class);
        Mockito.when(mockDocInfo1.getUri()).thenReturn(new URI("http://example.com/1"));
        Mockito.when(mockDocInfo1.getMetaData()).thenReturn(ImmutableMap.of("1","1"));

        DocInfo mockDocInfo2 = Mockito.mock(DocInfo.class);
        Mockito.when(mockDocInfo2.getUri()).thenReturn(new URI("http://example.com/2"));
        Mockito.when(mockDocInfo2.getMetaData()).thenReturn(ImmutableMap.of("2","2"));

        DocInfo mockDocInfo3 = Mockito.mock(DocInfo.class);
        Mockito.when(mockDocInfo3.getUri()).thenReturn(new URI("http://example.com/3"));
        Mockito.when(mockDocInfo3.getMetaData()).thenReturn(ImmutableMap.of("3","3"));

        DocInfoResult mockDocInfoResult = Mockito.mock(DocInfoResult.class); // Surprisingly painful to construct these
        Mockito.when(mockDocInfoResult.asMap()).thenReturn(ImmutableMap.of(
            new URI("http://example.com/1"), mockDocInfo1,
            new URI("http://example.com/2"), mockDocInfo2,
            new URI("http://example.com/3"), mockDocInfo3
        ));
        
        RelatedDocumentFetcher rdf = new RelatedDocumentFetcher();
        DataAPIConnectorPADRE dataApiConnectorPadre = Mockito.mock(DataAPIConnectorPADRE.class);
        Mockito
            .when(dataApiConnectorPadre.getDocInfoResult(
                Mockito.any() /* Could confirm the list of URLs here, but the ordering makes it tricky, so I won't */,
                Mockito.any()))
            .thenReturn(mockDocInfoResult);
        rdf.setDataAPIConnectorPADRE(dataApiConnectorPadre);

        SetMultimap<URI, RelatedDataTarget> actions = MultimapBuilder.hashKeys().hashSetValues().build();
        actions.put(new URI("http://example.com/1"), new RelatedDataTarget(result, "a"));
        actions.put(new URI("http://example.com/2"), new RelatedDataTarget(result, "b"));
        actions.put(new URI("http://example.com/3"), new RelatedDataTarget(result, "b"));

        rdf.performActions(null, actions);
        
        Assert.assertEquals(ImmutableSet.of(new RelatedDocument(new URI("http://example.com/1"), null, ImmutableMap.of("1","1"))),
            result.getRelatedDocuments().get("a"));
        
        Assert.assertEquals(ImmutableSet.of(
            new RelatedDocument(new URI("http://example.com/2"), null, ImmutableMap.of("2","2")),
            new RelatedDocument(new URI("http://example.com/3"), null, ImmutableMap.of("3","3"))),
            result.getRelatedDocuments().get("b"));
    }

}