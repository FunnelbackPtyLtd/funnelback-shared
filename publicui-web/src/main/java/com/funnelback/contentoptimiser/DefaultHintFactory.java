package com.funnelback.contentoptimiser;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.Hint;

@Log
@Component
public class DefaultHintFactory implements HintFactory {

	@Override
	public Hint create(String name, String type,String category,ResultPacket rp) {
		if("max_other".equals(type)) {
			return new HintMaxOther(name,category);
		} else if("max_possible".equals(type)) {
			return new HintMaxPossible(name,category);
		} else if("max_possible_multiword_only".equals(type)) {
			return new HintMaxPossibleMultiWordOnly(name,category,rp);
		}
		
		log.warn("Unknown feature type in content optimiser : '" + type + "'. Returning max_other");
		return new HintMaxOther(name,category);
	}

}
