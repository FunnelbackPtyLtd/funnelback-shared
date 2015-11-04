package com.funnelback.publicui.test.spring;

import java.io.File;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.funnelback.common.config.Keys;
import com.funnelback.common.testutils.SearchHomeConfigs;
import com.funnelback.common.testutils.SearchHomeFolders;
import com.funnelback.common.testutils.SearchHomeProvider;

/**
 * A test intended to ensure that the public UI's spring context (the real one, not the test one)
 * can be loaded (i.e. it doesn't reference any missing classes etc).
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
    "file:src/main/webapp/WEB-INF/applicationContext.xml",
    "file:src/main/webapp/WEB-INF/publicui-servlet.xml"
})
public class SpringContextTests {
    // These test set and unset funnelback.installdir, it has to use a external class
    // SpringContextTestHelper to hold what it was set to because it seems static values in this class get reset
    // when spring gets involved.
    
    
    private static final String f = dosetFunnelbackInstallDir();    
    
    private static String dosetFunnelbackInstallDir() {
        Properties props = System.getProperties();
        SpringContextTestHelper.ORIG_INSTALL_DIR = props.getProperty("funnelback.installdir");
        try {
            SearchHomeConfigs searchHomeConfigs = SearchHomeConfigs.getWithDefaults();
            searchHomeConfigs.getGlobalCfgDefault().put(Keys.SERVER_SECRET, "foobar");
            
            File searchHome = SearchHomeProvider.getWritableSearchHomeIHaveToUseStrings("SpringContextTests", 
                "testThatContextLoadedSuccessfully", null, searchHomeConfigs, null);
            new File(SearchHomeFolders.getConfDir(searchHome), "realm.properties").createNewFile();
            SpringContextTestHelper.WAS_SET = true;
            props.setProperty("funnelback.installdir", searchHome.getAbsolutePath());
            
            File modernUIProps = new File(searchHome, "web/conf/modernui/modernui.properties");
            modernUIProps.getParentFile().mkdirs();
            modernUIProps.createNewFile();
        } catch (Exception e) {
            throw new RuntimeException("test setup failure, because search home could not be created", e);
        }
        
        return "";
    }
    
    @AfterClass
    public static void setBackFunnelbackInstallDir() {
        Properties props = System.getProperties();
        if(SpringContextTestHelper.WAS_SET) {
            if(SpringContextTestHelper.ORIG_INSTALL_DIR != null) {
                props.setProperty("funnelback.installdir", SpringContextTestHelper.ORIG_INSTALL_DIR);
            } else {
                props.remove("funnelback.installdir");
            }
        }
    }
    
    /** Mock request provided by the WebAppConfiguration annotation */
    @Autowired
    private MockHttpServletRequest request;

    /** Must set execution context (normally done in the jetty context) */
    @Before
    public void before() {
        request.setAttribute("ExecutionContext", "Public");
    }
    
    /**
     * Test doesn't actually need to do anything (will fail before this is
     * called if the context doesn't load) but a test method must be present for
     * JUnit.
     */
    @Test
    public void testThatContextLoadedSuccessfully() {
    }
    
    
}
