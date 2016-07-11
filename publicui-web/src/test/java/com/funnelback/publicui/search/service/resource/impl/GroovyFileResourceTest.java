package com.funnelback.publicui.search.service.resource.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.funnelback.common.testutils.TmpFolderProvider;
public class GroovyFileResourceTest {

    @Rule public TestName testName = new TestName();
    
    @Test
    public void testisScriptTooOldCalled() throws Exception {
        File tmpDir = TmpFolderProvider.getTmpDir(getClass(), testName);
        File myFile = new File(tmpDir, "bar");
        myFile.createNewFile();
        
        GroovyFileResource<Object> resource = spy(new GroovyFileResource<Object>(myFile){
            @Override public Object parse() throws IOException {return null;}
        });
        
        //Here we mock out isScriptTooOld to confirm it is called correctly and the result
        //is respected.
        doReturn(true).when(resource).isScriptTooOld(myFile.lastModified());
        doReturn(false).when(resource).isScriptTooOld(myFile.lastModified()+10000);
        doReturn(false).when(resource).isScriptTooOld(myFile.lastModified()-10000);
        
        Assert.assertTrue(resource.isStale(myFile.lastModified(), null));
        Assert.assertFalse(resource.isStale(myFile.lastModified()+10000, null));
        
        Assert.assertTrue("We should have check with the parent class as well", 
            resource.isStale(myFile.lastModified()-10000, null));
    }
 
    @Test
    public void testIsScriptTooOld() {
        GroovyFileResource<Object> resource = spy(new GroovyFileResource<Object>(mock(File.class)){
            @Override public Object parse() throws IOException {return null;}
        });
        long currentTime =GroovyClassResource.MAX_SCRIPT_AGE * 2; 
        doReturn(currentTime).when(resource).getTime();
        
        
        Assert.assertTrue("The resouce was stored in the cache system more tha 60s ago",
            resource.isScriptTooOld(currentTime - GroovyClassResource.MAX_SCRIPT_AGE - 1));
        
        Assert.assertFalse("The resouce was stored in the cache system less than 60s ago and is still valid",
            resource.isScriptTooOld(currentTime - GroovyClassResource.MAX_SCRIPT_AGE + 1));
        
    }
}
