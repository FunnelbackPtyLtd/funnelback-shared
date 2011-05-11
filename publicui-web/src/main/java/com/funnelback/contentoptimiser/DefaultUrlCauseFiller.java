package com.funnelback.contentoptimiser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

@Log
@Component
public class DefaultUrlCauseFiller implements UrlCausesFiller {

	@Override
	public void FillCauses(UrlComparison comparison) {
		for(int i = 0;i < comparison.getUrls().size();i++) {
			List<RankingScore> causes = comparison.getUrls().get(i).getCauses();
			causes.add(new RankingScore("Content"));
			causes.add(new RankingScore("Anchors"));
			causes.add(new RankingScore("URL"));
			causes.add(new RankingScore("meta_tag"));
		}
/*		List<RankingScore> causes = comparison.getImportantOne().getCauses();
		causes.add(new RankingScore("Content"));
		causes.add(new RankingScore("Anchors"));
		causes.add(new RankingScore("URL"));
		causes.add(new RankingScore("meta_tag"));*/
	
		for(int i = 0;i < comparison.getUrls().size();i++) {
			log.debug("Unsorted: "+ comparison.getUrls().get(i).sum());
		}
		
		// todo: remove this later - it's fake!
		Collections.sort(comparison.getUrls());
		for(int i = 0;i < comparison.getUrls().size();i++) {
			comparison.getUrls().get(i).setRank(i+1);
			log.debug("Sorted: "+ comparison.getUrls().get(i).sum());
		}
		
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
		if(comparison.getUrls().size() <= 4) { 
			UrlInfoAndScore info = new UrlInfoAndScore();
			
			info.url = url;
			info.rank = 4;
			info.title = "Title of this URL";
			List<RankingScore> causes = info.getCauses();
			causes.add(new RankingScore("Content"));
			causes.add(new RankingScore("Anchors"));
			causes.add(new RankingScore("URL"));
			causes.add(new RankingScore("meta_tag"));
			
			comparison.setImportantOne(info);
		} else {
			comparison.setImportantOne(comparison.getUrls().get(4));
		}
	}

}

