package com.funnelback.contentoptimiser;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.transaction.contentoptimiser.Hint;

@Component
public class DefaultHintFactory implements HintFactory {

	@Override
	public Hint create(String name, String type) {
		if("max_other".equals(type)) {
			return new HintMaxOther(name);
		} else if("max_possible".equals(type)) {
			return new HintMaxPossible(name);
		}

		return null;
	}

}
