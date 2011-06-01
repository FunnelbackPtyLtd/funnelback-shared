package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HintCollection implements Comparable<HintCollection> {
	@Getter private final String name;
	@Getter private final List<Hint> hints = new ArrayList<Hint>();
	
	public float getWin() {
		float total = 0;
		for (Hint hint : hints) {
			total += hint.getWin();
		}
		return total;
	}
	
	@Override
	public int compareTo(HintCollection that) {
		if(this.getWin() < that.getWin()) return 1;
		if(that.getWin() < this.getWin()) return -1;
		return 0;
	} 
}
