package com.funnelback.publicui.recommender.tuple;

import lombok.Data;

/**
 * An ItemTuple records information on an item in the recommendation system.
 * @author fcrimmins@funnelback.com
 */
@Data
public class ItemTuple {
	/**
	 * The ID of the item e.g. URL address, musician name etc.
	 */
    private String itemID = "";
    
    /**
     * The score assigned to this item, which can be used to rank it in a list of recommendations.
     */
    private float score = 0;

    public ItemTuple(String item, float score) {
        this.itemID = item;
        this.score = score;
    }

    public String toString() {
        return new String("Item ID: " + itemID + " Score: " + score);
    }
}