package com.funnelback.publicui.search.model.util.map;

import java.util.Map;
import java.util.Optional;

import com.funnelback.publicui.xml.AutoConvertingMapXStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import lombok.extern.log4j.Log4j2;

/**
 * A map that can auto convert the key from one type to the expected type.
 * 
 * For example if you have a map that is Map<String, String> but people are using Integer
 * keys you can use this to convert the Integer key to a String key.
 *
 * @param <K>
 * @param <V>
 */
@Log4j2
@XStreamConverter(AutoConvertingMapXStreamConverter.class)
public class AutoConvertingMap<K, V> extends DelegateMap<K, V>{

    private final Converter<K> keyConverter;
    private final String note;
    
    public AutoConvertingMap(Converter<K> keyConverter, String note, Map<K,V> underlyingMap) {
        super(underlyingMap);
        this.keyConverter = keyConverter;
        this.note = note;
    }
    
    private K convertKey(K key) {
        Optional<K> convertedKey = this.keyConverter.convert(key);
        if(convertedKey.isPresent()) {
            String msg = "map expects type: " + this.keyConverter.getKeyType().getCanonicalName() + " yet it was a " + 
                key.getClass().getCanonicalName() + ". This behaviour may not be supported in future versions. "
                    + "Note: " + this.note;
            
            log.warn(msg + ". Increase logging to get a stack trace to find where this is happening.");
            
            if(log.isDebugEnabled()) {
                log.debug(msg, new RuntimeException());
            }
            return convertedKey.get();
        }
        return key;
    }
    
    @Override
    public V put(K key, V value) {
        return super.put(convertKey(key), value);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        return super.get(convertKey((K) key));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(convertKey((K) key));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        return super.remove(convertKey((K) key));
    }
    
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }
    
    /**
     * Converter to convert from some other type to the expected type. 
     * 
     *
     * @param <T>
     */
    public interface Converter<T> {
        
        /**
         * If the object is a type we support but not the expected
         * type this should take care of converting that.
         * 
         * e.g. if we want to work with Integer on our map but our map
         * expects Strings then if the type is a Integer then we would return
         * a String
         * @param o 
         * @return optional empty if it is a type we don't expect to convert,
         * otherwise if we convert the type then an optional holding its new
         * value, when a non empty optional is returned a warning is logged.
         */
        public Optional<T> convert(Object o);
        
        public Class<T> getKeyType(); 
    }


}
