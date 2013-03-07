package com.funnelback.contentoptimiser.test;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.funnelback.contentoptimiser.DefaultMultipleConfigReader;
import com.funnelback.contentoptimiser.MultipleConfigReader;
import com.funnelback.contentoptimiser.test.mock.MockConfigReader;

public class MultipleConfigReaderTest {

    @Test    
    public void testClobber() {
        MultipleConfigReader<String> mcr = new DefaultMultipleConfigReader<String>(new MockConfigReader());
        
        String[] a = {"one.txt","two.txt","three.txt"};
        List<String> fileNames = Arrays.asList(a);
        
        Map<String,String> m = mcr.read(fileNames);

        for(String key : a) {
            Assert.assertEquals(key,m.get(key));    
        }
    }
    
    @Test
    public void testFilesFound () throws FileNotFoundException {
        String folder ="src/test/resources/MultipleConfigReader/";
        String[] existingFiles = { folder + "one.txt",folder + "three.txt"};
        String[] allFileNames = { folder + "one.txt",folder + "two.txt", folder + "three.txt"};
        MultipleConfigReader<String> mcr = new DefaultMultipleConfigReader<String>(new MockConfigReader());
        List<String> fileNames = Arrays.asList(allFileNames);
        Set<String> mustExist = new HashSet<String>(Arrays.asList(existingFiles));
        Map<String,String> m =  mcr.read(fileNames,mustExist);
        
        for(String key : allFileNames) {
            Assert.assertEquals(key,m.get(key.substring(key.lastIndexOf("/") +1)));    
        }
    }
    
    @Test (expected=FileNotFoundException.class)
    public void testFilesNotFound () throws FileNotFoundException {
        String folder ="src/test/resources/MultipleConfigReader/";
        String[] existingFiles = { folder + "two.txt",folder + "four.txt"};
        String[] allFileNames = { folder + "one.txt",folder + "two.txt", folder + "three.txt"};
        MultipleConfigReader<String> mcr = new DefaultMultipleConfigReader<String>(new MockConfigReader());
        List<String> fileNames = Arrays.asList(allFileNames);
        Set<String> mustExist = new HashSet<String>(Arrays.asList(existingFiles));
        Map<String,String> m =  mcr.read(fileNames,mustExist);
        
        for(String key : allFileNames) {
            Assert.assertEquals(key,m.get(key.substring(key.lastIndexOf("/") +1)));    
        }
        
        Assert.fail("Should never get here -- the exception should have been thrown");
    }
}
