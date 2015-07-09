package com.funnelback.publicui.test.search.web.binding;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.test.mock.MockConfigRepository;

public class CollectionEditorTest {

    private CollectionEditor editor;
    private MockConfigRepository repository;
    
    @Before
    public void before() {
        repository = new MockConfigRepository();
        repository.addCollection(new Collection("test1", null));
        repository.addCollection(new Collection("test2", null));
        
        editor = new CollectionEditor(repository);
    }
    
    @Test
    public void test() {
        
        editor.setAsText("test1");
        Assert.assertEquals(repository.getCollection("test1"), editor.getValue());
        Assert.assertEquals("test1", editor.getAsText());
        
        editor.setAsText("test2");
        Assert.assertEquals(repository.getCollection("test2"), editor.getValue());
        Assert.assertEquals("test2", editor.getAsText());
        
        editor.setAsText("invalid-collection");
        Assert.assertEquals(null, editor.getValue());
        
        editor.setAsText("test2,test1");
        Assert.assertEquals(repository.getCollection("test2"), editor.getValue());
        Assert.assertEquals("test2", editor.getAsText());        

    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNull() {
        editor.setAsText(null);
        Assert.assertEquals(null, editor.getValue());
    }
    
}
