package com.funnelback.publicui.search.model.log;

import java.net.URI;
import java.net.URL;
import java.util.Date;

import lombok.Getter;
import lombok.ToString;

import org.apache.commons.lang.time.FastDateFormat;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

@ToString
public class ClickLog extends Log {
    
    public static enum Type {
        FP, CLICK;
    }
        
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("EEE MMM d HH:mm:ss yyyy");
    
    @Getter final private URL referer;
    @Getter final private int rank;
    @Getter final private URI target;
    @Getter final private Type type;
    @Getter final private String requestIp;
    
    public ClickLog(Date date, Collection collection, Profile profile, String userId,
            URL referer, int rank, URI target, Type type,String requestIp) {
        super(date, collection, profile, userId);
        this.referer = referer;
        this.rank = rank;
        this.target = target;
        this.type = type;
        this.requestIp = requestIp;
    }
}
