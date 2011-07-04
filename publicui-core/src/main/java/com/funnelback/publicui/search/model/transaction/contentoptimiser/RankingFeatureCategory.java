package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RankingFeatureCategory implements Comparable<RankingFeatureCategory> {
	@Getter private final String name;
	@Getter private final List<RankingFeature> hints = new ArrayList<RankingFeature>();
	
	public float getWin() {
		float total = 0;
		for (RankingFeature hint : hints) {
			total += hint.getWin();
		}
		return total;
	}
	
	@Override
	public int compareTo(RankingFeatureCategory that) {
		if(this.getWin() < that.getWin()) return 1;
		if(that.getWin() < this.getWin()) return -1;
		return 0;
	} 
}
