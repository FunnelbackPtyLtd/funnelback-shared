<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	<meta name="robots" content="nofollow" />
	<link rel="stylesheet" media="screen" href="/search/search.css" type="text/css" />
	<script type="text/javascript" src="/search/js/jquery/jquery-1.4.2.min.js"></script>
	
	<title>${SearchTransaction.question.collection.id}, Funnelback Search</title>
	
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
				jQuery(this).children('.category:gt(5)').css('display', 'none');
			});
			
			jQuery('.moreOrLessCategories>a').each( function() {
				var nbCategories = jQuery(this).parent().parent('div.facet').children('div.category').size();
				if ( nbCategories <= 5 ) {
					jQuery(this).css('display', 'none');
				} else if (nbCategories > 5) {
					jQuery(this).click( function() {
						if (jQuery(this).text().indexOf('more...') < 0) {
							jQuery(this).parent().parent('div.facet').children('div.category:gt(5)').css('display', 'none');
							jQuery(this).text('more...');
						} else {
							jQuery(this).parent().parent('div.facet').children('div.category').css('display', 'block');
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
				document.location.replace(document.URL.replace(/search..../, 'search.' + format));
			});			
		});
	</script>
	
	<style type="text/css">
		span.change-format {
			font-size: small;
		}
		
		div.sub-categories {
			margin-left: 1em;
		}
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
            	<input name="query" id="query" type="text" value="${SearchTransaction.question.query!""}" accesskey="q">
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
		<p id="fb-matching" <#if SearchTransaction.response.facets?exists>class="fb-with-faceting"</#if>>
			<span id="fb-utils">
				<a href="#" id="fb-advanced-toggle">Advanced search</a>
	            <span id="fb-help">| <a href="/search/help/simple_search.html">Help</a></span>
	        </span>
				
			<#if SearchTransaction.response.resultPacket.resultsSummary.totalMatching == 0>
				<span class="fb-result-count" id="fb-total-matching">0</span> search results for <strong>${SearchTransaction.question.query}</strong>
			<#else>
				<span class="fb-result-count" id="fb-page-start">${SearchTransaction.response.resultPacket.resultsSummary.currStart}</span> -
		        <span class="fb-result-count" id="fb-page-end">${SearchTransaction.response.resultPacket.resultsSummary.currEnd}</span> of
		        <span class="fb-result-count" id="fb-total-matching">${SearchTransaction.response.resultPacket.resultsSummary.totalMatching}</span>
	            search results for <strong>${SearchTransaction.question.query}</strong>	
			</#if>
		</p>
		
		<div id="fb-wrapper" <#if SearchTransaction.response.facets?exists>class="fb-with-faceting"</#if>>
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
							<h3><a href="click.cgi?collection=${result.collection}&rank=${result.rank}&index_url=${result.liveUrl?url}">${result.title}</a></h3>
							
							<p>
								<#if result.date?exists><span class="fb-date">${result.date?date?string.medium}:</span></#if>
								<span class="fb-summary">${result.summary}</span>
							</p>
							
							<p>
	            				<cite>${result.displayUrl}</cite>
	                            - <a class="fb-cached" href="${result.cacheUrl}" title="Cached version of ${result.title} (${result.rank})">Cached</a>
			               </p>
			               
			               <#if result.quickLinks?exists>
								<ul class="fb-quicklinks">
								<#list result.quickLinks.quickLinks as ql> 
									<li><a href="http://${ql.url}" title="${ql.text}">${ql.text}</a></li>
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
			
			<#if SearchTransaction.response.facets?exists && (SearchTransaction.response.facets?size > 0)>
			<div id="fb-facets">
				<form method="get">
					<input type="hidden" name="query" value="${SearchTransaction.question.query}" />
					<input type="hidden" name="collection" value="${SearchTransaction.question.collection.id}" />

					<input type="submit" value="Refine" />
								
					<#list SearchTransaction.response.facets as facet>
						<div class="facet">
							<h3><div class="facetLabel">${facet.name}</div></h3>
							<#list facet.categories as category>
								<div class="category">
									<#assign checked = "" />
									<#if SearchTransaction.question.selectedCategories[category.urlParams?split("=")[0]]?exists>
										<#if SearchTransaction.question.selectedCategories[category.urlParams?split("=")[0]]?seq_contains(category.label)>
											<#assign checked="checked=\"checked\"" />
										</#if>
									</#if>
									<input type="checkbox" name="${category.urlParams?split("=")[0]}" value="${category.urlParams?split("=")[1]}" ${checked} />
									<span class="categoryName">										
										<a href="?query=${SearchTransaction.question.query}&amp;collection=${SearchTransaction.question.collection.id}&amp;start_rank=${SearchTransaction.response.resultPacket.resultsSummary.currStart}&num_ranks=${SearchTransaction.response.resultPacket.resultsSummary.numRanks}&${category.urlParams}">${category.label}</a>
									</span>
									&nbsp;<span class="fb-facet-count">(<span class="categoryCount">${category.count}</span>)
									
									<div class="sub-categories">
										<#list category.categories as subCategory>
											<#assign checked = "" />
											<#if SearchTransaction.question.selectedCategories[subCategory.urlParams?split("=")[0]]?exists>
												<#if SearchTransaction.question.selectedCategories[subCategory.urlParams?split("=")[0]]?seq_contains(subCategory.label)>
													<#assign checked="checked=\"checked\"" />
												</#if>
											</#if>
									
											<input type="checkbox" name="${subCategory.urlParams?split("=")[0]}" value="${subCategory.urlParams?split("=")[1]}" ${checked} />
											<span class="categoryName">${subCategory.label}</span>
											&nbsp;<span class="fb-facet-count">(<span class="categoryCount">${subCategory.count}</span>)
											<br />
										</#list>
									</div>
									
								</div>
							</#list>
							<span class="moreOrLessCategories">
								<a href="#">more...</a>
							</span>
						</div>
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
