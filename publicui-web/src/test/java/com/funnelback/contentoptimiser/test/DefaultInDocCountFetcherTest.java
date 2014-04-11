package com.funnelback.contentoptimiser.test;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.contentoptimiser.fetchers.impl.DefaultInDocCountFetcher;
import com.funnelback.contentoptimiser.utils.PanLook;
import com.funnelback.contentoptimiser.utils.PanLookFactory;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import junit.framework.Assert;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DefaultInDocCountFetcherTest {


    @Autowired
    private I18n i18n;

    @Test
    public void testSeriousError() throws FileNotFoundException, EnvironmentVariableException {
        Properties properties = new Properties();
        properties.put("log4j.rootLogger","FATAL");
        PropertyConfigurator.configure(properties);
        
        DefaultInDocCountFetcher fetcher = new DefaultInDocCountFetcher();
        ContentOptimiserModel comparison = new ContentOptimiserModel();
        Collection collection = new Collection("testcollection", new NoOptionsConfig("dummy"));
        fetcher.setI18n(i18n);
        fetcher.setPanLookFactory(getMockExplodingPanLookFactory());
        Map<String,Integer> termWeights = fetcher.getTermWeights(comparison, "testterm",collection.getId());
        Assert.assertEquals("Should have no term weights if pan-look exploded",0,termWeights.size());
        Assert.assertEquals("Should have only one error message",1,comparison.getMessages().size());
        Assert.assertEquals("Error message should be correct",i18n.tr("error.obtainingTermWeights"),comparison.getMessages().get(0));
    }
    
    @Test
    public void testParseError() throws FileNotFoundException, EnvironmentVariableException {
        DefaultInDocCountFetcher fetcher = new DefaultInDocCountFetcher();
        ContentOptimiserModel comparison = new ContentOptimiserModel();
        Collection collection = new Collection("testcollection", new NoOptionsConfig("dummy"));
        fetcher.setI18n(i18n);
        fetcher.setPanLookFactory(getMockParsingErrorPanLookFactory());
        Map<String,Integer> termWeights = fetcher.getTermWeights(comparison, "testterm",collection.getId());
        Assert.assertEquals("term weights should only contain three entries",3,termWeights.size());
        Assert.assertEquals("General term weight (_) should be correct",5,termWeights.get("_").intValue());
        Assert.assertEquals("Anchor weight (k) should be correct",1,termWeights.get("k").intValue());
        Assert.assertEquals("Title weight (t) should be correct",2,termWeights.get("t").intValue());
        Assert.assertEquals("Should have two error message",2,comparison.getMessages().size());
        Assert.assertEquals("Error message should be correct",i18n.tr("error.parsingTermWeights"),comparison.getMessages().get(0));
        Assert.assertEquals("Error message should be correct",i18n.tr("error.parsingTermWeights"),comparison.getMessages().get(1));
    }

    @Test
    public void testFetcher() throws FileNotFoundException, EnvironmentVariableException {
        DefaultInDocCountFetcher fetcher = new DefaultInDocCountFetcher();
        
        ContentOptimiserModel comparison = new ContentOptimiserModel();
        
        Collection collection = new Collection("testcollection", new NoOptionsConfig("dummy")); 
        fetcher.setPanLookFactory(getMockPanLookFactory());
        Map<String,Integer> termWeights = fetcher.getTermWeights(comparison, "testterm",collection.getId());
        
        Assert.assertEquals("term weights should only contain three entries",3,termWeights.size());
        Assert.assertEquals("General term weight (_) should be correct",5,termWeights.get("_").intValue());
        Assert.assertEquals("Anchor weight (k) should be correct",1,termWeights.get("k").intValue());
        Assert.assertEquals("Title weight (t) should be correct",2,termWeights.get("t").intValue());
    }

    private PanLookFactory getMockPanLookFactory() {
        return new PanLookFactory() {
            
            @Override
            public PanLook getPanLookForLex(File sortedFile, String word)
                    throws IOException {
                Assert.assertEquals("Requested word should be correct","testterm", word);
                return new PanLook() {
                    
                    @Override
                    public Iterator<String> iterator() {
                        String[] array = {"wood   6  5",
                                        "wood k   1  1",
                                        "wood t   3  2"};
                        
                        return (Arrays.asList(array)).iterator();
                    }

                    @Override
                    public void close() throws IOException {
                        // Nothing to close
                    }
                };
            }
            
            @Override
            public PanLook getPanLook(File sortedFile, String prefix)
                    throws IOException {
                Assert.fail("Shouldn't call this method when obtaining panlook in termWeightFetcher");
                return null;
            }
        };
    }
    
    private PanLookFactory getMockParsingErrorPanLookFactory() {
        return new PanLookFactory() {
            
            @Override
            public PanLook getPanLookForLex(File sortedFile, String word)
                    throws IOException {
                Assert.assertEquals("Requested word should be correct","testterm", word);
                return new PanLook() {
                    
                    @Override
                    public Iterator<String> iterator() {
                        String[] array = {"wood   6  5",
                                        "wood k   1  1",
                                        "this linewo n'tparse",
                                        "wood t   3  2",
                                        "thislinewon'tparseeither",};
                        
                        return (Arrays.asList(array)).iterator();
                    }

                    @Override
                    public void close() throws IOException {
                        // Nothing to close
                    }
                };
            }
            
            @Override
            public PanLook getPanLook(File sortedFile, String prefix)
                    throws IOException {
                Assert.fail("Shouldn't call this method when obtaining panlook in termWeightFetcher");
                return null;
            }
        };
    }

    
    private PanLookFactory getMockExplodingPanLookFactory() {
        
        return new PanLookFactory() {
            
            @Override
            public PanLook getPanLookForLex(File sortedFile, String word)
                    throws IOException {
                throw new IOException("This is an expected exception during the test!");
            }
            
            @Override
            public PanLook getPanLook(File sortedFile, String prefix)
                    throws IOException {
                Assert.fail("Shouldn't call this method when obtaining panlook in termWeightFetcher");
                return null;
            }
        };
    }

}
