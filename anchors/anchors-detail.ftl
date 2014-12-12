<#ftl encoding="utf-8" />
<#setting number_format="computer">
<#macro pageNumbers>
 	       	<#assign totalPages = (anchors.detail.size/max_urls_per_page)?int />
	       	<#assign currentPage = (anchors.detail.start/max_urls_per_page)?int>
	       	<#if (totalPages > 0)>
		       	<ul class="pagination pagination-sm">
		       		<#if ((currentPage -5) >= 0)>
						<li><a class="in" href="?collection=${anchors.collection?url}&docNum=${anchors.docnum}&anchortext=${anchors.detail.linkAnchortext?url}&start=0"> &laquo; </a><li>
					</#if>
			       	<#list 0..totalPages as page>
			       		<#if (currentPage - 5 < page) && (currentPage + 10 > page)>
			       			<li <#if currentPage == page>class="active"</#if>><a class="in" href="?collection=${anchors.collection?url}&docnum=${anchors.docNum}&anchortext=${anchors.detail.linkAnchortext?url}&start=${max_urls_per_page * page}"> ${page+1} </a><li>
			       			</#if>
			       	</#list>
			       	<#if ((currentPage +10) <= totalPages)>
						<a class="in" href="?collection=${anchors.collection?url}&docnum=${anchors.docNum}&anchortext=${anchors.detail.linkAnchortext?url}&start=${totalPages * max_urls_per_page}">&raquo;<#--${totalPages+1}--></a>
					</#if>
		       	</ul>
		    </#if> 
</#macro>
<#if !RequestParameters.ajax??>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	<style type="text/css">
		body {
			font-family: Arial, Helvetica, sans-serif;
			background-color: #F0F0F0;
			font-size: 90%;
			color: #222;
		}
		a {
		  color: #3e62a4;
		  text-decoration: none;
		}
		a:hover{ text-decoration: underline}
		a:visited{ color: #8276CB}
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
			background-color: #ffffff;
			border: 1px solid #ccc;
			left: 50%;
			margin: 0 auto;
			padding: 25px;
			width: 800px;

			-moz-border-radius: 4px;
			-webkit-border-radius: 4px;
			-khtml-border-radius: 4px;
			border-radius: 4px;
			-moz-box-shadow: 0 1px 1px #ccc;
			-webkit-box-shadow:0 1px 1px #ccc;
			box-shadow: 0 1px 1px #ccc;
		}
		#anchors-pane > div:first-child { margin-bottom: 30px}
		#anchors-pane h3{
			position: relative; top: 34px; left: 195px; margin: 0px; padding: 0px;
		}
		table, td, th {

		  border-color: #eee;
		  border-image: none;
		  border-spacing: 0;
		  border-style: solid;
		  border-width: 0 0 1px;
		  
		}
		td{padding: 8px 15px;}
		.pagination {
			border-radius: 4px;
			display: inline-block;
			margin: 20px 0;
			padding-left: 0;
		}
		.pagination > li > a, .pagination > li > span {
			background-color: #fff;
			border: 1px solid #ddd;
			float: left;
			line-height: 1.42857;
			margin-left: -1px;
			padding: 6px 12px;
			position: relative;
			text-decoration: none;
		}
		.pagination-sm > li > a, .pagination-sm > li > span {
			font-size: 12px;
			padding: 5px 10px;
		}
		.pagination > li {
			display: inline;
		}

		
	</style>
	<title>Anchors Information: ${anchors.collection} ${anchors.docNum}</title>
</head>
<body>
	</#if>
    <div id="anchors-pane">
        <div>
        	<h3>Anchors Detail Summary</h3>
        	<#if !RequestParameters.ajax??><img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36"></#if>
       	<#if anchors.error??> 
       		<div class="messages">
				<ul>
				 	<li>${anchors.error}</li>	
				</ul>
			</div>     
       	<#else>
		<p>Showing documents <strong>${anchors.detail.start?string.number}-${anchors.detail.end?string.number}</strong> of <b>${anchors.detail.size?string.number}</b> total documents linking to <a class="out" href="${anchors.url}">${anchors.url}</a> 
		<#if RequestParameters.ajax??><br></#if> in the <strong>${anchors.collection}</strong> collection with the link text <strong>${anchors.detail.anchortext}</strong></p>	 
	       		<!-- Document Number: ${anchors.docNum} -->
	       		<!-- Distilled File: ${anchors.distilledFileName} --> 
 
			<@pageNumbers/>
	       	<table style="width:100%;">
	       			<th>URL</th>
	       		</tr>
	       		<#list anchors.detail.urls as url>
	       			<tr>
	       				<td style="WORD-BREAK:BREAK-ALL; "><a class="out" href="${url}">${url}</a></td>
	       			</tr>	
	       		</#list>
	       	</table>

	       	<div class="pagination-wrap">
				<@pageNumbers/>
			</div>

	     </#if>
    </div>
    <#if !RequestParameters.ajax??>
</body>
</html>
</#if>