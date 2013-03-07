package com.funnelback.publicui.test.search.service.anchors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.anchors.AnchorDescription;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.service.anchors.DefaultAnchorsFetcher;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DefaultAnchorsFetcherTest {

    @Autowired
    private I18n i18n;
    
    @Test
    public void testParseAnchorsToMapParseError() throws IOException {
        AnchorModel model = new AnchorModel();
        DefaultAnchorsFetcher fetcher = new DefaultAnchorsFetcher();
        fetcher.setI18n(i18n);
        ArrayList<String> stream = new ArrayList<String>();
        stream.add(("this is a bad line"));
        
        fetcher.parseAnchorsToMap(model,stream);
        Assert.assertEquals("anchors.parse.failed",model.getError());
    }
    
    @Test 
    public void testParseAnchorsToMap() throws IOException {
        AnchorModel model = new AnchorModel();
        DefaultAnchorsFetcher fetcher = new DefaultAnchorsFetcher();
        fetcher.setI18n(i18n);
        ArrayList<String> stream = new ArrayList<String>(); 
    
        stream.add("00000001 00000002 [k0]three times anchortext");
        stream.add("00000001 00000003 [k0]three  times  anchortext");
        stream.add("00000001 00000004 [k0]three   times   anchortext   ");
        stream.add("00000001 00000005 [k1]three   times   anchortext   ");
        stream.add("00000001 -00000001 [K]click associated query");
        stream.add("00000001 00000000 [k1]one time anchortext");

        
        Map<String,AnchorDescription> m = fetcher.parseAnchorsToMap(model, stream);
        
        // Check that there are three links with the text "three times anchortext" of type 0
        Assert.assertTrue(m.containsKey("[k0]three times anchortext"));
        Assert.assertEquals(3,m.get("[k0]three times anchortext").getInternalLinkCount());
        
        // Check that the three links are 2,3 and 4.
        int i = 2;
        List<String> linksTo = new ArrayList<String>(m.get("[k0]three times anchortext").getLinksTo());
        Collections.sort(linksTo);
        for(String s : linksTo ) {
            Assert.assertEquals(i,Integer.parseInt(s));
            i++;
        }
        
        // Check that there is one link with the text "three times anchortext" of type 1        
        Assert.assertTrue(m.containsKey("[k1]three times anchortext"));
        Assert.assertEquals(1,m.get("[k1]three times anchortext").getInternalLinkCount());
        Assert.assertEquals("1",m.get("[k1]three times anchortext").getLinkType());
        
        Assert.assertTrue(m.containsKey("[k1]one time anchortext"));
        Assert.assertEquals(1,m.get("[k1]one time anchortext").getInternalLinkCount());
        Assert.assertEquals("1",m.get("[k1]one time anchortext").getLinkType());

        Assert.assertTrue(m.containsKey("[K]click associated query"));
        Assert.assertEquals(1,m.get("[K]click associated query").getExternalLinkCount());
        Assert.assertEquals("K",m.get("[K]click associated query").getLinkType());
        
    }
    
}
