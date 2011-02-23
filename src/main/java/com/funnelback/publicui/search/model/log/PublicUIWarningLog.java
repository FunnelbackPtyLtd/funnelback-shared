package com.funnelback.publicui.search.model.log;

import java.util.Date;

import lombok.Getter;

import org.apache.commons.lang.time.FastDateFormat;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

public class PublicUIWarningLog extends Log {

	public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyyMMdd HH:mm:ss");
	
	@Getter private final String message;
	
	public PublicUIWarningLog(Date date, Collection collection, Profile profile, String userId,
			String message) {
		super(date, collection, profile, userId);
		this.message = message;
	}
	
	@Override
	public String toString() {
		return DATE_FORMAT.format(date) + " " + collection.getId() + " - " + message;
	}

}
