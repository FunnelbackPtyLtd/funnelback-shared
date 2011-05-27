package com.funnelback.contentoptimiser;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.transaction.contentoptimiser.Hint;

@Log
@Component
public class DefaultHintFactory implements HintFactory {

	@Override
	public Hint create(String name, String type) {
		if("max_other".equals(type)) {
			return new HintMaxOther(name);
		} else if("max_possible".equals(type)) {
			return new HintMaxPossible(name);
		}
		
		log.warn("Unknown feature type in content optimiser : '" + type + "'. Returning max_other");
		return new HintMaxOther(name);
	}

}
