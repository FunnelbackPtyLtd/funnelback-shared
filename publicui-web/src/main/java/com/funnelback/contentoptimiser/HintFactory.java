package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.Hint;

public interface HintFactory {

	Hint create(String name, String type,String category, ResultPacket rp);

}
