package com.funnelback.plugin.index.model.querycompletion;

import java.util.List;
import java.util.function.Supplier;

import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class QueryCompletionCSV {
    
    /**
     * All profiles for which the auto completion CSV applies to.
     */
    private List<String> profiles;
    
    private Supplier<InputStream> queryCompletionCSV;
}
