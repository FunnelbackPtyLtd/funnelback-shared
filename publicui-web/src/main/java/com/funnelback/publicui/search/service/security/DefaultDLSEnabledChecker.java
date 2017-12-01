package com.funnelback.publicui.search.service.security;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.metamapcfg.MetaDataType;
import com.funnelback.common.config.metamapcfg.MetaMapCfgEntry;
import com.funnelback.common.config.metamapcfg.MetaMapCfgMarshaller;
import com.funnelback.common.config.xmlcfg.XmlCfgMetadataMapping;
import com.funnelback.common.config.xmlcfg.XmlCfgMarshaller;
import com.funnelback.publicui.search.model.collection.Collection;

import lombok.NoArgsConstructor;


@Component
@NoArgsConstructor
public class DefaultDLSEnabledChecker implements DLSEnabledChecker {

    
    private MetaMapCfgMarshaller metaMapCfgMarshaller = new MetaMapCfgMarshaller();
    private XmlCfgMarshaller xmlCfgMarshaller = new XmlCfgMarshaller();
    
    
    @Override
    public boolean isDLSEnabled(Collection collection) throws IllegalArgumentException {
        Config config = collection.getConfiguration();
        
        return securityConfiguredInConfig(config)
            || securityDefinedInMetaMapCfg(config)
            || securityDefinedInXmlCfg(config);
    }
    
    boolean securityConfiguredInConfig(Config config ) {
        
        //Typically a thing is defined which gets the keys from the user, thus giving away we are in DLS
        if(!"".equals(config.value(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, ""))) {
            return true;
        }
        
        if(!"".equals(config.value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, ""))) {
            if(!Config.isFalse(config.value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, ""))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 
     * @param config
     * @return true if a security meta data type is configured in metamap.cfg
     */
    boolean securityDefinedInMetaMapCfg(Config config) {
        File metaMapCfg = new File(config.getSearchHomeDir(), 
            DefaultValues.FOLDER_CONF 
            + File.separator + config.getCollectionName() 
            + File.separator + Files.META_MAP_FILENAME);
        if(metaMapCfg.exists()) {
            try {
                byte[] b = FileUtils.readFileToByteArray(metaMapCfg);
                for(MetaMapCfgEntry entry : metaMapCfgMarshaller.unMarshal(b)) {
                    if(MetaDataType.SECURITY.equals(entry.getType())) {
                        return true;
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }
    
    /**
     * 
     * @param config
     * @return true if a security meta data type is configured in metamap.cfg
     */
    boolean securityDefinedInXmlCfg(Config config) {
        File xmlCfg = new File(config.getSearchHomeDir(), 
            DefaultValues.FOLDER_CONF 
            + File.separator + config.getCollectionName() 
            + File.separator + Files.XML_META_MAP_CONFIG_FILENAME);
        if(xmlCfg.exists()) {
            try {
                byte[] b = FileUtils.readFileToByteArray(xmlCfg);
                for(XmlCfgMetadataMapping entry : xmlCfgMarshaller.unMarshal(b).getMappedMetadata()) {
                    if(MetaDataType.SECURITY.equals(entry.getType())) {
                        return true;
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

    void setMetaMapCfgMarshaller(MetaMapCfgMarshaller metaMapCfgMarshaller) {
        this.metaMapCfgMarshaller = metaMapCfgMarshaller;
    }

    void setXmlCfgMarshaller(XmlCfgMarshaller xmlCfgMarshaller) {
        this.xmlCfgMarshaller = xmlCfgMarshaller;
    }

}
