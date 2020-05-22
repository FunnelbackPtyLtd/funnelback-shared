package com.funnelback.plugin.index.model.querycompletion;

import java.util.Set;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AutoCompletionCSV {
    
    /**
     * The profiles for which the auto completion CSV applies to.
     */
    private Set<String> profiles;
    
    private Supplier<AutoCompletionEntry> queryCompletionEntrySupplier;
}
