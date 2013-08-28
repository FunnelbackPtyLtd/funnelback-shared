package com.funnelback.curator.action;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.curator.action.data.UrlAdvert;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorConifgResource;

public class DisplayUrlAdvertTests {

    @Test
    public void testDisplayUrlAdvert() {
        UrlAdvert advert = new UrlAdvert();
        DisplayUrlAdvert dua = new DisplayUrlAdvert(advert);
        
        SearchTransaction st = ActionTestUtils.runAllPhases(dua);
        
        Assert.assertTrue("Expected url advert to be added to the response", st.getResponse().getCuratorModel().getExhibits().contains(advert));
        Assert.assertEquals("Expected only one exhibit in the response", 1, st.getResponse().getCuratorModel().getExhibits().size());
    }

    @Test
    public void testMultipleDisplayUrlAdvert() {
        UrlAdvert advert1 = new UrlAdvert();
        DisplayUrlAdvert dua1 = new DisplayUrlAdvert(advert1);

        UrlAdvert advert2 = new UrlAdvert();
        DisplayUrlAdvert dua2 = new DisplayUrlAdvert(advert2);

        SearchTransaction st = ActionTestUtils.runAllPhases(dua1);
        ActionTestUtils.runAllPhases(dua2, st);
        
        Assert.assertTrue("Expected url advert1 to be added to the response", st.getResponse().getCuratorModel().getExhibits().contains(advert1));
        Assert.assertTrue("Expected url advert2 to be added to the response", st.getResponse().getCuratorModel().getExhibits().contains(advert2));
        Assert.assertEquals("Expected only two exhibits in the response", 2, st.getResponse().getCuratorModel().getExhibits().size());
    }
    
    @Test
    public void testSerializeDisplayUrlAdvert() {
        UrlAdvert advert = new UrlAdvert();
        advert.setTitleHtml("uniquehtml");
        DisplayUrlAdvert dua = new DisplayUrlAdvert(advert);

        String yaml = CuratorConifgResource.getYamlObject().dumpAsMap(dua);
        Assert.assertTrue("", yaml.contains("uniquehtml"));
    }
}
