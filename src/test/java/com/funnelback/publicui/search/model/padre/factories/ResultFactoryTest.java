package com.funnelback.publicui.search.model.padre.factories;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.padre.Result;



public class ResultFactoryTest {

    @Test
    public void isDocumentVisibleToUserTestTrue() {
        Map<String, String> data = new HashMap<>();
        data.put(Result.Schema.DOCUMENT_VISIBLE_TO_USER, "true");
        Assert.assertTrue(ResultFactory.isDocumentVisibleToUser(data));
    }
    
    @Test
    public void isDocumentVisibleToUserTestFalse() {
        Map<String, String> data = new HashMap<>();
        data.put(Result.Schema.DOCUMENT_VISIBLE_TO_USER, "false");
        Assert.assertFalse(ResultFactory.isDocumentVisibleToUser(data));
    }
}
