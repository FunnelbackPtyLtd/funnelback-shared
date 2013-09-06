package com.funnelback.publicui.recommender.tuple;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * A PreferenceTuple records information on a preference in the recommendation system.
 * @author fcrimmins@funnelback.com
 */
@Data
public class PreferenceTuple implements Serializable {
	/**
	 * A unique identifier for a user e.g. joe@example.com, 145.89.78.56-26483269
	 */
    private String userID = "";
    
    /**
     * A unique identifier for an item that this user has expressed a positive preference for
     * e.g. a URL, a musician etc.
     */
    private String itemID = "";
    
    /**
     * The hostname from which the preference was expressed. May not be available.
     */
    private String host = "";
    
    /**
     * The query which resulted in this preference. May not be applicable in some applications.
     */
    private String query = "";
    
    /**
     * The timestamp at which the preference occurred.
     */
    private Date date = null;

    public PreferenceTuple(String userID, String itemID, Date date, String host, String query) {
        this.userID = userID;
        this.itemID = itemID;
        this.date = date;
        this.host = host;
        this.query = query;
    }
}