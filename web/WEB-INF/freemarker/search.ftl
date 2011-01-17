<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	
	<style type="text/css">
		body {
			font-family: Verdana;
		}
		
		div.error {
			background-color: #ffcccc;
		}
		
		h1 {
			font-size: large;
		}
		
		div#padre-info {
			background-color: #dedeff;
		}
		
		div#results {
			float:right;
			width: 80%;
			border: solid 1px grey;
		}
		
		ul#search-results {
			list-style-type: none;
			margin:0;
			padding: 0;
		}
		
		ul#search-results li {
			margin-top: 15px;
			margin-bottom: 15px;
			background-color: #deffde;
		}
		
		ul#search-results li p {
			margin: 3px;
		}
		
		p.url {
			font-family: monospace;
			font-size: small;
			padding-left: 10px;
		}
		
		p.url a {
			text-decoration: none;
		}
		
		p.url a:hover {
			background-color: cyan;
		}
		
		p.summary {
			padding: 5px;
			background-color: #cdeecd;
		}
		
		div#faceted-navigation {
			width: 20%;
			border: solid 1px grey;
			background-color: #eeeeee;
		}
		
	</style>
	
	<#if (SearchTransaction.question?exists && SearchTransaction.question.collection?exists)>
		<title>${SearchTransaction.question.collection.id}, Funnelback Search</title>
	<#else>
		<title>Funnelback Search</title>
	</#if>
</head>

<body>

<#if SearchTransaction.error?exists>
	<div class="error">
		<h3>Oops...</h3>
		<p>${SearchTransaction.error.reason}</p>
		<pre>${SearchTransaction.error.additionalData!""}</pre>
	</div>
<#else>

	<p>Searching on collection <strong>${SearchTransaction.question.collection.id}</strong></p>

	<form>
		<input type="hidden" name="collection" value="${SearchTransaction.question.collection.id}" />
		<input type="text" name="query" value="${SearchTransaction.question.query!""}"/>
		<input type="submit" value="Search" />
	</form>

	<#if SearchTransaction.response?exists>
		<div id="results">
		
			<div id="padre-info">
				Query: <tt>${SearchTransaction.response.resultPacket.query}</tt><br />
				Query as processed: <tt>${SearchTransaction.response.resultPacket.queryAsProcessed}</tt><br />
			</div>
		
			<h1>Results</h1>
	
			<p id="summary">
				${SearchTransaction.response.resultPacket.resultsSummary.currStart}
				to
				${SearchTransaction.response.resultPacket.resultsSummary.currEnd}
				of
				${SearchTransaction.response.resultPacket.resultsSummary.totalMatching}
			</p>
		
			<ul id="search-results">
				<#list SearchTransaction.response.resultPacket.results as result>
					<li>${result.rank}. ${result.title?html}
						<p class="url">Title link: <a href="click.cgi?collection=${result.collection}&rank=${result.rank}&index_url=${result.liveUrl?url}">click.cgi?collection=${result.collection}&rank=${result.rank}&index_url=${result.liveUrl}</a>
						<p class="url">Display URL: <a href="${result.displayUrl?url}">${result.displayUrl}</a>
						<p class="url">Live URL: <a href="${result.liveUrl?url}">${result.liveUrl}</a></p>
						<p class="url">Cached URL: <a href="${result.cacheUrl}">${result.cacheUrl}</a></p>
						<p class="summary">${result.summary}</p>
					</li>
				</#list>
			</ul>
		
			<#if (SearchTransaction.response.resultPacket.resultsSummary.currStart - SearchTransaction.response.resultPacket.resultsSummary.numRanks >= 0)>
				<a href="?query=${SearchTransaction.question.query}&amp;collection=${SearchTransaction.question.collection.id}&amp;start_rank=${SearchTransaction.response.resultPacket.resultsSummary.currStart - SearchTransaction.response.resultPacket.resultsSummary.numRanks}&num_ranks=${SearchTransaction.response.resultPacket.resultsSummary.numRanks}">Prev</a>
			</#if>
			
			<#if (SearchTransaction.response.resultPacket.resultsSummary.currStart + SearchTransaction.response.resultPacket.resultsSummary.numRanks <= SearchTransaction.response.resultPacket.resultsSummary.totalMatching)>
				<a href="?query=${SearchTransaction.question.query}&amp;collection=${SearchTransaction.question.collection.id}&amp;start_rank=${SearchTransaction.response.resultPacket.resultsSummary.currStart + SearchTransaction.response.resultPacket.resultsSummary.numRanks}&num_ranks=${SearchTransaction.response.resultPacket.resultsSummary.numRanks}">Next</a>		
			</#if>
		
		</div>
		<#if SearchTransaction.response.facets?exists && (SearchTransaction.response.facets?size > 0)>
			<div id="faceted-navigation">
				<h1>Facets</h1>
				
				<ul id="facets">
					<#list SearchTransaction.response.facets as facet>
					<li class="facet">${facet.name}</li>
					<li>
						<ul class="categories">
						<#list facet.categories as category>
							<li>
								<a href="?query=${SearchTransaction.question.query}&amp;collection=${SearchTransaction.question.collection.id}&amp;start_rank=${SearchTransaction.response.resultPacket.resultsSummary.currStart + SearchTransaction.response.resultPacket.resultsSummary.numRanks}&num_ranks=${SearchTransaction.response.resultPacket.resultsSummary.numRanks}&meta_${facet.name}_phrase_sand=$++ ${category.title} $++">${category.title}</a>
								<span class="count">(${category.count})</span>
							</li>
						</#list>
						</ul>
					</li>
					</#list>
				</ul>
			</div>
		</#if>
		
	</#if>
</#if>

</body>

</html>
