package com.funnelback.publicui.search.service.resource.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.funnelback.common.testutils.TmpFolderProvider;
public class GroovyClassResourceTest {

    @Rule public TestName testName = new TestName();
    
    @Test
    public void testisScriptTooOldCalled() throws Exception {
        File tmpDir = TmpFolderProvider.getTmpDir(getClass(), testName);
        File myFile = new File(tmpDir, "bar");
        myFile.createNewFile();
        
        GroovyClassResource<Object> resource = spy(new GroovyClassResource<>(myFile, "", myFile));
        
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
        GroovyClassResource<Object> resource = spy(new GroovyClassResource<>(mock(File.class), "", mock(File.class)));
        long currentTime =GroovyClassResource.MAX_SCRIPT_AGE * 2; 
        doReturn(currentTime).when(resource).getTime();
        
        
        Assert.assertTrue("The resouce was stored in the cache system more tha 60s ago",
            resource.isScriptTooOld(currentTime - GroovyClassResource.MAX_SCRIPT_AGE - 1));
        
        Assert.assertFalse("The resouce was stored in the cache system less than 60s ago and is still valid",
            resource.isScriptTooOld(currentTime - GroovyClassResource.MAX_SCRIPT_AGE + 1));
        
    }
}
