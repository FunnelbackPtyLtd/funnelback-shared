package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class Hint implements Comparable<Hint> {
//	@Getter private final String html;
//	@Getter private final String link;
//	@Getter private final String linkText;
	
	
	@Getter private final String name;
	@Getter @Setter private float maxScore;
	@Getter @Setter private float minScore = 101;
	@Getter private float win;
	
	
	@Override
	public int compareTo(Hint that) {
		if(this.win < that.getWin()) return 1;
		if(that.getWin() < this.win) return -1;
		return 0;
	}

	public void setWin(float percentage, float weighting) {
		win = maxScore - (percentage);
//		win = (weighting - percentage);
	}
}
