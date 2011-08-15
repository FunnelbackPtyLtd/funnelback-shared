<#ftl encoding="utf-8" />
<#setting number_format="computer">
<#macro pageNumbers>
 	       	<#assign totalPages = (anchors.detail.size/max_urls_per_page)?int />
	       	<#assign currentPage = (anchors.detail.start/max_urls_per_page)?int>
	       	<#if (totalPages > 0)>
		       	<p>Page 
		       		<#if ((currentPage -5) >= 0)>
						<a href="?collection=${anchors.collection?url}&docNum=${anchors.docNum}&anchortext=${anchors.detail.linkAnchortext?url}&start=0">1</a>....
					</#if>
			       	<#list 0..totalPages as page>
			       		<#if (currentPage - 5 < page) && (currentPage + 10 > page)>
			       			<#if currentPage == page><strong></#if> 
			       			<a href="?collection=${anchors.collection?url}&docNum=${anchors.docNum}&anchortext=${anchors.detail.linkAnchortext?url}&start=${max_urls_per_page * page}">${page+1}</a>
			       			<#if currentPage == page></strong></#if>
			       		</#if>
			       	</#list>
			       	<#if ((currentPage +10) <= totalPages)>
						....<a href="?collection=${anchors.collection?url}&docNum=${anchors.docNum}&anchortext=${anchors.detail.linkAnchortext?url}&start=${totalPages * max_urls_per_page}">${totalPages+1}</a>
					</#if>
		       	</p>
		    </#if> 
</#macro>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	<style type="text/css">
		body {
			font-family: Arial, Helvetica, sans-serif;
			background-color: #F0F0F0;
		}
		h3 {
			color: #FF9F00;
			text-shadow: #DDD 1px 1px 0px;
			color: #FF9F00;
			letter-spacing: .2em;
			line-height: 30px;
			font-size: 24px;
			font-weight: normal;
			margin: 0px;
			padding-top: 0px;
		}
		
		.messages {
			background-color: #ffaaaa;
			border: 1px solid #ff0000;
		}
	
		#anchors-pane {
			padding: 10px; border: 1px solid #A0A0A4;
			margin-bottom: 15px; width: 800px; position: relative; left: 50%; margin-left: -400px;
			-moz-box-shadow: 2px 2px 8px #BBB;
			-webkit-box-shadow: 2px 2px 8px #BBB;
			box-shadow: 2px 2px 8px #BBB;
			background-color: #FFFFFF;
			-moz-border-radius: 10px;
			-webkit-border-radius: 10px;
			-khtml-border-radius: 10px;
			border-radius: 10px;
		}
		table, td, th {
			border: 1px solid #dddddd;
			border-spacing: 0px;
			border-collapse:collapse;
		}
		
	</style>
	<title>Anchors Information: ${anchors.collection} ${anchors.docNum}</title>
</head>
<body>
    <div id="anchors-pane">
        <div style="margin-bottom: 30px;">
        	<h3 style="position: relative; top: 34px; left: 195px; margin: 0px; padding: 0px;">Anchors Detail Summary</h3>
        	<img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36">
       	</div>
       	<#if anchors.error??> 
       		<div class="messages">
				<ul>
				 	<li>${anchors.error}</li>	
				</ul>
			</div>     
       	<#else>
	       	<ul>
	       		<li><strong>Collection</strong>: ${anchors.collection}</li>
	       		<li><strong>Document Number</strong>: ${anchors.docNum}</li>
	       		<li><strong>Distilled File</strong>: '${anchors.distilledFileName}'</li> 
	       		<li><strong>Anchor text</strong>: ${anchors.detail.linkAnchortext}</li>
				<li>Showing documents <strong>${anchors.detail.start?string.number}-${anchors.detail.end?string.number}</strong> of ${anchors.detail.size?string.number} total documents linking to <a href="${anchors.url}">${anchors.url}</a> in ${anchors.collection} collection with the anchor text <strong>${anchors.detail.anchortext}</strong></li>	 
	       	</ul>
 
			<@pageNumbers/>
	       	<table style="width: 100%;">
	       		
	       			<th>URL</th>
	       		</tr>
	       		<#list anchors.detail.urls as url>
	       			<tr>
	       				<td style="WORD-BREAK:BREAK-ALL; padding-bottom: 4px;"><a href="${url}">${url}</a></td>
	       			</tr>	
	       		</#list>
	       	</table>
			<@pageNumbers/>
	     </#if>
    </div>
</body>
</html>