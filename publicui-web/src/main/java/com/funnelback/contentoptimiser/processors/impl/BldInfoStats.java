package com.funnelback.contentoptimiser.processors.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BldInfoStats {
	public BldInfoStats() {
		totalDocuments = -1;
		avgWords = -1;
	}
	@Getter private final long totalDocuments;
	@Getter private final int avgWords;
}
