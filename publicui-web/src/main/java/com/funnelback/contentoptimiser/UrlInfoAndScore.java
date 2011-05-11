package com.funnelback.contentoptimiser;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class UrlInfoAndScore implements Comparable<UrlInfoAndScore>{
	@Getter @Setter
	String url;
	@Getter @Setter
	String title;
	@Getter @Setter
	int rank;
	
	@Getter
	List<RankingScore> causes = new ArrayList<RankingScore>();

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
