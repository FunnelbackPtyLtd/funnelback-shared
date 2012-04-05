package com.funnelback.publicui.test.search.web.binding;

import junit.framework.Assert;

import org.junit.Test;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.test.mock.MockConfigRepository;

public class CollectionEditorTest {

	@Test
	public void test() {
		MockConfigRepository repository = new MockConfigRepository();
		repository.addCollection(new Collection("test1", null));
		repository.addCollection(new Collection("test2", null));
		
		CollectionEditor editor = new CollectionEditor(repository);
		
		editor.setAsText("test1");
		Assert.assertEquals(repository.getCollection("test1"), editor.getValue());
		Assert.assertEquals("test1", editor.getAsText());
		
		editor.setAsText("test2");
		Assert.assertEquals(repository.getCollection("test2"), editor.getValue());
		Assert.assertEquals("test2", editor.getAsText());
		
		editor.setAsText(null);
		Assert.assertEquals(null, editor.getValue());
		
		editor.setAsText("invalid-collection");
		Assert.assertEquals(null, editor.getValue());
		
		editor.setAsText("test2,test1");
		Assert.assertEquals(repository.getCollection("test2"), editor.getValue());
		Assert.assertEquals("test2", editor.getAsText());		

	}
	
}
