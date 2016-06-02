package com.funnelback.publicui.search.service.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.metamapcfg.MetaDataType;
import com.funnelback.common.config.metamapcfg.MetaMapCfgEntry;
import com.funnelback.common.config.metamapcfg.MetaMapCfgMarshaller;
import com.funnelback.common.config.xmlcfg.XmlCfgConfig;
import com.funnelback.common.config.xmlcfg.XmlCfgEntry;
import com.funnelback.common.config.xmlcfg.XmlCfgMarshaller;
import com.funnelback.publicui.search.model.collection.Collection;

public class DefaultDLSEnabledCheckTest {

    @Rule public TestName testName = new TestName();
    
    @Test
    public void isDLSEnabledForComponentTestConfig() {
        Config config = mock(Config.class);
        when(config.getCollectionType()).thenReturn(Type.web);
        Collection collection = mock(Collection.class);
        when(collection.getConfiguration()).thenReturn(config);
        Assert.assertTrue("Are we not checking securityConfiguredInConfig for security setup", 
            spyDefaultDLSEnabledCheck(true, false, false).isDLSEnabled(collection));
    }
    
    @Test
    public void isDLSEnabledForComponentTestMetamap() {
        Config config = mock(Config.class);
        when(config.getCollectionType()).thenReturn(Type.web);
        Collection collection = mock(Collection.class);
        when(collection.getConfiguration()).thenReturn(config);
        Assert.assertTrue("Are we not checking securityDefinedInMetaMapCfg for security setup", 
            spyDefaultDLSEnabledCheck(false, true, false).isDLSEnabled(collection));
    }
    
    @Test
    public void isDLSEnabledForComponentTestXmlCfg() {
        Config config = mock(Config.class);
        when(config.getCollectionType()).thenReturn(Type.web);
        Collection collection = mock(Collection.class);
        when(collection.getConfiguration()).thenReturn(config);
        Assert.assertTrue("Are we not checking securityDefinedInXmlCfg for security setup", 
            spyDefaultDLSEnabledCheck(false, false, true).isDLSEnabled(collection));
    }
    
    
    @Test
    public void isDLSEnabledForComponentTest() {
        Config config = mock(Config.class);
        when(config.getCollectionType()).thenReturn(Type.web);
        Collection collection = mock(Collection.class);
        when(collection.getConfiguration()).thenReturn(config);
        Assert.assertFalse(spyDefaultDLSEnabledCheck(false, false, false).isDLSEnabled(collection));
    }
    
    @Test
    public void securityConfiguredInConfigNotConfigured() {
        Config config = mock(Config.class);
        when(config.value(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, "")).thenReturn("");
        when(config.value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, "")).thenReturn("");

        DefaultDLSEnabledChecker dlsEnabledCheck = new DefaultDLSEnabledChecker();
        Assert.assertFalse(dlsEnabledCheck.securityConfiguredInConfig(config));
    }
    
    
    @Test
    public void securityConfiguredInConfigDLSDisabled() {
        Config config = mock(Config.class);
        when(config.value(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, "")).thenReturn("");
        when(config.value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, "")).thenReturn("disabled");

        DefaultDLSEnabledChecker dlsEnabledCheck = new DefaultDLSEnabledChecker();
        Assert.assertFalse(dlsEnabledCheck.securityConfiguredInConfig(config));
    }
    
    @Test
    public void securityConfiguredInConfigTestEarlyBinding() {
        Config config = mock(Config.class);
        when(config.value(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, "")).thenReturn("something");
        when(config.value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, "")).thenReturn("");

        DefaultDLSEnabledChecker dlsEnabledCheck = new DefaultDLSEnabledChecker();
        Assert.assertTrue(dlsEnabledCheck.securityConfiguredInConfig(config));
    }
    
    
    @Test
    public void securityConfiguredInConfigTestDLS() {
        Config config = mock(Config.class);
        when(config.value(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, "")).thenReturn("");
        when(config.value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, "")).thenReturn("something");

        DefaultDLSEnabledChecker dlsEnabledCheck = new DefaultDLSEnabledChecker();
        Assert.assertTrue(dlsEnabledCheck.securityConfiguredInConfig(config));
    }
    
    @Test
    public void securityDefinedInMetaMapCfgTestSecurityFieldDefined() throws Exception { 
        securityDefinedInMetaMapCfgTest(MetaDataType.SECURITY, true);
    }
    
    @Test
    public void securityDefinedInMetaMapCfgTestNoSecurityFieldDefined() throws Exception { 
        securityDefinedInMetaMapCfgTest(MetaDataType.GEOSPATIAL, false);
    }
    
    public void securityDefinedInMetaMapCfgTest(MetaDataType metaDataType, boolean isSecured) throws Exception { 
        MetaMapCfgMarshaller mockMarshaller = mock(MetaMapCfgMarshaller.class);
        when(mockMarshaller.unMarshal(Matchers.any())).thenAnswer(new Answer<List<MetaMapCfgEntry>>() {

            @Override
            public List<MetaMapCfgEntry> answer(InvocationOnMock invocation) throws Throwable {
                byte[] arg = (byte[]) invocation.getArguments()[0];
                Assert.assertEquals("yep", new String(arg, "UTF-8").trim());
                List<MetaMapCfgEntry> entries = new ArrayList<>();
                entries.add(new MetaMapCfgEntry("d", metaDataType, "ss"));
                return entries;
            }
            
        });
        File workDir = new File("target" + File.separator + "workdir" + testName.getClass().getSimpleName() + "-" + testName.getMethodName());
        workDir.mkdirs();
        
        DefaultDLSEnabledChecker dlsEnabledCheck = new DefaultDLSEnabledChecker();
        
        Config config = mock(Config.class);
        when(config.getSearchHomeDir()).thenReturn(workDir);
        when(config.getCollectionName()).thenReturn("coll");
        
        File metamapcfg = new File(workDir, "conf" + File.separator + "coll" + File.separator + "metamap.cfg");
        metamapcfg.getParentFile().mkdirs();
        FileUtils.writeStringToFile(metamapcfg, "yep");
        
        dlsEnabledCheck.setMetaMapCfgMarshaller(mockMarshaller);
        
        Assert.assertTrue(dlsEnabledCheck.securityDefinedInMetaMapCfg(config) == isSecured);
    }
    
    @Test
    public void securityDefinedInMetaMapCfgTestNoMetaMapCfg() throws Exception { 
        MetaMapCfgMarshaller mockMarshaller = mock(MetaMapCfgMarshaller.class);
        when(mockMarshaller.unMarshal(Matchers.any())).thenAnswer(new Answer<List<MetaMapCfgEntry>>() {

            @Override
            public List<MetaMapCfgEntry> answer(InvocationOnMock invocation) throws Throwable {
                Assert.fail("No metamap cfg should not have been called");
                return null;
            }
            
        });
        File workDir = new File("target" + File.separator + "workdir" + testName.getClass().getSimpleName() + "-" + testName.getMethodName());
        workDir.mkdirs();
        
        DefaultDLSEnabledChecker dlsEnabledCheck = new DefaultDLSEnabledChecker();
        
        Config config = mock(Config.class);
        when(config.getSearchHomeDir()).thenReturn(workDir);
        when(config.getCollectionName()).thenReturn("coll");
        
        File metamapcfg = new File(workDir, "conf" + File.separator + "coll" + File.separator + "metamap.cfg");
        metamapcfg.getParentFile().mkdirs();
        Assert.assertFalse(metamapcfg.exists());
        
        
        dlsEnabledCheck.setMetaMapCfgMarshaller(mockMarshaller);
        
        Assert.assertFalse(dlsEnabledCheck.securityDefinedInMetaMapCfg(config));
    }
    
    @Test
    public void securityDefinedInXmlCfgTestSecurityFieldDefined() throws Exception { 
        securityDefinedInXmlCfgTest(MetaDataType.SECURITY, true);
    }
    
    @Test
    public void securityDefinedInXmlCfgTestNoSecurityFieldDefined() throws Exception { 
        securityDefinedInXmlCfgTest(MetaDataType.GEOSPATIAL, false);
    }
    
    public void securityDefinedInXmlCfgTest(MetaDataType metaDataType, boolean isSecured) throws Exception { 
        XmlCfgMarshaller mockMarshaller = mock(XmlCfgMarshaller.class);
        when(mockMarshaller.unMarshal(Matchers.any())).thenAnswer(new Answer<XmlCfgConfig>() {

            @Override
            public XmlCfgConfig answer(InvocationOnMock invocation) throws Throwable {
                byte[] arg = (byte[]) invocation.getArguments()[0];
                Assert.assertEquals("yep", new String(arg, "UTF-8").trim());
                List<XmlCfgEntry> entries = new ArrayList<>();
                entries.add(new XmlCfgEntry("d", metaDataType, "ss"));
                return new XmlCfgConfig(entries);
            }
            
        });
        File workDir = new File("target" + File.separator + "workdir" + testName.getClass().getSimpleName() + "-" + testName.getMethodName());
        workDir.mkdirs();
        
        DefaultDLSEnabledChecker dlsEnabledCheck = new DefaultDLSEnabledChecker();
        
        Config config = mock(Config.class);
        when(config.getSearchHomeDir()).thenReturn(workDir);
        when(config.getCollectionName()).thenReturn("coll");
        
        File Xmlcfg = new File(workDir, "conf" + File.separator + "coll" + File.separator + "xml.cfg");
        Xmlcfg.getParentFile().mkdirs();
        FileUtils.writeStringToFile(Xmlcfg, "yep");
        
        dlsEnabledCheck.setXmlCfgMarshaller(mockMarshaller);
        
        Assert.assertTrue(dlsEnabledCheck.securityDefinedInXmlCfg(config) == isSecured);
    }
    
    @Test
    public void securityDefinedInXmlCfgTestNoFile() throws Exception { 
        XmlCfgMarshaller mockMarshaller = mock(XmlCfgMarshaller.class);
        when(mockMarshaller.unMarshal(Matchers.any())).thenAnswer(new Answer<XmlCfgConfig>() {

            @Override
            public XmlCfgConfig answer(InvocationOnMock invocation) throws Throwable {
                Assert.fail("Should not be called as xml.cfg does no exist.");
                return null;
            }
            
        });
        File workDir = new File("target" + File.separator + "workdir" + testName.getClass().getSimpleName() + "-" + testName.getMethodName());
        workDir.mkdirs();
        
        DefaultDLSEnabledChecker dlsEnabledCheck = new DefaultDLSEnabledChecker();
        
        Config config = mock(Config.class);
        when(config.getSearchHomeDir()).thenReturn(workDir);
        when(config.getCollectionName()).thenReturn("coll");
        
        File Xmlcfg = new File(workDir, "conf" + File.separator + "coll" + File.separator + "xml.cfg");
        Xmlcfg.getParentFile().mkdirs();
        Assert.assertFalse(Xmlcfg.exists());
        
        dlsEnabledCheck.setXmlCfgMarshaller(mockMarshaller);
        
        Assert.assertFalse(dlsEnabledCheck.securityDefinedInXmlCfg(config));
    }
    
    private DefaultDLSEnabledChecker spyDefaultDLSEnabledCheck(boolean config, boolean metamap, boolean xmlcfg) {
        DefaultDLSEnabledChecker dlsEnabledCheck = spy(new DefaultDLSEnabledChecker());
        
        doReturn(config).when(dlsEnabledCheck).securityConfiguredInConfig(Matchers.any());
        doReturn(metamap).when(dlsEnabledCheck).securityDefinedInMetaMapCfg(Matchers.any());
        doReturn(xmlcfg).when(dlsEnabledCheck).securityDefinedInXmlCfg(Matchers.any());
        
        return dlsEnabledCheck;
    }
    
}
