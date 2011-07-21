package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;


public abstract class RankingFeature implements Comparable<RankingFeature> {

	public RankingFeature(String name, String category, String longName) {
		this.name = name;
		this.category = category;
		if(longName != null && !"".equals(longName)) {
			this.longName = longName.trim().replaceAll(" weight$", " score");
		} else {
			this.longName = name;
		}
	}

	@Getter private final String name;	
	@Getter private final String category;
	@Getter private final Map<String,Float> scores = new HashMap<String,Float>();
	@Getter private final List<String> hintTexts = new ArrayList<String>();
	@Getter private final String longName;
	
	
	@Getter protected float win;
	protected float maxScore;
	protected float minScore = 101;

	public void rememberScore(float score,String rank) {
		// Set max and min scores for this feature in the hint object
		// Used for calculating possible wins, and features that are uninteresting
		if(maxScore < score) {
			maxScore = score;
		}
		if(minScore > score) {
			minScore = score;
		}
		scores.put(rank, score);
	}
	
	public boolean isInteresting() {
		return (scores.size()!=0); // && win > 0.0000001;
	}
	
	@Override
	public int compareTo(RankingFeature that) {
		if(this.win < that.getWin()) return 1;
		if(that.getWin() < this.win) return -1;
		return 0;
	}

	public abstract void caculateWin(float selectedScore, float weighting);
}
