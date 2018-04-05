package com.funnelback.publicui.relateddocuments;

import lombok.Data;

/**
 * A very small wrapper for the source of related data, and the key under which
 * any related documents found here will be stored.
 */
@Data
public class RelationToExpand {
    private final RelationSource relationSource;
    private final String relationTargetKey;
}