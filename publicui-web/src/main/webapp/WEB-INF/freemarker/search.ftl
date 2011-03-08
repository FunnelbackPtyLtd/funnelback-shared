<#include "facets.ftl" />
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	<meta name="robots" content="nofollow" />
	<link rel="stylesheet" media="screen" href="css/search.css" type="text/css" />
	<script type="text/javascript" src="js/jquery/jquery-1.5.1.min.js"></script>
	
	<#if SearchTransaction.question?exists && SearchTransaction.question.collection?exists>
		<title>${SearchTransaction.question.collection.id}, Funnelback Search</title>
	<#else>
		<title>Funnelback Search</title>
	</#if>
	
	<script type="text/javascript">
		function prevPage(offset) {
			var out = '';
			
			var qs = window.location.search.substring(1);
			var kvs = qs.split('&');
			for (i=0; i<kvs.length;i++) {
				var kv = kvs[i].split('=');
				out += '&';
				if (kv[0] == 'start_rank') {
					out += 'start_rank=' + offset;
				} else {
					out += kvs[i];
				}
			}

			window.location.search = '?' + out;
		}
		
		function nextPage(offset) {
			var out = '';
			
			var qs = window.location.search.substring(1);
			var kvs = qs.split('&');
			for (i=0; i<kvs.length;i++) {
				var kv = kvs[i].split('=');
				
				if (kv[0] == 'start_rank') {
					out += 'start_rank=' + offset;
				} else {
					out += kvs[i];
				}
				if (i+1 < kvs.length) {
					out += '&';
				}
			}
			if (out.indexOf('start_rank') < 0) {
				out += '&start_rank=' + offset;
			}
			window.location.search = '?' + out;
		}
		
		jQuery().ready(function() {
			jQuery('.facet').each( function() {
				jQuery(this).children('.category-type').each( function() {
					jQuery(this).children('.category:gt(5)').css('display', 'none');
				});
			});
			
			jQuery('.moreOrLessCategories>a').each( function() {
				var nbCategories = jQuery(this).parent().parent('.category-type').children('div.category').size();
				// alert(jQuery(this).parent().parent('.category-type').children('.category:eq(1)').text());
				if ( nbCategories <= 5 ) {
					jQuery(this).css('display', 'none');
				} else if (nbCategories > 5) {
					jQuery(this).click( function() {
						if (jQuery(this).text().indexOf('more...') < 0) {
							jQuery(this).parent().parent('.category-type').children('div.category:gt(5)').css('display', 'none');
							jQuery(this).text('more...');
						} else {
							jQuery(this).parent().parent('.category-type').children('div.category').css('display', 'block');
							jQuery(this).text('less...');
						}
					});
				}
			});
			
			jQuery('.change-format a').click(function () {
				var format = jQuery(this).text();
				if (format == 'html') {
					format = 'ftl';
				}
				document.location.replace(document.URL.replace(/\/search\..../, '/search.' + format));
			});			
		});
	</script>
	
	<style type="text/css">
		span.change-format {
			font-size: small;
		}
		
		div.sub-categories {
			margin-left: 1em;
			display: none;
		}
		
		div.apart {
			/* display: none; */
		}
		
		div.facet h4, div.facet h5 {
			margin: 0;
			color: grey;
		}
		
		/*
		#fb-wrapper.fb-with-faceting {
			padding-left: 300px;
		}
		
		div#fb-facets {
			width: 280px;
			overflow: scroll;
		}
		*/
	</style>
</head>

<body>

<#if SearchTransaction.error?exists>
	<div class="error">
		<h3>Oops...</h3>
		<p>${SearchTransaction.error.reason}</p>
		<pre>${SearchTransaction.error.additionalData!""}</pre>
	</div>
<#else>

	<#if ! SearchTransaction.question.query?exists>
		<div id="fb-initial">
			<a href="http://funnelback.com/"><img src="/search/funnelback.png" alt="Funnelback logo"></a>
		</div>	
	</#if>
 
	<div><a id="fb-logo" <#if ! SearchTransaction.question.query?exists>class="fb-initial"</#if> href="http://funnelback.com/"><img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36"></a></div>

	<!-- QUERY FORM -->
    <div id="fb-queryform" <#if ! SearchTransaction.question.query?exists>class="fb-initial"</#if>>
        <form method="GET">
            <div>
            	<label for="query" style="text-indent: -9999em;">Search</label>
            	<input name="query" id="query" type="text" value="${SearchTransaction.question.originalQuery!""}" accesskey="q">
            	<input type="submit" value="Search">
            	<input type="hidden" name="collection" value="${SearchTransaction.question.collection.id}">
            	<#if SearchTransaction.question.profile?exists>
            		<input type="hidden" name="profile" value="${SearchTransaction.question.profile}">
            	</#if>
            	<#if SearchTransaction.question.scope?exists>
            		<input type="hidden" name="scope" value="${SearchTransaction.question.scope}">
            	</#if>
				<!--           	
          			<s:FacetScope>Within selected categories only</s:FacetScope>
                	<s:IfDefCGI from-advanced><br /><span id="fb-advanced-note">Your search contained advanced operators [<a href="<s:env>SCRIPT_NAME</s:env>?fb-advanced=true&amp;<s:env>QUERY_STRING</s:env>" id="fb-advanced-view">view advanced search</a>]</span></s:IfDefCGI>
				-->
				&nbsp;&nbsp;&nbsp;
				<span class="change-format">
					&middot; <a href="#">html</a> &middot; <a href="#">xml</a> &middot; <a href="#">json</a> &middot;
				</span>
			</div>
			 
         </form>
        
    </div> 

	<#if SearchTransaction.question.query?exists>
		<p id="fb-matching" <#if SearchTransaction.response.facets?size &gt; 0>class="fb-with-faceting"</#if>>
			<span id="fb-utils">
				<a href="#" id="fb-advanced-toggle">Advanced search</a>
	            <span id="fb-help">| <a href="/search/help/simple_search.html">Help</a></span>
	        </span>

			<#if SearchTransaction.response.resultPacket.resultsSummary?exists>				
				<#if SearchTransaction.response.resultPacket.resultsSummary.totalMatching == 0>
					<span class="fb-result-count" id="fb-total-matching">0</span> search results for <strong>${SearchTransaction.response.resultPacket.queryAsProcessed}</strong>
				<#else>
					<span class="fb-result-count" id="fb-page-start">${SearchTransaction.response.resultPacket.resultsSummary.currStart}</span> -
			        <span class="fb-result-count" id="fb-page-end">${SearchTransaction.response.resultPacket.resultsSummary.currEnd}</span> of
			        <span class="fb-result-count" id="fb-total-matching">${SearchTransaction.response.resultPacket.resultsSummary.totalMatching}</span>
		            search results for <strong>${SearchTransaction.response.resultPacket.queryAsProcessed}</strong>	
				</#if>
			</#if>
		</p>
		
		<div id="fb-wrapper" <#if SearchTransaction.response.facets?size &gt; 0>class="fb-with-faceting"</#if>>
			<div id="fb-summary">
				<#if SearchTransaction.response.resultPacket.spell?exists>
					<span id="fb-spelling">Did you mean <a href="?${SearchTransaction.response.resultPacket.spell.url}">${SearchTransaction.response.resultPacket.spell.text}</a>?</span>
				</#if>
			</div>
			
			<#if SearchTransaction.response.resultPacket.bestBets?size &gt; 0>
				<#list SearchTransaction.response.resultPacket.bestBets as bb>
					<div class="fb-best-bet">
						<h3><a href="${bb.link}">${bb.title}</a></h3>
						<p>${bb.description}</p>
						<p><cite>${bb.link}</cite></p>
					</div>
				</#list>
			</#if>
			
			<ol id="fb-results">
				<#list SearchTransaction.response.resultPacket.resultsWithTierBars as result>
					<#if result.matched?exists>
						<h2 class="fb-title">Search results that match ${result.matched} of ${result.outOf} words</h2> 
					<#else>
						<li>
							<#if SearchTransaction.question.collection.configuration.valueAsBoolean("click_tracking")>
								<h3><a href="${result.clickTrackingUrl}" title="${result.liveUrl}">${result.title?html}</a></h3>
							<#else>
								<h3><a href="${result.liveUrl}">${result.title?html}</a></h3>
							</#if>
							
							
							<p>
								<#if result.date?exists><span class="fb-date">${result.date?date?string.medium}:</span></#if>
								<span class="fb-summary">${result.summary}</span>
							</p>
							
							<p>
	            				<cite>${result.displayUrl?html}</cite>
	                            - <a class="fb-cached" href="${result.cacheUrl}" title="Cached version of ${result.title} (${result.rank})">Cached</a>
	                            <a class="fb-explore" href="?collection=${SearchTransaction.question.collection.id}&amp;query=explore:${result.liveUrl}">Explore</a>
							</p>
			               
			               <#if result.quickLinks?exists>
								<ul class="fb-quicklinks">
								<#list result.quickLinks.quickLinks as ql> 
									<li><a href="http://${ql.url?url}" title="${ql.text}">${ql.text?html}</a></li>
								</#list> 
								</ul> 
			               </#if>
							
							
						</li>
					</#if>
				</#list>
			</ol>
			
			
			<p class="fb-page-nav">
				<#if SearchTransaction.response.resultPacket.resultsSummary.prevStart?exists>
					<a href="javascript:prevPage(${SearchTransaction.response.resultPacket.resultsSummary.prevStart});" class="fb-previous-result-page fb-page-nav">Prev ${SearchTransaction.response.resultPacket.resultsSummary.numRanks}</a>
				</#if>

				<#assign currentPage = 1>				
				<#if SearchTransaction.response.resultPacket.resultsSummary.currStart?exists && SearchTransaction.response.resultPacket.resultsSummary.numRanks?exists>
					<#assign currentPage = SearchTransaction.response.resultPacket.resultsSummary.currStart + (SearchTransaction.response.resultPacket.resultsSummary.numRanks-1) / SearchTransaction.response.resultPacket.resultsSummary.numRanks> 
				</#if>

				<#if SearchTransaction.response.resultPacket.resultsSummary.nextStart?exists>
					<a href="javascript:nextPage(${SearchTransaction.response.resultPacket.resultsSummary.nextStart});" class="fb-next-result-page fb-page-nav">Next ${SearchTransaction.response.resultPacket.resultsSummary.numRanks}</a>
				</#if>
			</p>
			
			<#if SearchTransaction.response.facets?size &gt; 0>
			<div id="fb-facets">
				<form method="get">
					<input type="hidden" name="query" value="${SearchTransaction.question.originalQuery}" />
					<input type="hidden" name="collection" value="${SearchTransaction.question.collection.id}" />
					<input type="submit" value="Refine" />
								
					<#list SearchTransaction.response.facets as facet>
						<#if facet.categoryTypes?size &gt; 0>
							<div class="facet">
								<h3><div class="facetLabel">${facet.name}</div></h3>
								<#list facet.categoryTypes as categoryType>
									<@facetCategoryType categoryType />
								</#list>
							</div>
						</#if>
					</#list>
				
				</form>
			
			</div>
			</#if>
			
			<#if SearchTransaction.response.resultPacket.contextualNavigation?exists && (SearchTransaction.response.resultPacket.contextualNavigation?size > 0)>
				<div id="fb-contextual-navigation">
					<h2>Have you tried?</h2>
					<#list SearchTransaction.response.resultPacket.contextualNavigation.categories as category>
						<#if category.name != 'site' || (category.name == 'site' && category.clusters?size > 1)>
							<div id="fb-contextual-navigation-type">
	                        	<span class="fb-fade"></span>
                        		<h3>
                        			<#if category.name == 'type'>Types of <strong>${SearchTransaction.response.resultPacket.contextualNavigation.searchTerm}</strong></#if>
                        			<#if category.name == 'topic'>Topics on <strong>${SearchTransaction.response.resultPacket.contextualNavigation.searchTerm}</strong></#if>
                        			<#if category.name == 'site'><em>${SearchTransaction.response.resultPacket.contextualNavigation.searchTerm}</em> by site</#if>
                        		</h3>
                        		<ul>
                        			<#list category.clusters as cluster>
                        				<li><a href="${cluster.href}">${cluster.label}</a></li>
                        			</#list>
                        			<#if category.moreLink?exists>
                        				<li class="fb-contextual-navigation-more"><a href="${category.moreLink}">more...</a></li>
                        			</#if>                     
                        		</ul>
	                    	</div>
	                    </#if>
					</#list>
				</div>
			</#if>
		</div>
		
	
	</#if>
</#if>

</body>
</html>
