package com.funnelback.publicui.search.model.util.map;

import java.util.Map;
import java.util.Optional;

import com.funnelback.publicui.xml.AutoConvertingMapXStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import lombok.extern.log4j.Log4j2;

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
    
    public interface Converter<T> {
        public Optional<T> convert(Object o);
        
        public Class<T> getKeyType(); 
    }


}
