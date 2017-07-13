package com.funnelback.publicui.test.contentauditor;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.test.search.lifecycle.input.processors.BothFacetedNavigationInputProcessors;
import com.funnelback.publicui.test.search.lifecycle.input.processors.FacetedNavigationAbstractMetadataFieldFillTestMethods;

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
        
        processor = new BothFacetedNavigationInputProcessors();
        processor.switchAllFacetConfigToSelectionAnd(st);
    }
    
}
