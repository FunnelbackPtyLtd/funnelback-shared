package com.funnelback.plugin.index.consumers;

import com.funnelback.plugin.index.model.querycompletion.AutoCompletionEntry;

import java.util.Set;

public interface AutoCompletionConsumer {

    /**
     * A plugin may call this to supply autocompletion entries and the profiles for which they
     * should be applied.
     * 
     * @param autoCompletionEntry The autocompletion entry to be applied.
     * @param profiles The profiles to which the entry should be applied.
     * @throws IllegalArgumentException when one or more of the arguments is not valid.
     */
    void applyAutoCompletionEntryToProfiles(AutoCompletionEntry autoCompletionEntry, Set<String> profiles)
        throws IllegalArgumentException;
}
