package com.funnelback.contentoptimiser;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UrlInfoAndScore implements Comparable<UrlInfoAndScore>{
	@Getter private final String url;
	@Getter private final String title;
	@Getter private final int rank;
	@Getter private final List<RankingScore> causes = new ArrayList<RankingScore>();

	public int sum() {
		int sumThis = 0;
		for (RankingScore score : causes) {
			sumThis+= score.getPercentage();
		} 
		return sumThis;
	}
	
	@Override
	public int compareTo(UrlInfoAndScore that) {
		return that.sum() - this.sum();
	}

	
}
