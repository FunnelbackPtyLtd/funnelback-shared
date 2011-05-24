package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RankingScore {
	@Getter private final String name;	
	@Getter private final float percentage;
}
