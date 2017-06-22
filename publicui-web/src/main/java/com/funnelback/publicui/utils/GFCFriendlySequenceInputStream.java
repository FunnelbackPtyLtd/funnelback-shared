package com.funnelback.publicui.utils;

import java.io.SequenceInputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class GFCFriendlySequenceInputStream extends SequenceInputStream {
    
    // Pray it is not larger than 2GB
    public int size; //TODO remove.

    public GFCFriendlySequenceInputStream(List<GCFriendlyInputStream> inputData) {
        super(toEnumeration(inputData));
        size = inputData.stream().mapToInt(GCFriendlyInputStream::getSize).sum();
    }
    
    private static <E> Enumeration<E> toEnumeration(Iterable<E> iterator) {
        Iterator<E> iterable = iterator.iterator();
        return new Enumeration<E>() {

            @Override
            public boolean hasMoreElements() {
                return iterable.hasNext();
            }

            @Override
            public E nextElement() {
                return iterable.next();
            }
        };
    }
    
}
