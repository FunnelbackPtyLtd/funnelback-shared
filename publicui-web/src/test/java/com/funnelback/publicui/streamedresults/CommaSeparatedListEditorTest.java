package com.funnelback.publicui.streamedresults;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;



public class CommaSeparatedListEditorTest {

    @Test
    public void testWithEncodedCSV() throws Exception {
        // I just verify that I understand the encoding.
        List<String> list = new CommaSeparatedListEditor().parseValue("\"bar,\"\"\",two");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("bar,\"", list.get(0));
        Assert.assertEquals("two", list.get(1));
    }
    
    
}
