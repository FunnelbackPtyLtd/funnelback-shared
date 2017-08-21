package com.funnelback.publicui.test.search.lifecycle.input.processors;

import static com.funnelback.common.facetednavigation.models.FacetConstraintJoin.AND;
import static com.funnelback.common.facetednavigation.models.FacetConstraintJoin.OR;
import static com.funnelback.common.facetednavigation.models.FacetSelectionType.MULTIPLE;
import static com.funnelback.common.facetednavigation.models.FacetSelectionType.SINGLE;
import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_SCOPED_QUERY;
import static com.funnelback.common.facetednavigation.models.FacetValuesOrder.COUNT_DESCENDING;
import static java.util.Arrays.asList;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.CollectionFill;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.TransactionFacetedNavigationConfigHelper;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationCollectionTests {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;
    
    private BothFacetedNavigationInputProcessors processor;
    private SearchTransaction st;

    @Before
    public void before() {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-gscopes"));
        
        
        st = new SearchTransaction(question, null);
        
        processor = new BothFacetedNavigationInputProcessors();
        
        FacetedNavigationConfig facetConfig = new FacetedNavigationConfig(asList(
            new FacetDefinition("Tabs", 
                    asList(new CollectionFill("News", asList("weather", "finance", "fake-news")),
                            new CollectionFill("Social", asList("facebook", "youtube"))), 
                    SINGLE, AND, FROM_SCOPED_QUERY, asList(COUNT_DESCENDING)),
            new FacetDefinition("Social types", 
                    asList(new CollectionFill("Facebook", asList("facebook")), 
                            new CollectionFill("Youtube", asList("youtube"))), 
                    MULTIPLE, OR, FROM_SCOPED_QUERY, asList(COUNT_DESCENDING))
            
            ));
        
        new TransactionFacetedNavigationConfigHelper().updateTheFacetConfigToUse(st, facetConfig);
    }
    
    /**
     * Test what happens within a facet with multiple categories selected.
     */
    @Test
    public void testNewsAndSocialSelected() {
        st.getQuestion().getRawInputParameters().put("f.Tabs|facebook,youtube", new String[]{"Social"});
        st.getQuestion().getRawInputParameters().put("f.Tabs|weather,finance,fake-news", new String[]{"News"});
        processor.processInput(st);
        Assert.assertTrue(st.getQuestion().getFacetCollectionConstraints().isPresent());
        
        List<String> cliveColls = st.getQuestion().getFacetCollectionConstraints().get();
        
        Assert.assertEquals("The intersect of collections of news and social is nothing", 0, cliveColls.size());
    }
    
    /**
     * Test what happens within a facet with multiple categories selected.
     */
    @Test
    public void testFacebookAndYoutubeSelected() {
        st.getQuestion().getRawInputParameters().put("f.Social types|facebook", new String[]{"Facebook"});
        st.getQuestion().getRawInputParameters().put("f.Social types|youtube", new String[]{"Youtube"});
        processor.processInput(st);
        Assert.assertTrue(st.getQuestion().getFacetCollectionConstraints().isPresent());
        
        List<String> cliveColls = st.getQuestion().getFacetCollectionConstraints().get();
        
        Assert.assertTrue(cliveColls.contains("youtube"));
        Assert.assertTrue(cliveColls.contains("facebook"));
        
        Assert.assertEquals("Should be both as the join is a OR", 2, cliveColls.size());
    }
    
    /**
     * Test what happens between facets.
     */
    @Test
    public void testSocialAndYoutubeSelected() {
        st.getQuestion().getRawInputParameters().put("f.Tabs|weather,finance,fake-news", new String[]{"Social"});
        st.getQuestion().getRawInputParameters().put("f.Social types|youtube", new String[]{"Youtube"});
        processor.processInput(st);
        Assert.assertTrue(st.getQuestion().getFacetCollectionConstraints().isPresent());
        
        List<String> cliveColls = st.getQuestion().getFacetCollectionConstraints().get();
        
        Assert.assertTrue(cliveColls.contains("youtube"));
        
        Assert.assertEquals(1, cliveColls.size());
    }
    
    @Test
    public void testNewsSelected() {
        st.getQuestion().getRawInputParameters().put("f.Tabs|weather,finance,fake-news", new String[]{"News"});
        processor.processInput(st);
        Assert.assertTrue(st.getQuestion().getFacetCollectionConstraints().isPresent());
        
        List<String> cliveColls = st.getQuestion().getFacetCollectionConstraints().get();
        
        Assert.assertTrue(cliveColls.contains("weather"));
        Assert.assertTrue(cliveColls.contains("finance"));
        Assert.assertTrue(cliveColls.contains("fake-news"));
        
        Assert.assertEquals(3, cliveColls.size());
    }
    
    @Test
    public void testEmpty() {
        st.getQuestion().getRawInputParameters().put("f.Tabs|weather,finance,fake-news", new String[0]);
        processor.processInput(st);
        Assert.assertFalse(st.getQuestion().getFacetCollectionConstraints().isPresent());
        

        st.getQuestion().getRawInputParameters().put("f.Tabs|weather,finance,fake-news", new String[]{""});
        processor.processInput(st);
        Assert.assertFalse(st.getQuestion().getFacetCollectionConstraints().isPresent());
    }
    
    
}
