<#include "content-optimiser-common-macros.ftl"/>
<!DOCTYPE html>
<html lang="en"> 
<head> 
  	<meta charset="utf-8" />
	<meta name="robots" content="nofollow"> 
	<title> 
      Content Optimiser : Funnelback Search
	</title> 

	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7; IE=EmulateIE9">
    <style>
/* QUERY FORM */

#fb-initial { text-align: center; }
#fb-logo.fb-initial { display: none; }
#fb-queryform.fb-initial #fb-logo { display: none; }
#fb-queryform.fb-initial { 
    width: 400px;
    margin: 0 auto;
    padding-left: 10px; }
#fb-queryform form { margin: 0; padding: 0; }
#fb-queryform {
    padding: 10px 0 10px 0;
    margin-left: 190px;
    position: relative;
}
#fb-queryform label, #fb-advanced label { 
    display: block;
    font-size: .9em;
    font-style: italic;
    padding-left: 3px; }
#fb-queryform label[for=facetScope], #fb-queryform-advanced label[for=facetScope] { display: inline; }
#fb-queryform input[type=submit], #fb-queryform-advanced input[type=submit] { font-size: 1em; }
#query, #fb-advanced #query-advanced { 
    width: 300px;
    font-size: 1em;
    padding: 2px 4px; }
#fb-advanced #query-advanced { margin-top: 1px; }
#fb-advanced-note { font-size: 0.85em; }
    
    </style>
    <link rel="stylesheet" type="text/css" href="${ContextPath}/content-optimiser/js/jqPlot-1.0.0b/jquery.jqplot.min.css"/>
	<link rel="stylesheet" type="text/css" href="${ContextPath}/content-optimiser/optimiser.css"/>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0b/jquery-1.4.4.min.js"></script>
	<!--[if IE]>
   		<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0b/excanvas.js"></script>
	<![endif]-->
	
	<script type="text/javascript" src="/search/js/jquery/jquery-ui-1.8.14.dialog-only.min.js"></script>
</head> 
<body> 

     <div id="content-optimiser-pane">
		<div id="fb-initial"> 
			<a href="http://funnelback.com/"><img src="/search/funnelback.png" alt="Funnelback logo"></a> 
		</div> 
		
		<!-- SEARCH LOGO -->	
		<div>
			<a id="fb-logo" class="fb-initial" href="http://funnelback.com/"><img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36"></a>
		</div> 
		<div class="section">
			<h2 style="text-align: center;">Content Optimiser
				<#if Request.advanced??>Advanced</#if>
			</h2> 
			<@content_optimiser_requery/>
		</div>
 	</div>
</body></html>
