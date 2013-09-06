package com.funnelback.publicui.search.model.transaction.session;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Internal Primary Key class for {@link SessionResult}
 *
 * @since v12.5
 */
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SessionResultPK implements Serializable {

    private static final long serialVersionUID = -694314277777986501L;

    /** @see {@link SessionResult#userId} */
    private String userId;

    /** @see {@link SessionResult#collection} */
    private String collection;

    /** @see {@link SessionResult#indexUrl} */
    private String indexUrl;

}
