package com.funnelback.publicui.search.model.curator.config;

import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

public class ActionTypeIdResolver implements TypeIdResolver {

    private static final String ACTION_PACKAGE = "com.funnelback.publicui.curator.action";
    
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
        if ( name.startsWith(ACTION_PACKAGE) ) {
            return name.substring(ACTION_PACKAGE.length() + 1);
        }
        throw new IllegalStateException("class " + suggestedType + " is not in the package " + ACTION_PACKAGE);
    }

    @Override
    public JavaType typeFromId(String id) {
        Class<?> clazz;
        String clazzName = ACTION_PACKAGE + "." + id;
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
