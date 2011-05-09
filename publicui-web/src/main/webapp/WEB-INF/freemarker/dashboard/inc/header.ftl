<!DOCTYPE HTML>
<html>
<head>
	<title>Public UI - Dashboard</title>
	<link rel="stylesheet" type="text/css" href="${contextPath}/dashboard/resources/dashboard.css" />
	<link rel="stylesheet" type="text/css" href="${contextPath}/dashboard/resources/js/jqPlot-1.0.0a/jquery.jqplot.css" />
	
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/jquery-1.4.4.min.js"></script>
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/jquery.sparkline.min.js"></script> 
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/dygraph-combined.js"></script>
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/jqPlot-1.0.0a/jquery.jqplot.min.js"></script>
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.cursor.min.js"></script>
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.pieRenderer.min.js"></script>
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.barRenderer.min.js"></script>
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.categoryAxisRenderer.min.js"></script>
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.dateAxisRenderer.min.js"></script>
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.canvasTextRenderer.min.js"></script>
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.canvasAxisTickRenderer.min.js"></script>
	<script type="text/javascript" src="${contextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.highlighter.min.js"></script>
	
</head>

<body>

<nav id="main">
	<img src="${contextPath}/dashboard/resources/funnelback-logo.png" />
	<ul>
		<li><a href="${contextPath}/dashboard/caches/list">caches</a></li>
		<li class="sep">|</li>
		<li><a href="${contextPath}/dashboard/stats/list">stats</a></li>
		<li class="sep">|</li>
		<li><a href="${contextPath}/dashboard/settings/list">settings</a></li>
		<li class="sep">|</li>
		<li><a href="${contextPath}/dashboard/collections/list">collections</a></li>
	</ul>
</nav>

<section>

	<div id="ajax-messages"><p class="error"></p><p class="success"></p></div>

