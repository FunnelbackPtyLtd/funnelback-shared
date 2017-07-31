package com.funnelback.publicui.test.search.lifecycle.input.processors;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationMetadataFieldFillTests extends FacetedNavigationAbstractMetadataFieldFillTestMethods {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;
    
    @Before
    public void before() {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-metadatafieldfill"));
        st = new SearchTransaction(question, null);
        
        processor = new BothFacetedNavigationInputProcessors();
        processor.switchAllFacetConfigToSelectionAnd(st);
    }
    
    
}
