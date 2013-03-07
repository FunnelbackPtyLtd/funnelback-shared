package com.funnelback.contentoptimiser;

import java.util.Map;

public interface ConfigReader<T> {

    Map<String, ? extends T> read(String file);
    
}
