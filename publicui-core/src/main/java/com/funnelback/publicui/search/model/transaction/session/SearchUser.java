package com.funnelback.publicui.search.model.transaction.session;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * User performing the search.
 * 
 * @since v12.4
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SearchUser implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /** Unique identifier of the user */
    @Getter @Setter private String id;
    
    /**
     * <p>E-mail adress of the user.</p>
     * 
     * <p>Can be null if it's not known.</p>
     */
    @Getter @Setter private String email;

    public SearchUser(String id) {
        this.id = id;
    }
    
}
