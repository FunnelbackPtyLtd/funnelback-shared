package com.funnelback.publicui.search.model.log;

import java.util.Date;
import java.util.List;

import lombok.Getter;

import org.apache.commons.lang.time.FastDateFormat;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

/**
 * Contextual navigation log
 */
public class ContextualNavigationLog extends Log {

    /** Date format used in the logs */
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyyMMdd HH:mm:ss");
    
    @Getter final private String cluster;
    @Getter final private List<String> previousClusters;
    
    /**
     * @param date Date of the contextual navigation query
     * @param collection Collection
     * @param profile Profile
     * @param requestId Request identifier (IP, hash, '-')
     * @param cluster Clicked cluster
     * @param previousClusters Previously clicked cluster (breadcrumb)
     */
    public ContextualNavigationLog(Date date, Collection collection, Profile profile, String requestId,
            String cluster, List<String> previousClusters) {
        super(date, collection, profile, requestId);
        this.cluster = cluster;
        this.previousClusters = previousClusters;
    }
    
    /**
     * @return XML representation suitable for writing to log file
     */
    public String toXml() {
        StringBuffer out = new StringBuffer();
        out.append("<cflus>")
            .append("<t>").append(DATE_FORMAT.format(date)).append("</t>");
        
        for (int i=0; i<previousClusters.size(); i++) {
            out.append("<cluster").append(i).append(">");
            out.append(previousClusters.get(i));
            out.append("</cluster").append(i).append(">");
        }
        
        out.append("<coll>").append(collection.getId()).append("</coll>")
            .append("<fluster>").append(cluster).append("</fluster>")
            .append("<prof>").append(profile.getId()).append("</prof>")
            .append("<requestip>").append(requestId).append("</requestip>")
            .append("</cflus>");
        
        return out.toString();
        
    }
    
}
