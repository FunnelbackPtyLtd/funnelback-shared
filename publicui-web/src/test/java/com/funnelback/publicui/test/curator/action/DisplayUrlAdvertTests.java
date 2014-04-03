package com.funnelback.publicui.test.curator.action;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.curator.action.DisplayUrlAdvert;
import com.funnelback.publicui.search.model.curator.data.UrlAdvert;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.resource.impl.CuratorYamlConfigResource;

public class DisplayUrlAdvertTests {

    @Test
    public void testDisplayUrlAdvert() {
        UrlAdvert advert = new UrlAdvert();
        advert.setDisplayUrl("uniqueurl");
        DisplayUrlAdvert dua = new DisplayUrlAdvert(advert, true);
        
        SearchTransaction st = ActionTestUtils.runAllPhases(dua);
        
        Assert.assertTrue("Expected url advert to be added to the response", st.getResponse().getCurator().getExhibits().contains(advert));
        Assert.assertEquals("Expected only one exhibit in the response", 1, st.getResponse().getCurator().getExhibits().size());
        Assert.assertEquals("Expected 'uniqueurl' as the list of URLs to remove",  "uniqueurl", st.getQuestion().getAdditionalParameters().get(RequestParameters.REMOVE_URLS)[0]);
    }

    @Test
    public void testDisplayUrlAdvertNoRemove() {
        UrlAdvert advert = new UrlAdvert();
        advert.setDisplayUrl("uniqueurl");
        DisplayUrlAdvert dua = new DisplayUrlAdvert(advert, false);
        
        SearchTransaction st = ActionTestUtils.runAllPhases(dua);
        
        Assert.assertTrue("Expected url advert to be added to the response", st.getResponse().getCurator().getExhibits().contains(advert));
        Assert.assertEquals("Expected only one exhibit in the response", 1, st.getResponse().getCurator().getExhibits().size());
        Assert.assertFalse("Expected no URLs removed",  st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.REMOVE_URLS));
    }

    @Test
    public void testMultipleDisplayUrlAdvert() {
        UrlAdvert advert1 = new UrlAdvert();
        DisplayUrlAdvert dua1 = new DisplayUrlAdvert(advert1, true);

        UrlAdvert advert2 = new UrlAdvert();
        DisplayUrlAdvert dua2 = new DisplayUrlAdvert(advert2, true);

        SearchTransaction st = ActionTestUtils.runAllPhases(dua1);
        ActionTestUtils.runAllPhases(dua2, st, null);
        
        Assert.assertTrue("Expected url advert1 to be added to the response", st.getResponse().getCurator().getExhibits().contains(advert1));
        Assert.assertTrue("Expected url advert2 to be added to the response", st.getResponse().getCurator().getExhibits().contains(advert2));
        Assert.assertEquals("Expected only two exhibits in the response", 2, st.getResponse().getCurator().getExhibits().size());
    }
    
    @Test
    public void testSerializeDisplayUrlAdvert() {
        UrlAdvert advert = new UrlAdvert();
        advert.setTitleHtml("uniquehtml");
        DisplayUrlAdvert dua = new DisplayUrlAdvert(advert, true);

        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(dua);
        Assert.assertTrue("", yaml.contains("uniquehtml"));
    }
}
