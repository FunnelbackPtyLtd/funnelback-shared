<#ftl encoding="utf-8" />
<#setting number_format="computer">
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#include "content-optimiser-common-macros.ftl"/>
<#compress>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7; IE=EmulateIE9">
		
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jquery-1.6.2.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0b/jquery.jqplot.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0b/plugins/jqplot.categoryAxisRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0b/plugins/jqplot.canvasTextRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0b/plugins/jqplot.canvasAxisTickRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0b/plugins/jqplot.canvasAxisLabelRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0b/plugins/jqplot.barRenderer.min.js"></script>
	<!--[if IE]>
   		<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0b/excanvas.js"></script>
	<![endif]-->
	<link rel="stylesheet" type="text/css" href="${ContextPath}/content-optimiser/js/jqPlot-1.0.0b/jquery.jqplot.min.css"/>
	<link rel="stylesheet" type="text/css" href="${ContextPath}/content-optimiser/optimiser.css"/>
	
	<script type="text/javascript" src="/search/js/jquery/jquery-ui-1.8.14.dialog-only.min.js"></script>
	<title>Funnelback Content Optimiser</title>
</head>

<body>
	    <div id="content-optimiser-pane">
        <div style="margin-bottom: 30px;">
        	<h3 class="header">Content Optimiser Loading...</h3>
        	   	<img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36">
        	
        </div>
        	<div id="loading">
		    	<p style="padding-top: 30px; padding-bottom: 20px; text-align: center;">
		    		Examining the ranking. This will take several seconds....
		    	</p>
		    	<div style="position: relative; left: 50%; margin-left: -20px; padding-bottom: 20px;">
		    		<img style="" src="/search/optimiser-loading.gif" alt="loading"/>
		    	</div>
		    </div>
        	<p id="error" style="color: #ff0000; text-align: center;"></p>
        </div>
        <meta http-equiv="refresh" content="0; url=${urlToLoad}">
</body>

</#compress	>