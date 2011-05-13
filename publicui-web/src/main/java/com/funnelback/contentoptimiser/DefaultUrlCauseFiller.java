package com.funnelback.contentoptimiser;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;


@Component
public class DefaultUrlCauseFiller implements UrlCausesFiller {

	@Override
	public void consumeResultPacket(UrlComparison comparison, ResultPacket rp) {
		// TODO Auto-generated method stub
		
		
		for (Result result : rp.getResults()) {
			UrlInfoAndScore info = new UrlInfoAndScore();
			
			info.url = result.getLiveUrl();
			info.rank = result.getRank();
			info.title = result.getTitle();
	
			List<RankingScore> causes = info.getCauses();
			
			for (Map.Entry<String,Float> feature : result.getExplain().getFeatureScores().entrySet()) {
				causes.add(new RankingScore(feature.getKey(), feature.getValue()*rp.getCoolerWeights().get(feature.getKey())  *100 ));
			}
			comparison.getUrls().add(info);
		}
	}
	
	@Override
	public void fillHints(UrlComparison comparison) {
		comparison.hints.add(new Hint("<b>Content: </b>The document at rank 4 has a slightly higher content score. This is because the query terms \"King Lear\" appear 1 more time than the document at rank 1.","#","document content"));
		comparison.hints.add(new Hint("<b>Content: </b> Neither document has a meta description tag. Perhaps add a meta description tag which succinctly describes the content?", "#", "meta tags"));
		comparison.hints.add(new Hint("<b>Anchors: </b> Both documents have the same number of incoming links. Perhaps add links to these pages from other pages on the site? Make sure that the link text accurately describes the page content.", "#", "anchors"));
		comparison.hints.add(new Hint("<b>URL:</b> <b class='warn'>The document at rank 1 has a higher URL score.</b> This is because the URL contains the query term \"lear\" 1 more time, and it is slightly shorter. Do the URLs for both documents describe the content? Would a human be able to predict what the content found at the URL is without looking?", "#", "URL naming"));
	}

	@Override
	public void addUrl(String url, UrlComparison comparison) {
		UrlInfoAndScore info = new UrlInfoAndScore();
		
		info.url = url;
		info.rank = comparison.getUrls().size() + 1;
		info.title = "Title of this URL";
		
		comparison.getUrls().add(info);
	}

	@Override
	public void setImportantUrl(String url, UrlComparison comparison) {
		comparison.setImportantOne(comparison.getUrls().get(3));
	}


}

