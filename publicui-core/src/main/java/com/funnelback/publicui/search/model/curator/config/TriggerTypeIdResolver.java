package com.funnelback.publicui.search.model.curator.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;

/**
 * Jackson TypeIdResolver (works out which class to create for different JSON
 * file IDs) which understands curator triggers.
 * 
 * We use this to make the JSON config file look a bit nicer (remove common
 * suffixes) and to load different classes depending on the context.
 */
public class TriggerTypeIdResolver implements TypeIdResolver {

    /** Triggers in pubilcui-core - In some cases these cannot be run because they lack publicui-web dependencies */
    private static final String TRIGGER_PACKAGE = "com.funnelback.publicui.search.model.curator.trigger";

    /** Triggers in pubilcui-web - To support triggers which require pulicui-web dependencies these are loaded in preference. */
    private static final String TRIGGER_IMPLEMENTATION_PACKAGE = "com.funnelback.publicui.curator.trigger";

    /** Standard suffix (stripped in config files) for the Trigger class names */
    private static final String CLASS_SUFFIX = "Trigger";
    
    /** A Jackson object we need to store from the init method to use later in typeFromId) - Not really sure what it's for. */
    private JavaType baseType;
    
    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
    }

    /** Tell Jackson what 'ID' to put in the file for a given object */
    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    /** Tell Jackson what 'ID' to put in the file for a given object and it's type */
    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        String name = suggestedType.getName();
        if ( name.startsWith(TRIGGER_PACKAGE) && name.endsWith(CLASS_SUFFIX) ) {
            String id = name.substring(TRIGGER_PACKAGE.length() + 1, name.length() - CLASS_SUFFIX.length());
            return id;
        }
        if ( name.startsWith(TRIGGER_IMPLEMENTATION_PACKAGE) && name.endsWith(CLASS_SUFFIX) ) {
            String id = name.substring(TRIGGER_IMPLEMENTATION_PACKAGE.length() + 1, name.length() - CLASS_SUFFIX.length());
            return id;
        }
        throw new IllegalStateException("class " + suggestedType + " is not in the packages (" + TRIGGER_PACKAGE + "/"
            + TRIGGER_IMPLEMENTATION_PACKAGE + ") or does not end with " + CLASS_SUFFIX);
    }

    /** Tell Jackson what class to instantiate for a given ID form a JSON file */
    @Override
    public JavaType typeFromId(String id) {
        Class<?> clazz;
        String implementationClazzName = TRIGGER_IMPLEMENTATION_PACKAGE + "." + id + CLASS_SUFFIX;
        String clazzName = TRIGGER_PACKAGE + "." + id + CLASS_SUFFIX;
        try {
            // Try to load the implementation class (from publicui-web)
            clazz = ClassUtil.findClass(implementationClazzName);
        } catch (ClassNotFoundException eIgnored) {
            try {
                // Fall back to the data storage only class (from publicui-core)
                clazz = ClassUtil.findClass(clazzName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("cannot find class '" + clazzName + "'");
            }
        }
        return TypeFactory.defaultInstance().constructSpecializedType(baseType, clazz);
    }

    @Override
    public Id getMechanism() {
        return Id.CUSTOM;
    }

    @Override
    public String idFromBaseType() {
        throw new UnsupportedOperationException("Missing trigger type information - Can not construct");
    }
}
