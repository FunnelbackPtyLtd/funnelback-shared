package com.funnelback.publicui.search.model.log;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.time.FastDateFormat;

import lombok.Getter;
import lombok.ToString;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

@ToString
public class InteractionLog extends Log {

	public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("EEE MMM d HH:mm:ss yyyy");
	
	@Getter private final String logType;
	@Getter private final URL referer;
	@Getter private final Map<String,String[]> parameters;
	@Getter private final String requestIp;

	public InteractionLog(Date date, Collection collection, Profile profile,
			String userId, String logType, String requestIp, URL referer, Map<String, String[]> parameters) {
		super(date, collection, profile, userId);
		this.logType = logType;
		this.referer = referer;
		this.requestIp = requestIp;
		this.parameters = Collections.unmodifiableMap(parameters);
	}

}
