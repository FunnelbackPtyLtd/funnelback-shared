package com.funnelback.contentoptimiser;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class UrlComparison {
	
	// todo: remove setter later - it's only here for faking up some data
	@Getter @Setter
	List<UrlInfoAndScore> urls = new ArrayList<UrlInfoAndScore>();
	
	@Getter @Setter
	UrlInfoAndScore importantOne;
	
	@Getter
	public List<Hint> hints = new ArrayList<Hint>();

}
