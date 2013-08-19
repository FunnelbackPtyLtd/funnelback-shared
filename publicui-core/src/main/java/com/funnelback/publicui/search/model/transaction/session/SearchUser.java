package com.funnelback.publicui.search.model.transaction.session;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

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
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SearchUser implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /** Unique identifier of the user */
    @Id
    @Getter @Setter private String id;
    
    /**
     * <p>E-mail address of the user.</p>
     * 
     * <p>Can be null if it's not known.</p>
     */
    @Getter @Setter private String email;
    
    /**
     * Date this user was created
     */
    @Getter @Setter private Date createdDate;

    /**
     * Creates a new user
     * @param id ID to assign to the user
     */
    public SearchUser(String id) {
        this.id = id;
    }
    
}
