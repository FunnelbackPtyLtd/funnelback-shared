package com.funnelback.publicui.search.web.setup;

import org.springframework.beans.factory.FactoryBean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

/**
 * This factory bean configures our object-mapper as intended for
 * Funnelback.
 * 
 * We can't do this in publicui-servlet.xml because the methods
 * on ObjectMapper are not the normal getters/setters spring
 * knows how to handle.
 */
public class ObjectMapperFactoryBean implements FactoryBean<ObjectMapper>{

    public Class<?> getObjectType(){
        return ObjectMapper.class;
    }

    public boolean isSingleton(){
       return false;
    }

    private JsonInclude.Include jsonInclusion;
    
    public void setJsonInclusion(JsonInclude.Include jsonInclusion) {
        this.jsonInclusion = jsonInclusion;
    }
    
    public ObjectMapper getObject(){
        ObjectMapper result = new ObjectMapper();
        result.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        result.setSerializationInclusion(jsonInclusion);
        result.registerModule(new Jdk8Module());
        result.registerModule(new GuavaModule());
        return result;
    }
 }
