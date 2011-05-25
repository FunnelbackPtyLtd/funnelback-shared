package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Hint implements Comparable<Hint> {
//	@Getter private final String html;
//	@Getter private final String link;
//	@Getter private final String linkText;
	
	@Getter private final String name;
	@Getter protected float win;

	protected float maxScore;
	protected float minScore = 101;
	protected int count = 0;

	public void rememberScore(float score) {
		// Set max and min scores for this feature in the hint object
		// Used for calculating possible wins, and features that are uninteresting
		if(maxScore < score) {
			maxScore = score;
		}
		if(minScore > score) {
			minScore = score;
		}
		count++;
	}
	
	public boolean isInteresting() {
		return (count != 0 && win > 0.0000001);
	}
	
	@Override
	public int compareTo(Hint that) {
		if(this.win < that.getWin()) return 1;
		if(that.getWin() < this.win) return -1;
		return 0;
	}

	public abstract void caculateWin(float selectedScore, float weighting);
}
