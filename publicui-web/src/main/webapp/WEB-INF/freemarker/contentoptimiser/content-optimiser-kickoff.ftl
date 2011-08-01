<#include "content-optimiser-common-macros.ftl"/>
<html lang="en"> 
<head> 
	<title> 
      Content Optimiser : Funnelback Search
	</title> 
  
	<meta name="robots" content="nofollow"> 
	<script type="text/javascript" src="/search/js/jquery/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="/search/js/jquery/jquery-ui-1.8.14.dialog-only.min.js"></script>
	<meta http-equiv="Content-Type" content="text/html;charset=utf-8">	
        <link rel="stylesheet" media="screen" href="/search/10.1.0-search.css" type="text/css"> 
	<!--[if !IE]>--> 
        <link rel="stylesheet" media="handheld, only screen and (max-device-width: 480px)" href="/search/10.1.0-search-mobile.css" type="text/css"> 
	<!--<![endif]--> 
	<link rel="stylesheet" type="text/css" href="${ContextPath}/content-optimiser/optimiser.css"/>
</head> 
<body> 
     <div id="content-optimiser-pane">
		<div id="fb-initial"> 
			<a href="http://funnelback.com/"><img src="/search/funnelback.png" alt="Funnelback logo"></a> 
		</div> 
	
 
 
	<!-- SEARCH LOGO -->	
	<div><a id="fb-logo" class="fb-initial" href="http://funnelback.com/"><img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36"></a></div> 
 
 
	<!-- QUERY FORM --> 
	<div id="fb-queryform" class="fb-initial" > 
		Content Optimiser
		<form action="optimise.html" method="GET"> 
			<div id="form"> 
				<label for="query" style="text-indent: -9999em;">Optimise</label> 
				<input name="query" id="query" type="text" style="margin-right: 5px;" value="" accesskey="q">Query 
				 
				<label for="optimiser_url" style="text-indent: -9999em;">URL</label> 
				<input name="optimiser_url" id="optimiser_url" style="width: 300; font-size: 1em; padding: 2px 4px; margin-right: 5px;" type="text" value="" accesskey="o">URL  
				<input type="hidden" name="collection" value="${collection}"/>
				
				<input type="submit" value="Optimise">                         
		</div> 
        	</form> 
	</div> <!-- #fb-queryform -->  
 
	<!-- SEARCH --> 

 	</div>
 	<@content_optimiser_loading/>
</body></html>