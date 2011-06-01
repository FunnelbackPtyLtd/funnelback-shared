package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.transaction.contentoptimiser.Hint;

public class HintMaxPossible extends Hint {

	public HintMaxPossible(String name,String category) {
		super(name,category);
	}

	@Override
	public void caculateWin(float selectedScore, float weighting) {
		win = weighting - selectedScore;
	}

}
