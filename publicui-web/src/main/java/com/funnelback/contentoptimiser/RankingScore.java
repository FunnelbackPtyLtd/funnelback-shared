package com.funnelback.contentoptimiser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RankingScore {
	@Getter private final String name;	
	@Getter private final int percentage;
}
