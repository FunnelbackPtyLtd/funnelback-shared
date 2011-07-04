package com.funnelback.contentoptimiser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.Hint;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.HintCollection;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlInfoAndScore;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

@Log
@Component
public class DefaultUrlCausesFiller implements UrlCausesFiller {

	@Autowired
	DocFromCache docFromCache;
	
	@Autowired
	I18n i18n;


	
	// TODO replace with an implementation that gets this from padre's XML
	private String getCategory(String key) {
		String[] content = {"content","imp_phrase","recency","an_okapi","BM25F_rank","nonbin","BM25F","no_ads","geoprox"};
		Set<String> contentSet = new HashSet<String>(Arrays.asList(content));
		String[] link = {"offlink","onlink","host_linked_hosts_score","host_click_score","host_linking_hosts_score","host_incoming_link_score"};
		Set<String> linkSet = new HashSet<String>(Arrays.asList(link));
		String[] url = {"urllen","mainhosts","host_domain_shallowness_score","urltype"};
		Set<String> urlSet = new HashSet<String>(Arrays.asList(url));
		String[] beyond = {"qie","document_number","host_rank_in_crawl_order_score","comp_wt","domain_weight"};
		Set<String> beyondSet = new HashSet<String>(Arrays.asList(beyond));
		String[] annie = {"annie_rank","log_annie","anlog_annie","annie_rank","consistency","annie"};
		Set<String> annieSet = new HashSet<String>(Arrays.asList(annie));
		
		if(contentSet.contains(key)) return "content";
		if(urlSet.contains(key)) return "URL";
		if(beyondSet.contains(key)) return "administrator controlled";
		if(linkSet.contains(key)) return "link based";
		if(annieSet.contains(key)) return "annotation";
		
		throw new RuntimeException(key + " is missing");
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
		qie.add("The Query Independent Evidence (QIE) score for your document is low. This score is configured by the administrator of your funnelback installation. Ask your administrator how the score is currently calculated and how you can improve it.");
		m.put("qie", qie);
		List<String> recency = new ArrayList<String>();
		recency.add("Ensure that any dates in the page are accurate and that the content is up to date. The recency score for your document is low - this score is determined by date(s) available in your page content.");
		m.put("recency", recency);
		List<String> urltype = new ArrayList<String>();
		urltype.add("The URL type score of your page is low:");
		urltype.add("Ensure that your page URL does not contain excessive punctuation.");
		urltype.add("Try moving your page to the sites home page (if it is not already).");
		urltype.add("Ensure that your page does not contain copyright content.");
		m.put("urltype", urltype);
		List<String> annie = new ArrayList<String>();
		annie.add("The annotation score for your document is low. This is calculated from features like the link text of links pointing to your page, and search queries where users clicked your page.");
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
		content.add("<b class=\"warn\">TODO CONTENT SCORE BREAKDOWN</b>");
		m.put("content",content);


		return m;
	}
	
	// Reads the weights and top 10 results from the result packet. 
	@Override
	public void consumeResultPacket(UrlComparison comparison, ResultPacket rp,HintFactory hintFactory) {
		
		// Add weights, create hint objects
		for (Entry<String, Float> weightEntry :  rp.getCoolerWeights().entrySet()) {
			comparison.getWeights().put(weightEntry.getKey(), weightEntry.getValue() * 100);
		}

		Map<String, Hint> hintsByName = comparison.getHintsByName();
		for (Entry<String,String> nameAndType : rp.getExplainTypes().entrySet()) {
			Hint h = hintFactory.create(nameAndType.getKey(),nameAndType.getValue(),getCategory(nameAndType.getKey()),rp);
			hintsByName.put(nameAndType.getKey(),h);
			comparison.getHintsByWin().add(h);			
		}

		
		// Fill in results with re-weighted scores
		for (Result result : rp.getResults()) {
			if(comparison.getUrls().size() >= 10) break;
			
			UrlInfoAndScore info = new UrlInfoAndScore(result.getLiveUrl(),result.getCacheUrl(),result.getTitle(),""+ result.getRank());
	
			for (Map.Entry<String,Float> feature : result.getExplain().getFeatureScores().entrySet()) {
				float percentage = feature.getValue()*rp.getCoolerWeights().get(feature.getKey())  *100;
				//causes.add(new RankingScore(feature.getKey(), percentage));
				Hint hint = hintsByName.get(feature.getKey());
				hint.rememberScore(percentage,"" +result.getRank());
			}
			comparison.getUrls().add(info);
		}
	}
	

	// Called after an importantUrl has been selected. Groups hints together into a set of features
	@Override
	public void fillHintCollections(UrlComparison comparison) {
		// First remove uninteresting features
		List<Hint> remove = new ArrayList<Hint>();		
		for (Hint hint : comparison.getHintsByWin()) {
			if(!hint.isInteresting()) {
				remove.add(hint);
				comparison.getHintsByName().remove(hint.getName());
			}
		}
		comparison.getHintsByWin().removeAll(remove);
		
		for (Hint hint : comparison.getHintsByWin()) {
			if(! comparison.getHintCollectionsByName().containsKey(hint.getCategory())) {
				HintCollection hc = new HintCollection(hint.getCategory());
				comparison.getHintCollections().add(hc);
				comparison.getHintCollectionsByName().put(hint.getCategory(), hc);
			}
		
				hint.getHintTexts().addAll(getHintTextMap().get(hint.getName()));
		
			comparison.getHintCollectionsByName().get(hint.getCategory()).getHints().add(hint);
		}
		Collections.sort(comparison.getHintCollections());
	}
	
	
	@Override
	public void setImportantUrl(UrlComparison comparison,SearchTransaction searchTransaction) {
		
		ResultPacket allRp = searchTransaction.getResponse().getResultPacket();
		String urlString = searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.OPTIMISER_URL);

		// See if the selected document appears for the long query
		Result importantResult = null;
		for (Result result : allRp.getResults()) {
			if(result.getDisplayUrl().equals(urlString) || result.getDisplayUrl().equals("http://" + urlString)) importantResult = result;
		}	
		
		// If the selected document didn't appear in the long query, then terminate early
		if(importantResult == null) {
			comparison.getMessages().add(i18n.tr("info.selectedDocumentTooFarDown"));
			return;
		}
		
		// First see if the model already contains the selected document (it will if it's in the top 10)
		for (UrlInfoAndScore url : comparison.getUrls()) {
			if(url.getUrl().equals(importantResult.getDisplayUrl())) {
				comparison.setImportantOne(url);
			}
		}
		
		// Otherwise we must create it ourselves
		if(comparison.getImportantOne() == null) {
			UrlInfoAndScore url = new UrlInfoAndScore(importantResult.getLiveUrl(),importantResult.getCacheUrl(),importantResult.getTitle(), ""+importantResult.getRank());
			comparison.setImportantOne(url);
			for (Map.Entry<String,Float> feature : importantResult.getExplain().getFeatureScores().entrySet()) {
				float percentage = feature.getValue()*allRp.getCoolerWeights().get(feature.getKey())  *100;
				Hint hint = comparison.getHintsByName().get(feature.getKey());
				hint.rememberScore(percentage, "" +importantResult.getRank());
			}
		}

		// now calculate the wins for each feature;
		for (Map.Entry<String,Float> feature : importantResult.getExplain().getFeatureScores().entrySet()) {
			float percentage = feature.getValue()*allRp.getCoolerWeights().get(feature.getKey())  *100;
			Hint hint = comparison.getHintsByName().get(feature.getKey());
			hint.caculateWin(percentage, allRp.getCoolerWeights().get(feature.getKey())*100);
		}
		
		Collections.sort(comparison.getHintsByWin());
	}

	@Override
	public void obtainContentBreakdown(UrlComparison comparison,
			SearchTransaction searchTransaction, ResultPacket importantRp,AnchorModel anchors) {
		String documentContent = docFromCache.getDocument(comparison, importantRp.getResults().get(0).getCacheUrl(),searchTransaction.getQuestion().getCollection().getConfiguration());
		if(documentContent != null) {
			DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor(documentContent,anchors);
			String[] queryWords = searchTransaction.getResponse().getResultPacket().getQueryCleaned().split("\\s+");
			
			for(String queryWord : queryWords){
				DocumentContentScoreBreakdown content = dwp.explainQueryTerm(queryWord,searchTransaction.getQuestion().getCollection());
				comparison.getMessages().add("Query term \"<b>" + queryWord + "</b>\" appears " + content.getCount() + " time(s) in the raw document. "
							+ "It is more common than " + content.getPercentageLess() + "% of other terms in the document. ");
				if(content.getCounts().size() != 0) {
					RankerOptions rOpt = new RankerOptions(searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.QUERY_PROCESSOR_OPTIONS));
					
					StringBuilder sb = new StringBuilder();
					sb.append("In addition, \"<b>" + queryWord + "</b>\" has: ");
					for(Map.Entry<String, Integer> e : content.getCounts()) {
						sb.append(e.getValue() + " occurences in metadata field '"+ e.getKey() +"', which has weight " + rOpt.getMetaWeight(e.getKey()) + "; ");
					}
					comparison.getMessages().add(sb.toString());
				}
				
			}
			comparison.getMessages().add("There are " + dwp.totalWords() + " total words in the document. " +  dwp.uniqueWords() + " of those words are unique. The top 5 words are " + Arrays.toString(dwp.getTopFiveWords(importantRp.getStopWords(),"_")));
		} else {
			// we didn't get a document back from cache
		}
	}


}


