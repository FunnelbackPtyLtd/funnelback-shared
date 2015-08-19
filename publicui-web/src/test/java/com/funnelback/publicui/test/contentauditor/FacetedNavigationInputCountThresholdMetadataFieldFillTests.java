package com.funnelback.publicui.test.contentauditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.contentauditor.CountThresholdMetadataFieldFill;
import com.funnelback.publicui.search.lifecycle.input.processors.ContentAuditor;
import com.funnelback.publicui.search.lifecycle.input.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.GScopeItem;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.QueryItem;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.search.service.resource.impl.FacetedNavigationConfigResource;
import com.funnelback.publicui.test.search.lifecycle.input.processors.FacetedNavigationAbstractMetadataFieldFillTestMethods;
import com.funnelback.publicui.test.search.lifecycle.input.processors.FacetedNavigationMetadataFieldFillTests;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationInputCountThresholdMetadataFieldFillTests extends FacetedNavigationAbstractMetadataFieldFillTestMethods {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;

    @Before
    public void before() {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-metadatafieldfill"));
        st = new SearchTransaction(question, null);
        
        // We set this in code here because CountThresholdMetadataFieldFill is
        // not intended to be permitted in the faceted navigation config file.
        question.getCollection().setFacetedNavigationConfConfig(FacetedNavigationCountThresholdMetadataFieldFillTestUtils.buildFacetConfig());
        
        processor = new FacetedNavigation();
    }
    
}
