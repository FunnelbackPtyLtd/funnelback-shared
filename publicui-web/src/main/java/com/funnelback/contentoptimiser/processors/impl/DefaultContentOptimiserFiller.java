package com.funnelback.contentoptimiser.processors.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.contentoptimiser.RankingFeatureFactory;
import com.funnelback.contentoptimiser.SingleTermFrequencies;
import com.funnelback.contentoptimiser.fetchers.BldInfoStatsFetcher;
import com.funnelback.contentoptimiser.fetchers.DocFromCache;
import com.funnelback.contentoptimiser.fetchers.InDocCountFetcher;
import com.funnelback.contentoptimiser.fetchers.impl.MetaInfoFetcher;
import com.funnelback.contentoptimiser.processors.ContentOptimiserFiller;
import com.funnelback.contentoptimiser.processors.DocumentWordsProcessor;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.DocumentContentModel;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeature;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeatureCategory;
import com.funnelback.publicui.utils.MapUtils;
import com.google.common.collect.SetMultimap;

@Log4j
@Component
public class DefaultContentOptimiserFiller implements ContentOptimiserFiller {

	private static final char[] UNSUPPORTED_QUERY_OPTIONS = new char[] {'"',':','[',']','!','-','+','|','`','*','<','#'};
	
	@Autowired
	DocFromCache docFromCache;
	
	@Autowired
	I18n i18n;
	
	@Autowired
	InDocCountFetcher inDocCountFetcher;
	
	@Autowired
	BldInfoStatsFetcher bldInfoStatsFetcher;
	
	@Autowired @Setter File searchHome;
	
	// TODO replace with an implementation that gets this from padre's XML
	private String getCategory(String key) {
		String[] content = {"content","imp_phrase","recency","an_okapi","BM25F_rank","nonbin","BM25F","no_ads","geoprox",RankingFeature.CONSAT,
				"entropy","entropy_abs","entropy_abs_neg","entropy_neg",
				"contentWords","contentWords_abs","contentWords_abs_neg","contentWords_neg",
				"distinctWords","distinctWords_abs_neg","distinctWords_abs","distinctWords_neg",
				"stopwordCover","stopwordCover_abs","stopwordCover_abs_neg","stopwordCover_neg",
				"averageTermLen","averageTermLen_abs","averageTermLen_abs_neg","averageTermLen_neg",
				"titleWords", "titleWords_abs",	"titleWords_abs_neg","titleWords_neg",
				"stopwordFraction","stopwordFraction_abs","stopwordFraction_abs_neg","stopwordFraction_neg",
				"maxFreq","maxFreq_abs","maxFreq_abs_neg","maxFreq_neg",
				"compressionFactor","compressionFactor_abs","compressionFactor_abs_neg", "compressionFactor_neg","lexical_span_score"};
		Set<String> contentSet = new HashSet<String>(Arrays.asList(content)); 
		String[] link = {"offlink","onlink","host_linked_hosts_score","host_click_score","host_linking_hosts_score","host_incoming_link_score"};
		Set<String> linkSet = new HashSet<String>(Arrays.asList(link));
		String[] url = {"urllen","mainhosts","host_domain_shallowness_score","urltype"};
		Set<String> urlSet = new HashSet<String>(Arrays.asList(url));
		String[] beyond = {"qie","document_number","host_rank_in_crawl_order_score","comp_wt","domain_weight","doc_matches_regex","doc_does_not_match_regex"};
		Set<String> beyondSet = new HashSet<String>(Arrays.asList(beyond));
		String[] annie = {"annie_rank","log_annie","anlog_annie","annie_rank","consistency","annie"};
		Set<String> annieSet = new HashSet<String>(Arrays.asList(annie));
		
		if(contentSet.contains(key)) return "content";
		if(urlSet.contains(key)) return "URL";
		if(beyondSet.contains(key)) return "administrator controlled";
		if(linkSet.contains(key)) return "link based";
		if(annieSet.contains(key)) return "annotation";
		
		throw new RuntimeException("Category for ranking feature '" + key + "' is missing");
	}

	// TODO: take this out and have it read in by XML somewhere
	private Map<String,List<String>> getHintTextMap() {
		Map<String,List<String>> m = new HashMap<String,List<String>>();
		
		List<String> urllen = new ArrayList<String>();
		urllen.add("Try shortening the URL. Make sure that there aren't any unnecessary characters in your URL (e.g. long numbers or extra words).");
		urllen.add("URLs should be succinct - a person should be able to read the URL and work out what content is stored there.");
		m.put("urllen",urllen);
		List<String> offlink = new ArrayList<String>();
		offlink.add("Try obtaining more links from other sites. Your site name is everything between the second and third slashes in the URL, so if your URL is http://<b>www.example.com</b>/somewhere/, then your site name is <b>www.example.com</b>. Add links to your content anywhere where the page URL has a different site name.");
		offlink.add("If you don't control any other sites, then contact administrators of sites where there are pages on which it would make sense to have links to your content.");
		m.put("offlink",offlink);
		List<String> onlink = new ArrayList<String>();
		onlink.add("Try obtaining more links from within your site. Your site name is everything between the second and third slashes in the URL, so if your URL is http://<b>www.example.com</b>/somewhere/, then your site name is <b>www.example.com</b>. Add links to your content anywhere where the page URL has the same site name as your page.");
		m.put("onlink",onlink);
		List<String> qie = new ArrayList<String>();
		qie.add("The Query Independent Evidence (QIE) score for your page is low. This score is configured by the administrator of your funnelback installation. Ask your administrator how the score is currently calculated and how you can improve it.");
		m.put("qie", qie);
		List<String> recency = new ArrayList<String>();
		recency.add("Ensure that any dates in the page are accurate and that the content is up to date. The recency score for your page is low - this score is determined by date(s) available in your page content.");
		m.put("recency", recency);
		List<String> urltype = new ArrayList<String>();
		urltype.add("Ensure that your page URL does not contain excessive punctuation.");
		urltype.add("Try moving your page to the sites home page (if it is not already).");
		urltype.add("Ensure that your page does not contain copyright content.");
		m.put("urltype", urltype);
		List<String> annie = new ArrayList<String>();
		annie.add("The annotation score for your page is low. This is calculated from features like the link text of links pointing to your page, and search queries where users clicked your page.");
		annie.add("Ensure that links to your page contain descriptive link text (eg &quot;<a href=\"#\" onclick=\"return false;\">campus map</a>&quot; instead of &quot;click <a href=\"#\" onclick=\"return false;\">here</a> for the campus map</a>&quot;.");
		annie.add("Perhaps users are not clicking on your page in response to the query. Ensure that the &lt;meta description&gt; tag and your page title both contain a good description of your page content. Ensure that both tags also contain the search terms that you expect users to be searching for when they want your page.");
		m.put("annie", annie);
		List<String> domain_weight = new ArrayList<String>();
		domain_weight.add("Your page is being beaten by pages that have a higher domain weight. The administrator of your search service can configure weights to give to results from certain domains. Ask your administrator which domains currently recieve these weights.");
		m.put("domain_weight",domain_weight);
		List<String> geoprox = new ArrayList<String>();
		geoprox.add("Your page has a low geographic proximity score with respect to this query. Ensure that the correct lattitude and longitude is present in the page.");
		m.put("geoprox",geoprox);
		
		List<String> nonbin = new ArrayList<String>();
		nonbin.add("Try changing the format of your page. Your page is in a format that may be hard to read for some users (such as .pdf or .doc). Pages in text, HTML or XML format are ranked higher.");
		m.put("nonbin",nonbin);
		
		List<String> no_ads = new ArrayList<String>();
		no_ads.add("Try removing advertisements from your page. Pages with fewer ads rank higher.");
		m.put("no_ads",no_ads);
		
		List<String> consistency = new ArrayList<String>();
		consistency.add("Your page will receive extra ranking score if it gets good scores for content and annotations. Follow all improvement steps for content features and annotation features to improve this score.");
		m.put("consistency",consistency);
		
		m.put("log_annie", new ArrayList<String>());
		m.put("anlog_annie", new ArrayList<String>());
		m.put("annie_rank", new ArrayList<String>());
		
		m.put("BM25F", new ArrayList<String>());
		m.put("BM25F_rank", new ArrayList<String>());
		m.put("an_okapi", new ArrayList<String>());
		
		List<String> mainhosts = new ArrayList<String>();
		mainhosts.add("It appears your page is not on a main host within it's domain. Main hostnames either start with www, or are stored at the root of the domain (e.g. http://example.com instead of http://somewhere.example.com ).");
		m.put("mainhosts",mainhosts);

		List<String> comp_wt = new ArrayList<String>();
		comp_wt.add("Your page is being beaten by pages from a sub collection with a higher weight. The administrator of your search service can configure weights to give to results from certain sub collections. Ask your administrator whether the sub collection weights should be changed.");
		m.put("comp_wt",comp_wt);
		
		List<String> document_number = new ArrayList<String>();
		document_number.add("The document number score is low for your page. Pages earlier in the crawl are usually more important pages in the collection. Ask the administrator of your Funnelback installation about giving your page an earlier position in the crawl.");
		m.put("document_number",document_number);
		
		List<String> host_incoming_link_score = new ArrayList<String>();
		host_incoming_link_score.add("The overall link score to the host that your page is on is low. Try obtaining more links to pages on your host from other hosts.");
		m.put("host_incoming_link_score",host_incoming_link_score);
		
		List<String> host_click_score = new ArrayList<String>();
		host_click_score.add("Your page is being beaten by pages that have received more clicks in search results. Ensure that the &lt;meta description&gt; tag and your page title both contain a good description of your page content. Ensure that both tags also contain the search terms that you expect users to be searching for when they want your page.");
		m.put("host_click_score",host_click_score);

		List<String> host_linking_hosts_score = new ArrayList<String>();
		host_linking_hosts_score.add("The host that your page is on does not have enough unique hosts linking to it. Try soliciting links from hosts that do not currently link to pages on your host.");
		m.put("host_linking_hosts_score",host_linking_hosts_score);

		List<String> host_linked_hosts_score = new ArrayList<String>();
		host_linked_hosts_score.add("The host that your page is on does not link to enough other unique hosts. Try adding links to other hosts from pages on your host.");
		m.put("host_linked_hosts_score",host_linked_hosts_score);
		
		List<String> host_rank_in_crawl_order_score = new ArrayList<String>();
		host_rank_in_crawl_order_score.add("The host your page is on has a late rank in the crawl. Hosts earlier in the crawl are usually more important pages in the collection. Ask the administrator of your Funnelback installation about giving your host an earlier position in the crawl.");
		m.put("host_rank_in_crawl_order_score",host_rank_in_crawl_order_score);			
		
		List<String> host_domain_shallowness_score = new ArrayList<String>();
		host_domain_shallowness_score.add("Your page URL does not appear to be very 'shallow'. Shallow URLs have fewer '.'s in the hostname part, and are generally more important. Try moving your page to a shallower hostname.");
		m.put("host_domain_shallowness_score",host_domain_shallowness_score);			

		List<String> imp_phrase = new ArrayList<String>();
		imp_phrase.add("Your page receieved a low phrase matching score. For multiple word queries, you can improve your implicit phrase matching score by making sure that the query terms appear close together early in the document, or in the title. <b>Note: One word queries will never receive phrase matching scores.</b>");
		m.put("imp_phrase",imp_phrase);

		List<String> content = new ArrayList<String>();
		content.add("<b class=\"warn\">Content score breakdown unavailable</b>");
		m.put("content",content);
		
		m.put(RankingFeature.CONSAT, new ArrayList<String>());

		
		return m;
	}
	
	// Reads the weights and top 10 results from the result packet. 
	@Override
	public void consumeResultPacket(ContentOptimiserModel comparison, ResultPacket rp,RankingFeatureFactory hintFactory) {
		
		// Add weights, create hint objects
		for (Entry<String, Float> weightEntry :  rp.getCoolerWeights().entrySet()) {
			comparison.getWeights().put(weightEntry.getKey(), weightEntry.getValue() * 100);
		}

		Map<String, RankingFeature> hintsByName = comparison.getHintsByName();
		for (Entry<String,String> nameAndType : rp.getExplainTypes().entrySet()) {
			RankingFeature h = hintFactory.create(nameAndType.getKey(),nameAndType.getValue(),getCategory(nameAndType.getKey()),rp);
			hintsByName.put(nameAndType.getKey(),h);
			comparison.getHintsByWin().add(h);			
		}
		
		// also include a "consat" ranking feature in case not all constraints were satisfied
		RankingFeature h = hintFactory.create(RankingFeature.CONSAT,RankingFeature.CONSAT,getCategory(RankingFeature.CONSAT),rp);
		hintsByName.put(RankingFeature.CONSAT,h);
		comparison.getHintsByWin().add(h);	

		boolean seenEasterEgg = false;
		// Fill in results with re-weighted scores
		for (Result result : rp.getResults()) {
			if(comparison.getTopResults().size() >= 10) break;
	
			if(result.getExplain() != null) {
				if(seenEasterEgg) { 
					result.setRank(result.getRank() + 1);
				} 
				for (Map.Entry<String,Float> feature : result.getExplain().getFeatureScores().entrySet()) {
					float percentage = feature.getValue()*rp.getCoolerWeights().get(feature.getKey())  *100;
					//causes.add(new RankingScore(feature.getKey(), percentage));
					RankingFeature hint = hintsByName.get(feature.getKey());
					hint.rememberScore(percentage,"" +(result.getRank()));
				}
			} else {
				// this is the easter egg query, produce null hints
				for(Map.Entry<String,Float> feature : rp.getCoolerWeights().entrySet()) {
					RankingFeature hint = hintsByName.get(feature.getKey());
					hint.rememberScore(feature.getValue() * 100,"" +result.getRank());
				}
				seenEasterEgg = true;
			}
			comparison.getTopResults().add(result);
		}
	}
	

	// Called after an importantUrl has been selected. Groups hints together into a set of features
	@Override
	public void fillHintCollections(ContentOptimiserModel comparison) {
		// First remove uninteresting features
		List<RankingFeature> remove = new ArrayList<RankingFeature>();		
		for (RankingFeature hint : comparison.getHintsByWin()) {
			if(!hint.isInteresting()) {
				remove.add(hint);
				comparison.getHintsByName().remove(hint.getName());
			}
		}
		comparison.getHintsByWin().removeAll(remove);
		
		for (RankingFeature hint : comparison.getHintsByWin()) {
			if(! comparison.getHintCollectionsByName().containsKey(hint.getCategory())) {
				RankingFeatureCategory hc = new RankingFeatureCategory(hint.getCategory());
				comparison.getHintCollections().add(hc);
				comparison.getHintCollectionsByName().put(hint.getCategory(), hc);
			}
		
			if (getHintTextMap().get(hint.getName()) != null) {
				hint.getHintTexts().addAll(getHintTextMap().get(hint.getName()));
			}
		
			comparison.getHintCollectionsByName().get(hint.getCategory()).getHints().add(hint);
		}
		Collections.sort(comparison.getHintCollections());
	}
	
	
	@Override
	public void setImportantUrl(ContentOptimiserModel comparison,SearchTransaction searchTransaction) {
		
		ResultPacket allRp = searchTransaction.getResponse().getResultPacket();
		String urlString = MapUtils.getFirstString(searchTransaction.getQuestion().getRawInputParameters(), RequestParameters.CONTENT_OPTIMISER_URL, null);

		// See if the selected document appears for the long query
		Result importantResult = null;
		for (Result result : allRp.getResults()) {
			Set<String> possibleUrls = new HashSet<String>();
			possibleUrls.add(urlString);
			possibleUrls.add(urlString + "/");
			possibleUrls.add("http://" + urlString);
			possibleUrls.add("http://"+ urlString + "/");
			
			if(possibleUrls.contains(result.getDisplayUrl())
					|| possibleUrls.contains(result.getLiveUrl())
					|| possibleUrls.contains(result.getIndexUrl())
					|| urlString.endsWith(result.getClickTrackingUrl().replaceFirst("&search_referer=.*",""))) {
				importantResult = result;
			}
		}	
		
		// If the selected document didn't appear in the long query, then terminate early
		if(importantResult == null) {
			comparison.getMessages().add(i18n.tr("info.selectedDocumentTooFarDown"));
			return;
		}
		
		// First see if the model already contains the selected document (it will if it's in the top 10)
		for (Result url : comparison.getTopResults()) {
			if(url.getDisplayUrl().equals(importantResult.getDisplayUrl())) {
				comparison.setSelectedDocument(url);
			}
		}
		
		// Otherwise we must create it ourselves
		if(comparison.getSelectedDocument() == null) {
			comparison.setSelectedDocument(importantResult);
			for (Map.Entry<String,Float> feature : importantResult.getExplain().getFeatureScores().entrySet()) {
				float percentage = feature.getValue()*allRp.getCoolerWeights().get(feature.getKey())  *100;
				RankingFeature hint = comparison.getHintsByName().get(feature.getKey());
				hint.rememberScore(percentage, "" +importantResult.getRank());
			}
		}
		
		if(importantResult.getTier() != 1) {
			// this result didn't satisfy all the constraints, so we need to give the consat feature lots of score
			comparison.getHintsByName().get(RankingFeature.CONSAT).rememberScore(100 * importantResult.getExplain().getConsat(), "" + importantResult.getRank());
			for(Result r : comparison.getTopResults()) {
				comparison.getHintsByName().get(RankingFeature.CONSAT).rememberScore(100 * r.getExplain().getConsat(),"" +r.getRank());
			}
			comparison.getHintsByName().get(RankingFeature.CONSAT).caculateWin(0, importantResult.getTier());
			comparison.getWeights().put(RankingFeature.CONSAT, 1.0f);
		}

		// now calculate the wins for each feature;
		if(importantResult.getExplain() != null) for (Map.Entry<String,Float> feature : importantResult.getExplain().getFeatureScores().entrySet()) {
			float percentage = feature.getValue()*allRp.getCoolerWeights().get(feature.getKey())  *100;
			RankingFeature hint = comparison.getHintsByName().get(feature.getKey());
			hint.caculateWin(percentage, allRp.getCoolerWeights().get(feature.getKey())*100);
		}
		
		Collections.sort(comparison.getHintsByWin());
	}

	@Override
	public void obtainContentBreakdown(ContentOptimiserModel comparison,
			SearchTransaction searchTransaction, Result importantResult,AnchorModel anchors, SetMultimap<String,String> stemMatches) {
		DocumentContentModel content = new DocumentContentModel();
		
		
		if(stemMatches.entries().size() != 1) {
			// describe the stem equiv matches only if there's more than one
			// (a word will always stem to itself)
			for(Entry <String,String> stemMatch : stemMatches.entries()) {
				if(! content.getTermsToStemEquivs().containsKey(stemMatch.getValue())) {
					content.getTermsToStemEquivs().put(stemMatch.getValue(),new HashSet<String>());
				}
				content.getTermsToStemEquivs().get(stemMatch.getValue()).add(stemMatch.getKey());
			}
		}
		
		
		
		String documentContent = docFromCache.getDocument(comparison, importantResult.getCacheUrl(),searchTransaction.getQuestion().getCollection().getConfiguration(),importantResult.getCollection());
		if(documentContent != null) {
			DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor(documentContent,anchors,stemMatches);
			
			BldInfoStats bldInfoStats = null;
			try {
				bldInfoStats = bldInfoStatsFetcher.fetch(comparison, searchTransaction.getQuestion().getCollection());
			} catch (IOException e1) {
				comparison.getMessages().add(i18n.tr("error.readingBldinfo"));
				log.error("IOException when reading bldinfo file(s)",e1);
			}
							
			content.setTotalWords(dwp.getTotalWords());
			content.setUniqueWords(dwp.setUniqueWords());
			content.setCommonWords(Arrays.toString(dwp.getCommonWords(searchTransaction.getResponse().getResultPacket().getStopWords(),"_")));
			
			List<ContentHint> contentHints = new ArrayList<ContentHint>();
		
			MetaInfoFetcher mf = new MetaInfoFetcher(searchTransaction.getQuestion().getCollection(),searchTransaction.getQuestion().getProfile());
			try {
				mf.fetch(searchHome,searchTransaction.getQuestion().getProfile());
			} catch (FileNotFoundException e) {
				comparison.getMessages().add(i18n.tr("error.readingMetaInfo"));
				log.error("IOException when reading a meta-names.xml file",e);				
			}
			
			if(bldInfoStats != null) {
				contentHints.add(new ContentHint("The selected document is longer than the average document. Shorter documents are easier for users to digest. Try improving the clarity of the content by removing words,"+
						" or consider splitting this document into several shorter documents",dwp.getTotalWords() - bldInfoStats.getAvgWords()));
			}
			
			String queryFromRp = searchTransaction.getResponse().getResultPacket().getQueryCleaned();
			if(StringUtils.containsAny(queryFromRp, UNSUPPORTED_QUERY_OPTIONS)) {
				for(char c : UNSUPPORTED_QUERY_OPTIONS) {
					queryFromRp = queryFromRp.replace(c, ' ');
				}
				comparison.getMessages().add("The content optimiser does not support complex query options. For the purposes of generating content suggestions, the optimiser will interpret your query as \"" 
						+ queryFromRp + "\". This may result in inaccurate <strong>content</strong> suggestions.");
			}
			String[] queryWords = queryFromRp.split("\\s+");

			for(String queryWord : queryWords){
				SingleTermFrequencies frequencies = dwp.explainQueryTerm(queryWord,searchTransaction.getQuestion().getCollection());
				Map<String,Integer> inDocFreqs = inDocCountFetcher.getTermWeights(comparison,queryWord,anchors.getCollection());
				
				long totalDocuments = 0;
				if(bldInfoStats != null) totalDocuments = bldInfoStats.getTotalDocuments();

				for(Entry<String,Integer> metaFreqs : inDocFreqs.entrySet()) {
					Integer inDocFreq = inDocFreqs.get(metaFreqs.getKey());
					Integer termFreq = frequencies.getCount(metaFreqs.getKey());
					
					ContentHint contentHint = obtainContentHint(queryWord, totalDocuments,
							inDocFreq, termFreq, mf,
							metaFreqs.getKey());
					if(contentHint != null) {
						if(termFreq != 0) contentHint.setBucket(0);
						
						if(comparison.getSelectedDocument().getTier() != 1) {
							// this document doesn't satisfy all of the constraints
							if(termFreq == 0 && metaFreqs.getKey().equals("_")) {
								comparison.getHintsByName().get(RankingFeature.CONSAT).getHintTexts().add(contentHint.getHintText());
							}
						}
						contentHints.add(contentHint);
					}
				}
				if(inDocFreqs.get("_") == null ) {
					contentHints.add(new ContentHint("Query term '<strong>"+queryWord+"</strong>' does not appear in the document body for any documents in this collection.",10000));
				}
			}
			boolean cleared = false;
			if(contentHints.size() != 0) {
			
				Collections.sort(contentHints);
				for(ContentHint hint : contentHints) {
					if(hint.getScoreEstimate() > 0) {
						if(! cleared)	comparison.getHintsByName().get("content").getHintTexts().clear();
						comparison.getHintsByName().get("content").getHintTexts().add(hint.getHintText());
						cleared = true;
					} 
				}
				
				if(cleared == false) {
					// we did generate some suggestions, but we decided they were all of no value
					// so we display the generic help message
					comparison.getHintsByName().get("content").getHintTexts().clear();
					comparison.getHintsByName().get("content").getHintTexts().add("The content of the selected document appears well targetted to this query. " +
							" Consider asking the administrator of your search service to add this query and URL as a tuning test case.");
				}
			}
			
		}
		comparison.setContent(content);
	}

	private ContentHint obtainContentHint(String queryWord, long totalDocuments,
			Integer inDocFreq, Integer termFreq, MetaInfoFetcher mf,
			String metaClass) {
		double rawWeightForTerm = (Math.log((double)totalDocuments / inDocFreq.doubleValue())+1) * mf.getRankerOptions().getMetaWeight(metaClass);
		MetaInfo metaInfo = mf.get(metaClass);
		String metaTitle = metaInfo.getLongTitle();
		String metaHelp = metaInfo.getImprovementSuggestion();
		int metaThresh = metaInfo.getThreshold();
		
		String occurs = null;
		
		if(termFreq == 0) {
			occurs = "does not occur";
		} else if(termFreq < metaThresh) {
			occurs = "appears few times";
		} else {
			return null;
		}
		
		return new ContentHint("Query term \"<strong>" + queryWord + 
			"</strong>\" "+occurs+" in "+metaTitle+ 
			". " +  metaHelp ,
			(rawWeightForTerm * metaThresh) - (rawWeightForTerm * termFreq),
			inDocFreq.doubleValue()/totalDocuments);
	}

}



