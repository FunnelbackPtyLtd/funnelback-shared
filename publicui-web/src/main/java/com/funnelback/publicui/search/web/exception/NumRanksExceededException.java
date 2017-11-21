package com.funnelback.publicui.search.web.exception;

import com.funnelback.common.padre.NumRanks;
import com.funnelback.publicui.i18n.I18n;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The exception thrown when num_ranks has been exceeded.
 *
 */
@AllArgsConstructor
public class NumRanksExceededException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter private final NumRanks numRanksLimit; // string because it might be a gigantic number
    
    @Getter private final I18n i18n;
    
    @Override
    public String getMessage() {
        return i18n.tr("parameter.search.num_ranks.exceeded", numRanksLimit);
    }
    
}
