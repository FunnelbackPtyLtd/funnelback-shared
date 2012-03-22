package com.funnelback.contentoptimiser;

import lombok.extern.log4j.Log4j;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeature;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeatureMaxOther;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeatureMaxPossible;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeatureMaxPossibleMultiWordOnly;

@Log4j
@Component
public class DefaultRankingFeatureFactory implements RankingFeatureFactory {

	@Override
	public RankingFeature create(String name, String type,String category,ResultPacket rp) {
		if("max_other".equals(type)) {
			return new RankingFeatureMaxOther(name,category,rp);
		} else if("max_possible".equals(type)) {
			return new RankingFeatureMaxPossible(name,category,rp);
		} else if("max_possible_multiword_only".equals(type)) {
			return new RankingFeatureMaxPossibleMultiWordOnly(name,category,rp);
		} else if(RankingFeature.CONSAT.equals(type)) {
			return new RankingFeatureConstraintsSatisfied(name,category,rp);
		}
		log.warn("Unknown feature type in content optimiser : '" + type + "'. Returning max_other");
		return new RankingFeatureMaxOther(name,category,rp);
	}

}
