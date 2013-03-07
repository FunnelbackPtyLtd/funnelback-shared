package com.funnelback.contentoptimiser.test;

import java.io.File;
import java.io.FileNotFoundException;

import javax.annotation.Resource;

import org.apache.commons.exec.OS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.contentoptimiser.fetchers.DocFromCache;
import com.funnelback.contentoptimiser.fetchers.impl.DefaultDocFromCache;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.index.AutoRefreshLocalIndexRepository;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.utils.CgiRunnerFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DefaultDocFromCacheTest {

    @Resource(name="autoRefreshLocalIndexRepository")
    private AutoRefreshLocalIndexRepository indexRepository;

    @Resource(name="mockConfigRepository")
    private MockConfigRepository configRepository;
    
    
    private CgiRunnerFactory cgiRunnerFactory;
    
    @Before
    public void before() throws Exception {
        configRepository = new MockConfigRepository();
        configRepository.addCollection(
                new Collection("data-repository",
                        new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), "data-repository")
                            .setValue("collection_root",
                                    new File("src/test/resources/dummy-search_home/data/data-repository/").getAbsolutePath() )));
        indexRepository.setConfigRepository(configRepository);
        cgiRunnerFactory = new CgiRunnerFactory();
    }
    
    @Test
    public void testOptionsFromFedGov() {
        String[] originalArgs = {
                "-F/opt/funnelback/data/fed-gov/live/idx_reindex/index.click.anchors.gz",
                "-F/opt/funnelback/conf/fed-gov/index.click.anchors.gz",
                "-ifb",
                "-MWIPD20000",
                "-noaltanx",
                "-nosrcanx",
                "-W20000",
                "-big7",
                "-cleanup",
                "-hashlog",
                "-MMF/opt/funnelback/conf/fed-gov/metamap.cfg",
                "-EM/opt/funnelback/conf/fed-gov/external_metadata.cfg",
                "-XMF/opt/funnelback/conf/fed-gov/xml.cfg",            
        };
        
        String[] expectedArgs = {
                "-MWIPD20000",
                "-noaltanx",
                "-nosrcanx",
                "-cleanup",
                "-hashlog",
                "-MMF/opt/funnelback/conf/fed-gov/metamap.cfg",
                "-EM/opt/funnelback/conf/fed-gov/external_metadata.cfg",
                "-XMF/opt/funnelback/conf/fed-gov/xml.cfg",
                "-small", 
                "-show_each_word_to_file",
                "-noank_record"
        };
        
        DocFromCache dFromC = new DefaultDocFromCache();
        
        Assert.assertArrayEquals(expectedArgs, dFromC.getArgsForSingleDocument(originalArgs));
        
    }
    
    @Test
    public void testGetDocumentForks() throws FileNotFoundException, EnvironmentVariableException {
        File searchHome = new File("src/test/resources/dummy-search_home");
        DefaultDocFromCache dFromC = new DefaultDocFromCache();
        dFromC.setSearchHome(searchHome);
        dFromC.setIndexRepository(indexRepository);
        dFromC.setConfigRepository((ConfigRepository)configRepository);
        dFromC.setCgiRunnerFactory(cgiRunnerFactory);
        String ext = ".sh";
        if (OS.isFamilyWindows()) {
            ext = ".bat";
        }        
        String idx = "mock-padre-iw" + ext;
        
        String cacheCgi = "cache.cgi";        
        
        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("testGetDocumentForks",
                new NoOptionsConfig(searchHome, "testGetDocumentForks")
                    .setValue("indexer", idx)
                    .setValue(Keys.UI_CACHE_LINK, cacheCgi)
                    .setValue(Keys.COLLECTION_ROOT,
                            new File(searchHome, "data" + File.separator + "data-repository").toString())));
        
        ContentOptimiserModel comparison = new ContentOptimiserModel();
        comparison.setSelectedDocument(new Result(null, null, cacheCgi, cacheCgi, null, cacheCgi, cacheCgi, cacheCgi, null, null, cacheCgi, null, null, null, null, null, cacheCgi, cacheCgi, null, cacheCgi));
        
        dFromC.getDocument(comparison, "cache-url", qs.getCollection().getConfiguration(),"data-repository");
        Assert.assertTrue("Unexpected messages: " + comparison.getMessages().toString(),comparison.getMessages().isEmpty());
    }
}
