package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.transaction.contentoptimiser.Hint;


public class HintMaxOther extends Hint {

	public HintMaxOther(String name) {
		super(name);
	}

	@Override
	public void caculateWin(float selectedScore, float weighting) {
		win = maxScore - selectedScore;
	}

}
