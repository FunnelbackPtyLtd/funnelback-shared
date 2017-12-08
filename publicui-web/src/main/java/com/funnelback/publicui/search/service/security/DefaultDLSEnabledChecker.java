package com.funnelback.publicui.search.service.security;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.metadata.MetadataClassMappings;
import com.funnelback.common.config.metadata.marshaller.MetadataMappingCollectionReader;
import com.funnelback.common.config.metamapcfg.MetaDataType;
import com.funnelback.common.config.metamapcfg.MetaMapCfgEntry;
import com.funnelback.common.config.metamapcfg.MetaMapCfgMarshaller;
import com.funnelback.common.config.xmlcfg.XmlCfgMarshaller;
import com.funnelback.common.config.xmlcfg.XmlCfgMetadataMapping;
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
            || securityDefinedInXmlCfg(config)
            || securityDefinedInMetadataMapping(config);
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
     * @return
     */
    boolean securityDefinedInMetadataMapping(Config config) {
        return new MetadataMappingCollectionReader().readMappingsForCollection(config.getSearchHomeDir(), config.getCollectionId())
            .getClassMappings()
            .stream()
            .map(MetadataClassMappings::getType)
            .anyMatch(MetaDataType.SECURITY::equals);
    }
    
    /**
     * 
     * @param config
     * @return true if a security meta data type is configured in metamap.cfg
     */
    boolean securityDefinedInMetaMapCfg(Config config) {
        File metaMapCfg = new File(Files.configDir(config.getSearchHomeDir(), config.getCollectionId()), Files.META_MAP_FILENAME);
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
     * This is overprotective and still checks this file even if the newer metadata mappig file exists. 
     * 
     * @param config
     * @return true if a security meta data type is configured in xml.cfg
     */
    boolean securityDefinedInXmlCfg(Config config) {
        File xmlCfg = new File(Files.configDir(config.getSearchHomeDir(), config.getCollectionId()), Files.XML_META_MAP_CONFIG_FILENAME);
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
