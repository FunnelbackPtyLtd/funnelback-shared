package com.funnelback.contentoptimiser.processors.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BldInfoStats {

	@Getter private final long totalDocuments;
	@Getter private final int avgWords;
}
