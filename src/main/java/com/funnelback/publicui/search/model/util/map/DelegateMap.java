package com.funnelback.publicui.search.model.util.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class DelegateMap<K,V> implements Map<K, V> {

    @Getter private final Map<K, V> underlyingMap;

    public int hashCode() {
        return underlyingMap.hashCode();
    }

    public boolean equals(Object obj) {
        return underlyingMap.equals(obj);
    }

    public int size() {
        return underlyingMap.size();
    }

    public boolean isEmpty() {
        return underlyingMap.isEmpty();
    }

    public boolean containsKey(Object key) {
        return underlyingMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return underlyingMap.containsValue(value);
    }

    public V get(Object key) {
        return underlyingMap.get(key);
    }

    public String toString() {
        return underlyingMap.toString();
    }

    public V put(K key, V value) {
        return underlyingMap.put(key, value);
    }

    public V remove(Object key) {
        return underlyingMap.remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        underlyingMap.putAll(m);
    }

    public void clear() {
        underlyingMap.clear();
    }

    public Set<K> keySet() {
        return underlyingMap.keySet();
    }

    public Collection<V> values() {
        return underlyingMap.values();
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return underlyingMap.entrySet();
    }
}
