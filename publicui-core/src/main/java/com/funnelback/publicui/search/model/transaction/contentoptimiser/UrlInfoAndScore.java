package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UrlInfoAndScore {
	@Getter private final String url;
	@Getter private final String title;
	@Getter private final String rank;
	
}
