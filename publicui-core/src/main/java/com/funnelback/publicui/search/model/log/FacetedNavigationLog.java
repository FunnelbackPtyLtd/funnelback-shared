package com.funnelback.publicui.search.model.log;

import java.util.Date;

import lombok.Getter;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

/**
 * A faceted navigation click log
 */
public class FacetedNavigationLog extends Log {

    @Getter final private String facet;
    @Getter final private String query;
    
    /**
     * @param date Date of the event
     * @param collection Collection
     * @param profile Profile
     * @param requestId Request identifier (IP, Hash, ...)
     * @param userId User identifier (UUID)
     * @param facet Clicked facet
     * @param query Corresponding query
     */
    public FacetedNavigationLog(Date date, Collection collection, Profile profile, String requestId, String userId,
        String facet, String query) {
        super(date, collection, profile, requestId, userId);
        this.facet = facet;
        this.query = query;
    }
    
    /**
     * @return An XML representation of the log
     */
    public String toXml() {
        StringBuffer out = new StringBuffer();
        out.append("<cfac>")
            .append("<t>").append(XML_DATE_FORMAT.format(date)).append("</t>")
            .append("<coll>").append(collection.getId()).append("</coll>")
            .append("<facet>").append(facet).append("</facet>")
            .append("<prof>").append(profile.getId()).append("</prof>")
            .append("<requestip>").append(requestId).append("</requestip>")
            .append("<squery>").append(query).append("</squery>")
            .append("</cfac>");
        
        return out.toString();
        
    }


}
