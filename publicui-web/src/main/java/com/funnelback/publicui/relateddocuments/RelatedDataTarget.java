package com.funnelback.publicui.relateddocuments;

import com.funnelback.publicui.search.model.padre.Result;

import lombok.Data;

/**
 * A very small wrapper for the target of related data (specifically, the result
 * it should go into, and the key name it should have).
 */
@Data
public class RelatedDataTarget {
    private final Result result;
    private final String relationTargetKey;
}
