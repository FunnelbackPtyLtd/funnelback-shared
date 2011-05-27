package com.funnelback.contentoptimiser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.Hint;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingScore;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlInfoAndScore;

@Log
@Component
public class DefaultUrlCauseFiller implements UrlCausesFiller {

	@Override
	public void consumeResultPacket(UrlComparison comparison, ResultPacket rp,HintFactory hintFactory) {
		// TODO Auto-generated method stub
		
		// Add weights, create hint objects
		for (Entry<String, Float> weightEntry :  rp.getCoolerWeights().entrySet()) {
			comparison.getWeights().put(weightEntry.getKey(), weightEntry.getValue() * 100);
			

		}

		Map<String, Hint> hintsByName = comparison.getHintsByName();
		for (Entry<String,String> nameAndType : rp.getExplainTypes().entrySet()) {
			Hint h = hintFactory.create(nameAndType.getKey(),nameAndType.getValue());
			hintsByName.put(nameAndType.getKey(),h);
			comparison.getHintsByWin().add(h);			
		}

		
		// Fill in results with re-weighted scores
		for (Result result : rp.getResults()) {
			UrlInfoAndScore info = new UrlInfoAndScore(result.getLiveUrl(),result.getTitle(),""+ result.getRank());
	
			for (Map.Entry<String,Float> feature : result.getExplain().getFeatureScores().entrySet()) {
				float percentage = feature.getValue()*rp.getCoolerWeights().get(feature.getKey())  *100;
				//causes.add(new RankingScore(feature.getKey(), percentage));
				Hint hint = hintsByName.get(feature.getKey());
				hint.rememberScore(percentage,"" +result.getRank());
			}
			comparison.getUrls().add(info);
		}
	}
	
	@Override
	public void fillHints(UrlComparison comparison) {
		// Remove features which have no variance
		List<Hint> remove = new ArrayList<Hint>();		
		for (Hint hint : comparison.getHintsByWin()) {
			if(!hint.isInteresting()) {
				log.info("Removing " + hint.getName());
				remove.add(hint);
				comparison.getHintsByName().remove(hint.getName());
			}
		}
		comparison.getHintsByWin().removeAll(remove);
	}
	
	@Override
	public void setImportantUrl(UrlComparison comparison,ResultPacket rp) {		
		Result importantResult = rp.getResults().get(0);
		// First see if we already have this URL
		for (UrlInfoAndScore url : comparison.getUrls()) {
			if(url.getUrl().equals(importantResult.getDisplayUrl())) {
				comparison.setImportantOne(url);
			}
		}
		// Otherwise we must create it ourselves
		if(comparison.getImportantOne() == null) {
			UrlInfoAndScore url = new UrlInfoAndScore(importantResult.getLiveUrl(),importantResult.getTitle(),"> 10");
			comparison.setImportantOne(url);
		}
		for (Map.Entry<String,Float> feature : importantResult.getExplain().getFeatureScores().entrySet()) {
			float percentage = feature.getValue()*rp.getCoolerWeights().get(feature.getKey())  *100;
			Hint hint = comparison.getHintsByName().get(feature.getKey());
			if(comparison.getImportantOne().getRank().equals("> 10")) {
				hint.rememberScore(percentage, "> 10");
			}
			hint.caculateWin(percentage, rp.getCoolerWeights().get(feature.getKey())*100);
		}
		
		
		Collections.sort(comparison.getHintsByWin());
		
	}


}


