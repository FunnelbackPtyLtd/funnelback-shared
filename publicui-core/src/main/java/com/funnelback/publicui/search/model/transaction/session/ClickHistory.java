package com.funnelback.publicui.search.model.transaction.session;

import java.util.Date;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * A single entry in the user's click history
 * 
 * @since 12.5
 */
@Entity
public class ClickHistory extends SessionResult {

    /** Date when the click was performed */
    @Getter @Setter
    private Date clickDate;

}
