package com.funnelback.publicui.test.search.service.config;

import groovy.lang.Script;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.curator.action.DisplayMessage;
import com.funnelback.publicui.search.model.curator.config.ActionSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DefaultConfigRepositoryCollectionTest extends DefaultConfigRepositoryTestBase {

    @Test
    public void testBasic() {
        Collection coll = configRepository.getCollection("config-repository");
        Assert.assertEquals("config-repository", coll.getId());
        Assert.assertEquals("Test config repository", coll.getConfiguration().getServiceName());
    }
    
    @Test
    public void testTextMinerBlacklist() throws IOException {
        Collection coll = configRepository.getCollection("config-repository");
        Assert.assertTrue(coll.getTextMinerBlacklist().isEmpty());
        
        // Create file
        FileUtils.writeStringToFile(new File(TEST_DIR, Files.TEXT_MINER_BLACKLIST), "ab\ncd ef");
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(2, coll.getTextMinerBlacklist().size());
        Assert.assertTrue(coll.getTextMinerBlacklist().contains("ab"));
        Assert.assertTrue(coll.getTextMinerBlacklist().contains("cd ef"));
        
        // Update value
        writeAndTouchFuture(new File(TEST_DIR, Files.TEXT_MINER_BLACKLIST), "ab\ngh-ij");
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(2, coll.getTextMinerBlacklist().size());
        Assert.assertTrue(coll.getTextMinerBlacklist().contains("ab"));
        Assert.assertTrue(coll.getTextMinerBlacklist().contains("gh-ij"));
        
        // Delete file
        new File(TEST_DIR, Files.TEXT_MINER_BLACKLIST).delete();
        coll = configRepository.getCollection("config-repository");
        Assert.assertTrue(coll.getTextMinerBlacklist().isEmpty());
    }
    
    @Test
    public void testHookScripts() throws IOException {
        Collection coll = configRepository.getCollection("config-repository");
        Assert.assertTrue(coll.getHookScriptsClasses().isEmpty());
        
        // Create hook script
        FileUtils.writeStringToFile(new File(TEST_DIR, "hook_"+Hook.pre_datafetch.name()+".groovy"), "print 'hello'");
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(1, coll.getHookScriptsClasses().size());
        Assert.assertTrue(coll.getHookScriptsClasses().containsKey(Hook.pre_datafetch));
        Class<Script> clazz = coll.getHookScriptsClasses().get(Hook.pre_datafetch);
        
        // Second call should yield same object (cached)
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(clazz, coll.getHookScriptsClasses().get(Hook.pre_datafetch));
        
        // Update hook script
        writeAndTouchFuture(new File(TEST_DIR, "hook_"+Hook.pre_datafetch.name()+".groovy"), "print 'world'");
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(1, coll.getHookScriptsClasses().size());
        Assert.assertTrue(coll.getHookScriptsClasses().containsKey(Hook.pre_datafetch));
        Assert.assertNotSame(clazz, coll.getHookScriptsClasses().get(Hook.pre_datafetch));
        
        // Delete hook script
        new File(TEST_DIR, "hook_"+Hook.pre_datafetch.name()+".groovy").delete();
        coll = configRepository.getCollection("config-repository");
        Assert.assertTrue(coll.getHookScriptsClasses().isEmpty());
    }
    
    @Test
    public void testMeta() throws IOException {
        Collection coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(0, coll.getMetaComponents().length);
        
        // Create meta.cfg
        writeAndTouchFuture(new File(TEST_DIR, Files.META_CONFIG_FILENAME), "component-1\ncomponent-2");
        // Still not a meta collection
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(0, coll.getMetaComponents().length);
        
        // Transform to meta
        String content = FileUtils.readFileToString(new File(TEST_DIR, "collection.cfg"));
        content += "\n"+"collection_type=meta";
        writeAndTouchFuture(new File(TEST_DIR, "collection.cfg"), content);
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(Type.meta, coll.getType());
        Assert.assertEquals(2, coll.getMetaComponents().length);
        Assert.assertTrue(ArrayUtils.contains(coll.getMetaComponents(), "component-1"));
        Assert.assertTrue(ArrayUtils.contains(coll.getMetaComponents(), "component-2"));
        
        // Update meta.cfg
        writeAndTouchFuture(new File(TEST_DIR, Files.META_CONFIG_FILENAME), "component-3");
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(1, coll.getMetaComponents().length);
        Assert.assertTrue(ArrayUtils.contains(coll.getMetaComponents(), "component-3"));

        // Delete meta.cfg
        new File(TEST_DIR, Files.META_CONFIG_FILENAME).delete();
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(0, coll.getMetaComponents().length);
    }
    
    @Test
    public void testParametersTransform() throws IOException {
        Collection coll = configRepository.getCollection("config-repository");
        Assert.assertTrue(coll.getParametersTransforms().isEmpty());
        
        // Create cgi_transforms.cfg
        FileUtils.writeStringToFile(new File(TEST_DIR, Files.CGI_TRANSFORM_CONFIG_FILENAME), "a => b=c");
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(1, coll.getParametersTransforms().size());
        
        // Update cgi_transforms.cfg
        writeAndTouchFuture(new File(TEST_DIR, Files.CGI_TRANSFORM_CONFIG_FILENAME), "a => b=c\nA => -B");
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(2, coll.getParametersTransforms().size());
        
        // Delete cgi_transforms.cfg
        new File(TEST_DIR, Files.CGI_TRANSFORM_CONFIG_FILENAME).delete();
        coll = configRepository.getCollection("config-repository");
        Assert.assertTrue(coll.getParametersTransforms().isEmpty());        
    }
    
    @Test
    public void testProfiles() throws IOException {
        Collection coll = configRepository.getCollection("config-repository");
        Assert.assertTrue(coll.getProfiles().isEmpty());
        
        // Create first profile
        new File(TEST_DIR, "profile1").mkdirs();
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(1, coll.getProfiles().size());
        Assert.assertEquals("profile1", coll.getProfiles().get("profile1").getId());
        
        // Create second profile
        new File(TEST_DIR, "profile2").mkdirs();
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(2, coll.getProfiles().size());
        Assert.assertEquals("profile1", coll.getProfiles().get("profile1").getId());
        Assert.assertEquals("profile2", coll.getProfiles().get("profile2").getId());
        
        // Delete first profile
        new File(TEST_DIR, "profile1").delete();
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals(1, coll.getProfiles().size());
        Assert.assertEquals("profile2", coll.getProfiles().get("profile2").getId());
        
        // Create padre-opts file
        Assert.assertNull(coll.getProfiles().get("profile2").getPadreOpts());
        FileUtils.writeStringToFile(new File(TEST_DIR, "profile2/"+Files.PADRE_OPTS), "-rmcfA");
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals("-rmcfA", coll.getProfiles().get("profile2").getPadreOpts());
        
        // Update padre_opts
        writeAndTouchFuture(new File(TEST_DIR, "profile2/"+Files.PADRE_OPTS), "-rmcfABC");
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals("-rmcfABC", coll.getProfiles().get("profile2").getPadreOpts());

        // Delete padre-opts
        new File(TEST_DIR, "profile2/"+Files.PADRE_OPTS).delete();
        coll = configRepository.getCollection("config-repository");
        Assert.assertNull(coll.getProfiles().get("profile2").getPadreOpts());
    }
    
    @Test
    public void testProfilesCurator() throws IOException {
        new File(TEST_DIR, "profile2").mkdirs();
        
        Collection coll = configRepository.getCollection("config-repository");

        String curatorYamlConfig1 = 
            "triggerActions:\n"
            + "  ? !AllQueryWords\n"
            + "    triggerWords:\n"
            + "    - best\n"
            + "    - king\n"
            + "  : actions:\n"
            + "    - !DisplayMessage\n"
            + "      message:\n"
            + "        additionalProperties: null\n"
            + "        category: no-category\n"
            + "        messageHtml: yaml-message1html";
        String curatorYamlConfig2 = curatorYamlConfig1.replace("yaml-message1html", "yaml-message2html");
        File curatorYamlConfigFile = new File(TEST_DIR, "profile2/" + Files.CURATOR_YAML_CONFIG_FILENAME);

        String curatorJsonConfig1 = FileUtils.readFileToString(new File("src/test/resources/dummy-search_home/conf/config-repository/curator-config-test.json"));
        String curatorJsonConfig2 = curatorJsonConfig1.replace("json-message1html", "json-message2html");
        File curatorJsonConfigFile = new File(TEST_DIR, "profile2/" + Files.CURATOR_JSON_CONFIG_FILENAME);

        // Create curator.json file
        Assert.assertNull(coll.getProfiles().get("profile2").getPadreOpts());
        FileUtils.writeStringToFile(curatorJsonConfigFile, curatorJsonConfig1);
        coll = configRepository.getCollection("config-repository");
        System.out.println(coll.getProfiles().get("profile2").getCuratorConfig());
        ActionSet as = coll.getProfiles().get("profile2").getCuratorConfig().getTriggerActions().get(0).getActionSet();
        DisplayMessage dm = (DisplayMessage) as.getActions().get(0);
        Assert.assertEquals("json-message1html", dm.getMessage().getMessageHtml());

        // Update curator.json
        writeAndTouchFuture(curatorJsonConfigFile, curatorJsonConfig2);
        coll = configRepository.getCollection("config-repository");
        as = (ActionSet) coll.getProfiles().get("profile2").getCuratorConfig().getTriggerActions().get(0).getActionSet();
        dm = (DisplayMessage) as.getActions().get(0);
        Assert.assertEquals("json-message2html", dm.getMessage().getMessageHtml());
        
        // Don't delete the JSON file yet. We want to test it's taking precedence
        // over JSON
        
        // Create curator.yaml file
        Assert.assertNull(coll.getProfiles().get("profile2").getPadreOpts());
        FileUtils.writeStringToFile(curatorYamlConfigFile, curatorYamlConfig1);
        
        // JSON still active
        coll = configRepository.getCollection("config-repository");
        
        as = (ActionSet) coll.getProfiles().get("profile2").getCuratorConfig().getTriggerActions().get(0).getActionSet();
        dm = (DisplayMessage) as.getActions().get(0);
        Assert.assertEquals("json-message2html", dm.getMessage().getMessageHtml());

        // Delete JSON
        Assert.assertTrue("Expected successful deletion of " + curatorYamlConfigFile.getAbsolutePath(), curatorJsonConfigFile.delete());
        
        // YAML now applies
        coll = configRepository.getCollection("config-repository");
        as = (ActionSet) coll.getProfiles().get("profile2").getCuratorConfig().getTriggerActions().get(0).getActionSet();
        dm = (DisplayMessage) as.getActions().get(0);
        Assert.assertEquals("yaml-message1html", dm.getMessage().getMessageHtml());

        // Update curator.yaml
        writeAndTouchFuture(curatorYamlConfigFile, curatorYamlConfig2);
        coll = configRepository.getCollection("config-repository");
        ActionSet as2 = (ActionSet) coll.getProfiles().get("profile2").getCuratorConfig().getTriggerActions().get(0).getActionSet();
        DisplayMessage dm2 = (DisplayMessage) as2.getActions().get(0);
        Assert.assertEquals("yaml-message2html", dm2.getMessage().getMessageHtml());

        // Delete curator.yaml
        Assert.assertTrue("Expected successful deletion of " + curatorYamlConfigFile.getAbsolutePath(), curatorYamlConfigFile.delete());
        coll = configRepository.getCollection("config-repository");
        Assert.assertTrue("Expected curatorConfig to be empty", coll.getProfiles().get("profile2").getCuratorConfig()
            .getTriggerActions().isEmpty());
    }
    
    @Test
    public void testProfilesFacetedNav() throws IOException {
        new File(TEST_DIR, "profile2").mkdirs();
        
        Collection coll = configRepository.getCollection("config-repository");
        Assert.assertNull(coll.getProfiles().get("profile2").getFacetedNavConfConfig());
        
        FileUtils.copyFile(new File(TEST_DIR, "fnav-url.cfg"), new File(TEST_DIR, "profile2/"+Files.FACETED_NAVIGATION_CONFIG_FILENAME));

        coll = configRepository.getCollection("config-repository");
        Assert.assertNotNull(coll.getProfiles().get("profile2").getFacetedNavConfConfig());
        Assert.assertEquals("-count_urls 0", coll.getProfiles().get("profile2").getFacetedNavConfConfig().getQpOptions());
        Assert.assertEquals(1, coll.getProfiles().get("profile2").getFacetedNavConfConfig().getFacetDefinitions().size());
        
        // Update faceted nav
        FileUtils.copyFile(new File(TEST_DIR, "fnav-md-url.cfg"), new File(TEST_DIR, "profile2/"+Files.FACETED_NAVIGATION_CONFIG_FILENAME));
        // Force timestamp updated as copy preserve timestamps
        touchFuture(new File(TEST_DIR, "profile2/"+Files.FACETED_NAVIGATION_CONFIG_FILENAME));
        coll = configRepository.getCollection("config-repository");
        Assert.assertNotNull(coll.getProfiles().get("profile2").getFacetedNavConfConfig());
        Assert.assertEquals("-rmcfd -count_urls 0", coll.getProfiles().get("profile2").getFacetedNavConfConfig().getQpOptions());
        Assert.assertEquals(2, coll.getProfiles().get("profile2").getFacetedNavConfConfig().getFacetDefinitions().size());
        
        // Delete faceted nav
        new File(TEST_DIR, "profile2/"+Files.FACETED_NAVIGATION_CONFIG_FILENAME).delete();
        coll = configRepository.getCollection("config-repository");
        Assert.assertNull(coll.getProfiles().get("profile2").getFacetedNavConfConfig());

        // Faceted nav LIVE config
        coll = configRepository.getCollection("config-repository");
        Assert.assertNull(coll.getProfiles().get("profile2").getFacetedNavLiveConfig());
        
        File fnConfig = new File(SEARCH_HOME, "data/config-repository/live/idx/profile2/"+Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME);
        FileUtils.copyFile(new File(TEST_DIR, "fnav-url.cfg"), fnConfig);

        coll = configRepository.getCollection("config-repository");
        Assert.assertNotNull(coll.getProfiles().get("profile2").getFacetedNavLiveConfig());
        Assert.assertEquals("-count_urls 0", coll.getProfiles().get("profile2").getFacetedNavLiveConfig().getQpOptions());
        Assert.assertEquals(1, coll.getProfiles().get("profile2").getFacetedNavLiveConfig().getFacetDefinitions().size());
        
        // Update faceted nav
        FileUtils.copyFile(new File(TEST_DIR, "fnav-md-url.cfg"), fnConfig);
        // Force timestamp updated as copy preserve timestamps
        touchFuture(fnConfig);
        coll = configRepository.getCollection("config-repository");
        Assert.assertNotNull(coll.getProfiles().get("profile2").getFacetedNavLiveConfig());
        Assert.assertEquals("-rmcfd -count_urls 0", coll.getProfiles().get("profile2").getFacetedNavLiveConfig().getQpOptions());
        Assert.assertEquals(2, coll.getProfiles().get("profile2").getFacetedNavLiveConfig().getFacetDefinitions().size());
        
        // Delete faceted nav
        fnConfig.delete();
        coll = configRepository.getCollection("config-repository");
        Assert.assertNull(coll.getProfiles().get("profile2").getFacetedNavLiveConfig());
    }
    
    @Test
    public void testQuicklinks() throws IOException {        
        Collection coll = configRepository.getCollection("config-repository");
        Assert.assertTrue(coll.getQuickLinksConfiguration().isEmpty());
        
        // Create quicklinks
        FileUtils.writeStringToFile(new File(TEST_DIR, Files.QUICKLINKS_CONFIG_FILENAME), "key=value");
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals("value", coll.getQuickLinksConfiguration().get("key"));
        
        // Update quicklinks
        writeAndTouchFuture(new File(TEST_DIR, Files.QUICKLINKS_CONFIG_FILENAME), "key=value\nnew-key=New value");
        coll = configRepository.getCollection("config-repository");
        Assert.assertEquals("value", coll.getQuickLinksConfiguration().get("key"));
        Assert.assertEquals("New value", coll.getQuickLinksConfiguration().get("new-key"));
        
        // Delete quicklinks
        new File(TEST_DIR, Files.QUICKLINKS_CONFIG_FILENAME).delete();
        coll = configRepository.getCollection("config-repository");
        Assert.assertTrue(coll.getQuickLinksConfiguration().isEmpty());
        
    }
    
    @Test
    public void testFacetedNav() throws IOException {
        Collection coll = configRepository.getCollection("config-repository");
        Assert.assertNull(coll.getFacetedNavigationConfConfig());
        
        FileUtils.copyFile(new File(TEST_DIR, "fnav-url.cfg"), new File(TEST_DIR,Files.FACETED_NAVIGATION_CONFIG_FILENAME));
        coll = configRepository.getCollection("config-repository");
        Assert.assertNotNull(coll.getFacetedNavigationConfConfig());
        Assert.assertEquals("-count_urls 0", coll.getFacetedNavigationConfConfig().getQpOptions());
        Assert.assertEquals(1, coll.getFacetedNavigationConfConfig().getFacetDefinitions().size());
        
        // Update faceted nav
        FileUtils.copyFile(new File(TEST_DIR, "fnav-md-url.cfg"), new File(TEST_DIR, Files.FACETED_NAVIGATION_CONFIG_FILENAME));
        // Force timestamp updated as copy preserve timestamps
        touchFuture(new File(TEST_DIR, Files.FACETED_NAVIGATION_CONFIG_FILENAME));
        coll = configRepository.getCollection("config-repository");
        Assert.assertNotNull(coll.getFacetedNavigationConfConfig());
        Assert.assertEquals("-rmcfd -count_urls 0", coll.getFacetedNavigationConfConfig().getQpOptions());
        Assert.assertEquals(2, coll.getFacetedNavigationConfConfig().getFacetDefinitions().size());
        
        // Delete faceted nav
        new File(TEST_DIR, Files.FACETED_NAVIGATION_CONFIG_FILENAME).delete();
        coll = configRepository.getCollection("config-repository");
        Assert.assertNull(coll.getFacetedNavigationConfConfig());

        // Faceted nav LIVE config
        coll = configRepository.getCollection("config-repository");
        Assert.assertNull(coll.getFacetedNavigationLiveConfig());
        
        File fnConfig = new File(SEARCH_HOME, "data/config-repository/live/idx/"+Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME);
        FileUtils.copyFile(new File(TEST_DIR, "fnav-url.cfg"), fnConfig);
        coll = configRepository.getCollection("config-repository");
        Assert.assertNotNull(coll.getFacetedNavigationLiveConfig());
        Assert.assertEquals("-count_urls 0", coll.getFacetedNavigationLiveConfig().getQpOptions());
        Assert.assertEquals(1, coll.getFacetedNavigationLiveConfig().getFacetDefinitions().size());
        
        // Update faceted nav
        FileUtils.copyFile(new File(TEST_DIR, "fnav-md-url.cfg"), fnConfig);
        // Force timestamp updated as copy preserve timestamps
        touchFuture(fnConfig);
        coll = configRepository.getCollection("config-repository");
        Assert.assertNotNull(coll.getFacetedNavigationLiveConfig());
        Assert.assertEquals("-rmcfd -count_urls 0", coll.getFacetedNavigationLiveConfig().getQpOptions());
        Assert.assertEquals(2, coll.getFacetedNavigationLiveConfig().getFacetDefinitions().size());
        
        // Delete faceted nav
        fnConfig.delete();
        coll = configRepository.getCollection("config-repository");
        Assert.assertNull(coll.getFacetedNavigationLiveConfig());    
    }
    
    @Test
    public void testExtraSearches() throws Exception {
        Collection coll = configRepository.getCollection("config-repository");
        Assert.assertNull(configRepository.getExtraSearchConfiguration(coll, "extra-test"));
        
        // Create config
        FileUtils.writeStringToFile(new File(TEST_DIR, "extra_search.extra-test.cfg"), "collection=abc");
        Assert.assertEquals("abc", configRepository.getExtraSearchConfiguration(coll, "extra-test").get("collection"));
        
        // Update config
        writeAndTouchFuture(new File(TEST_DIR, "extra_search.extra-test.cfg"), "collection=abc\ntest=value");
        Assert.assertEquals("abc", configRepository.getExtraSearchConfiguration(coll, "extra-test").get("collection"));
        Assert.assertEquals("value", configRepository.getExtraSearchConfiguration(coll, "extra-test").get("test"));
        
        // Delete config
        new File(TEST_DIR, "extra_search.extra-test.cfg").delete();
        Assert.assertNull(configRepository.getExtraSearchConfiguration(coll, "extra-test"));
    }
    
    @Test
    public void testGetXmlTemplate() throws IOException {
        // No template at the beginning
        Assert.assertNull(configRepository.getXslTemplate("config-repository", "_default"));
        
        File createdXsl = new File(TEST_DIR, "_default/template.xsl");
        createdXsl.mkdirs();
        createdXsl.createNewFile();
        File xsl = configRepository.getXslTemplate("config-repository", "_default");
        Assert.assertNotNull(xsl);
        Assert.assertEquals(createdXsl, xsl);
        Assert.assertNull(configRepository.getXslTemplate("config-repository", "_default_preview"));
        
        createdXsl.delete();
        Assert.assertNull(configRepository.getXslTemplate("config-repository", "_default"));
        Assert.assertNull(configRepository.getXslTemplate("config-repository", "_default_preview"));
        
    }
}