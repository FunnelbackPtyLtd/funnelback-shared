package com.funnelback.publicui.search.model.log;

import java.net.URI;
import java.net.URL;
import java.util.Date;

import lombok.Getter;
import lombok.ToString;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

@ToString
public class ClickLog extends Log {
	
	public static enum Type {
		FP, CLICK;
	}
		
	@Getter final private URL referer;
	@Getter final private int rank;
	@Getter final private URI target;
	@Getter final private Type type;
	
	public ClickLog(Date date, Collection collection, Profile profile, String userId,
			URL referer, int rank, URI target, Type type) {
		super(date, collection, profile, userId);
		this.referer = referer;
		this.rank = rank;
		this.target = target;
		this.type = type;
	}	
	
}
