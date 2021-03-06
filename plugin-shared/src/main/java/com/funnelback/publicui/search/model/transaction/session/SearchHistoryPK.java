package com.funnelback.publicui.search.model.transaction.session;

import java.io.Serializable;

import lombok.EqualsAndHashCode;

/**
 * Internal Primary Key class for {@link SearchHistory}
 *
 * @since v12.5
 */
@EqualsAndHashCode
public class SearchHistoryPK implements Serializable {

    private static final long serialVersionUID = 3645177332051698516L;

    /** @see SearchHistory#userId */
    private String userId;
    
    /** @see SearchHistory#collection */
    private String collection;

    /** @see SearchHistory#searchParamsSignature */
    private int searchParamsSignature;

}
