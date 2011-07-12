package com.funnelback.contentoptimiser.processors.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MetaInfo {

		@Getter private final String shortTitle;
		@Getter private final String longTitle;
		@Getter private final String improvementSuggestion;
		@Getter private final Integer threshold;
}
