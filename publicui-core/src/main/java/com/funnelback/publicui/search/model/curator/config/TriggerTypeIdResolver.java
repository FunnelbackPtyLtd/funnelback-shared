package com.funnelback.publicui.search.model.curator.config;

import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

public class TriggerTypeIdResolver implements TypeIdResolver {

    private static final String TRIGGER_PACKAGE = "com.funnelback.publicui.search.model.curator.trigger";
    private static final String CLASS_SUFFIX = "Trigger";
    
    private JavaType baseType;
    
    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
    }

    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        String name = suggestedType.getName();
        if ( name.startsWith(TRIGGER_PACKAGE) && name.endsWith(CLASS_SUFFIX) ) {
            String id = name.substring(TRIGGER_PACKAGE.length() + 1, name.length() - CLASS_SUFFIX.length());
            return id;
        }
        throw new IllegalStateException("class " + suggestedType + " is not in the package " + TRIGGER_PACKAGE + " or does not end with " + CLASS_SUFFIX);
    }

    @Override
    public JavaType typeFromId(String id) {
        Class<?> clazz;
        String clazzName = TRIGGER_PACKAGE + "." + id + CLASS_SUFFIX;
        try {
            clazz = ClassUtil.findClass(clazzName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("cannot find class '" + clazzName + "'");
        }
        return TypeFactory.defaultInstance().constructSpecializedType(baseType, clazz);
    }

    @Override
    public Id getMechanism() {
        return Id.CUSTOM;
    }
}
