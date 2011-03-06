package com.funnelback.publicui.search.model.log;

import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

@RequiredArgsConstructor
public abstract class Log {

	@Getter final protected Date date;
	
	@Getter final protected Collection collection;
	@Getter final protected Profile profile;
	
	/**
	 * User identifier, could be an IP, a hash, or null
	 */
	@Getter final protected String userId;
		
}
