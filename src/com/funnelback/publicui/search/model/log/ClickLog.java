package com.funnelback.publicui.search.model.log;

import java.net.URI;
import java.net.URL;
import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import com.funnelback.publicui.search.model.collection.Collection;

@ToString
@RequiredArgsConstructor
public class ClickLog {
	
	public static enum Type {
		FP, CLICK;
	}

	@Getter final private Collection collection;
	@Getter final private String profile;
	@Getter final private Date date;
	
	/**
	 * User identifier, could be an IP, a hash, or null
	 */
	@Getter final private String userId;
	
	@Getter final private URL referer;
	@Getter final private int rank;
	@Getter final private URI target;
	@Getter final private Type type;
	
}
