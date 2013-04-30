package com.funnelback.publicui.search.model.transaction.session;

import java.util.Date;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * A result in a results cart
 * 
 * @since 12.5
 */
@Entity
public class CartResult extends SessionResult {

    /** Date when the result was added to the cart */
    @Getter @Setter
    private Date addedDate;
    
}
