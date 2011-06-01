package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.Hint;

public class HintMaxPossibleMultiWordOnly extends Hint {

	final boolean maybeMultiword;
	
	public HintMaxPossibleMultiWordOnly(String name,String category,ResultPacket rp) {
		super(name,category);
		// IF there are no spaces in the query, it will be a single word query
		maybeMultiword = rp.getQueryCleaned().indexOf(' ') != -1;
	}

	@Override
	public void caculateWin(float selectedScore, float weighting) {
		if(maybeMultiword) {
			win = weighting - selectedScore;
		} else {
			win = 0;
		}
	}


}
