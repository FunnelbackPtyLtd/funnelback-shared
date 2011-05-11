package com.funnelback.contentoptimiser;

import lombok.Getter;


public class RankingScore {
	@Getter
	private int percentage = 30;
	@Getter
	private String name  = "Anchors";
	
	
	public RankingScore(String name) {
		this.name = name;
		percentage = (int)(Math.random() * 40);
	}
	
}
