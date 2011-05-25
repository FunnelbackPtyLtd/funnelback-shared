package com.funnelback.contentoptimiser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.Hint;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingScore;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlInfoAndScore;


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
			UrlInfoAndScore info = new UrlInfoAndScore(result.getLiveUrl(),result.getTitle(),result.getRank());
	
			List<RankingScore> causes = info.getCauses();
			
			for (Map.Entry<String,Float> feature : result.getExplain().getFeatureScores().entrySet()) {
				float percentage = feature.getValue()*rp.getCoolerWeights().get(feature.getKey())  *100;
				causes.add(new RankingScore(feature.getKey(), percentage));
				Hint hint = hintsByName.get(feature.getKey());
				hint.rememberScore(percentage);
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
				for (UrlInfoAndScore url : comparison.getUrls()) {
					RankingScore toRemove = null;
					for (RankingScore cause : url.getCauses()) {
						if(cause.getName().equals(hint.getName())) {
							toRemove = (cause);
						}
					}
					if(toRemove != null) url.getCauses().remove(toRemove);
				}
				RankingScore toRemove = null;
				for (RankingScore cause :comparison.getImportantOne().getCauses()) {
					if(cause.getName().equals(hint.getName())) comparison.getUrls().remove(cause);
				}
				if(toRemove != null) comparison.getImportantOne().getCauses().remove(toRemove);
				comparison.getWeights().remove(hint.getName());
				comparison.getHintsByName().remove(hint.getName());				
				remove.add(hint);
			}
		}
		comparison.getHintsByWin().removeAll(remove);
	/*	comparison.getHints().add(new Hint("<b>Content: </b>The document at rank 4 has a slightly higher content score. This is because the query terms \"King Lear\" appear 1 more time than the document at rank 1.","#","document content"));
		comparison.getHints().add(new Hint("<b>Content: </b> Neither document has a meta description tag. Perhaps add a meta description tag which succinctly describes the content?", "#", "meta tags"));
		comparison.getHints().add(new Hint("<b>Anchors: </b> Both documents have the same number of incoming links. Perhaps add links to these pages from other pages on the site? Make sure that the link text accurately describes the page content.", "#", "anchors"));
		comparison.getHints().add(new Hint("<b>URL:</b> <b class='warn'>The document at rank 1 has a higher URL score.</b> This is because the URL contains the query term \"lear\" 1 more time, and it is slightly shorter. Do the URLs for both documents describe the content? Would a human be able to predict what the content found at the URL is without looking?", "#", "URL naming"));*/
	}
	
	@Override
	public void setImportantUrl(String url, UrlComparison comparison,ResultPacket original) {
		comparison.setImportantOne(comparison.getUrls().get(3));
		
		for (RankingScore cause : comparison.getImportantOne().getCauses()) {
			comparison.getHintsByName().get(cause.getName()).caculateWin(cause.getPercentage(),comparison.getWeights().get(cause.getName()));
		}
		Collections.sort(comparison.getHintsByWin());
	}


}


