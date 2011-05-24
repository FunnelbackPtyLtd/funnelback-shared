<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	
	<style type="text/css">
		body {
			font-family: Verdana;
		}

		ul {
			list-style-type: none;
			margin: 0;
			padding: 0;
		}
		h3 {
			text-align: center;
		}
		li {
			margin-left: 1em;
			margin-top: 1em;
			margin-right: 1em;
			padding: 0;
		}
		
		.tip {
			color: #444444;
		}
				
		.warn {
			color: #ff0000;
		}
		
		a.warn {
			color: #ff0000;
		}
		table {
			margin-top: 10px;
		}
		#legend {
			padding: 7px;
		}
		
	 	#content-optimiser-pane {
			padding: 10px; border: 1px solid; margin-bottom: 15px; width: 520px;
		}
		
		.section {
			width: 500px; border-top: 1px solid; margin: 10px;
		}
	</style>
	
	<script type="text/javascript" src="/publicui/dashboard/resources/js/jquery-1.4.4.min.js"></script>
	<script type="text/javascript" src="/publicui/dashboard/resources/js/jqPlot-1.0.0a/jquery.jqplot.min.js"></script>
	<script type="text/javascript" src="/publicui/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.categoryAxisRenderer.min.js"></script>
	<script type="text/javascript" src="/publicui/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.canvasTextRenderer.min.js"></script>
	<script type="text/javascript" src="/publicui/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.canvasAxisTickRenderer.min.js"></script>
	<script type="text/javascript" src="/publicui/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.canvasAxisLabelRenderer.min.js"></script>
	<script type="text/javascript" src="/publicui/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.barRenderer.min.js"></script>
		<title>Funnelback Content Optimiser</title>
</head>

<body>
    <div id="content-optimiser-pane">
        <h3>Content Optimiser</h3>
        <p class="tip">Tip: Drag search results in to this pane to compare them</p>
           
        <script type="text/javascript">
        	$(function() {
        		$("#barplot-cell").append('<div class="jqPlot" id="barplot" style="height:' + (50 * ${response.urlComparison.urls?size}) + 'px; width:300px;"></div>');
        		
	        	$.jqplot.config.enablePlugins = true;
	        
	        	
	        	<#list response.urlComparison.importantOne.causes as cause>
	        		var ${cause.name} = new Array();
	        		var important_${cause.name} = [[${cause.percentage},1]];
	        		var weight_${cause.name} = [[${response.urlComparison.weights[cause.name]},1]]
	        	</#list>
	        	
	        	

        		<#list response.urlComparison.urls as urlinfo>
        			<#list urlinfo.causes as cause>
        				${cause.name}.push([${cause.percentage},${response.urlComparison.urls?size} + 1 - ${urlinfo.rank}]);
        			</#list>
        		</#list>
	        	
				var barplot = $.jqplot('barplot', [
	        	<#list response.urlComparison.importantOne.causes as cause>
	        			${cause.name},
	        	</#list>
				
				], {
	    			stackSeries: true, 
	    			legend: {show: false},
	    			seriesDefaults: {
	        			renderer: $.jqplot.BarRenderer, 
	        			shadowAngle: 135, 
	        			rendererOptions: {barDirection: 'horizontal', barWidth: 20, barMargin: 5}
	    			}, 
	    			axes: {
	        			yaxis: {
				            renderer: $.jqplot.CategoryAxisRenderer, 
			    	        ticks: [
        					<#list response.urlComparison.urls as urlinfo>' ' ,</#list>
						    	        
			    	        ]
			        	},	
			        	xaxis: {min: 0, max: <#list response.urlComparison.importantOne.causes as cause> ${cause.name}[0][0] + </#list> 0
	        	, numberTicks:5,  tickRenderer: $.jqplot.CanvasAxisTickRenderer}
			    	}
				});
				
				var barplotImportant = $.jqplot('barplot-important', [
	        	<#list response.urlComparison.importantOne.causes as cause>
	        			important_${cause.name},
	        	</#list>
				
				], {
	    			stackSeries: true, 
	    			legend: {show: false},
	    			seriesDefaults: {
	        			renderer: $.jqplot.BarRenderer, 
	        			shadowAngle: 135, 
	        			rendererOptions: {barDirection: 'horizontal', barWidth: 20}
	    			}, 
	    			axes: {
	        			yaxis: {
				            renderer: $.jqplot.CategoryAxisRenderer, 
			    	        ticks: [' ']
			        	},	
			        	xaxis: {min: 0, max: 100, numberTicks:5,  tickRenderer: $.jqplot.CanvasAxisTickRenderer}
			    	}
				});
				
				var barplotWeight = $.jqplot('barplot-weights', [
	        	<#list response.urlComparison.importantOne.causes as cause>
	        			weight_${cause.name},
	        	</#list>
				
				], {
	    			stackSeries: true, 
	    			legend: {show: false},
	    			seriesDefaults: {
	        			renderer: $.jqplot.BarRenderer, 
	        			shadowAngle: 135, 
	        			rendererOptions: {barDirection: 'horizontal', barWidth: 20}
	    			}, 
	    			axes: {
	        			yaxis: {
				            renderer: $.jqplot.CategoryAxisRenderer, 
			    	        ticks: [' ']
			        	},	
			        	xaxis: {min: 0, max: 100, numberTicks:5,  tickRenderer: $.jqplot.CanvasAxisTickRenderer}
			    	}
				});				
				
				<#list response.urlComparison.importantOne.causes as cause>
	        		$("#legend").append('<span style="display: inline-block; padding: 2px; padding-left: 5px; padding-right: 5px;"><span style="display: inline-block;  width: 12px; height: 10px; background-color: '+barplot.seriesColors[${cause_index}%16]+' ">&nbsp;</span> <span>${cause.name}</span>');
	        	</#list>
	        	
	        	
	        	<#list response.urlComparison.importantOne.causes as cause>
	        		for(var i = 0; i < ${cause.name}.length;i++) {
	        			${cause.name}[i] = [${response.urlComparison.urls?size}+1-${cause.name}[i][1],${cause.name}[i][0]/${response.urlComparison.weights[cause.name]}*100];
	        		}
	        	
	        		var line_${cause.name} = [
	        		  <#list 0..response.urlComparison.urls?size as x>
	        		  	[${x},${cause.percentage}/${response.urlComparison.weights[cause.name]}*100],
	        		  </#list>
	        		  [${response.urlComparison.urls?size}+1, ${cause.percentage}/${response.urlComparison.weights[cause.name]}*100]
	        		];
	        		
	        		var plot_${cause.name} = $.jqplot('plot-${cause.name}', [${cause.name},line_${cause.name}], {
       				 	legend:{show:false},
       				 	axes:{
       				 		xaxis:{ticks: [ <#list 0..response.urlComparison.urls?size as x> ${x}, </#list> (${response.urlComparison.urls?size}+1) ],tickOptions:{formatString:'%d'}, min:0, max:${response.urlComparison.urls?size} +1,label: "Rank" , labelRenderer: $.jqplot.CanvasAxisLabelRenderer, tickRenderer: $.jqplot.CanvasAxisTickRenderer},
       				 		yaxis:{ 
       				 			label:'Score',
       				 			autoscale: true,
 							    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
 							    tickRenderer: $.jqplot.CanvasAxisTickRenderer
 							    }
       				 		},
        				series:[
            				{showLine:false, markerOptions:{style:'circle'},color: barplot.seriesColors[${cause_index}%16]},
            				{showLine:true, color:'#ff9999', showMarker:false},
		        		],
    				});
				</#list>
				
			
				});
        </script>
        <table>
                <tr><th>Rank</th><th>Title</th><th>Ranking caused by</th></tr>
           		<#list response.urlComparison.urls as urlinfo>
	                <tr>
	                	<td style="vertical-align: center;">
	                		<div style="overflow: hidden; white-space: nowrap; height: 43px; <#if urlinfo_index == 0> padding-top: 15px; </#if> <#if urlinfo.rank == response.urlComparison.importantOne.rank> background-color: #ffaaaa; </#if>">
	                			${urlinfo.rank}	                			
	                		</div>
	                	</td> 
	                	<td style="vertical-align: center;">
	                		<div style="overflow: hidden; white-space: nowrap; width: 160px; height: 43px;<#if urlinfo_index == 0> padding-top: 15px; </#if><#if urlinfo.rank == response.urlComparison.importantOne.rank> background-color: #ffaaaa; </#if> ">
	                			
	                			<a href="${urlinfo.url}"> ${urlinfo.title} </a>
	                			
	                		</div>
	                	</td> 
	                	<td <#if urlinfo_index == 0> rowspan="${response.urlComparison.urls?size}} " id="barplot-cell"</#if> >
	                	</td>
	                </tr>	
        	    </#list>
        	    <tr><td></td><td></td><td><div id="legend"></div></td></tr>
        </table>
        
    	<div class="section" <#if  (response.urlComparison.urls?size >= response.urlComparison.importantOne.rank) > style="display: none;" </#if> >
    		<h4>Selected Document</h4>
	        <table>
	                <tr><th>Rank</th><th>Title</th><th>Ranking caused by</th></tr>
	                <tr>
	                	<td style="vertical-align: center;">
	                		<div style="overflow: hidden; white-space: nowrap; height: 43px;padding-top: 15px; background-color: #ffaaaa;">
	                				${response.urlComparison.importantOne.rank}
	                		</div>
	                	</td> 
	                	<td style="vertical-align: center;">
	                		<div style="overflow: hidden; white-space: nowrap; width: 160px; height: 43px; padding-top: 15px; background-color: #ffaaaa;">
	                			<a href="${response.urlComparison.importantOne.url}"> ${response.urlComparison.importantOne.title} </a>
	                		</div>
	                	</td> 
	                	<td><div class="jqPlot" id="barplot-important" style="height:60px; width:300px;"></div></td>
	                </tr>	
			</table>
		</div>    	
        
        <div class="section">
	        <table>
	                <tr>
	                	<td style="vertical-align: center;">
	                		<div style="overflow: hidden; white-space: nowrap; width: 190px; height: 43px; padding-top: 15px;">
	                			Maximum possible scores:
	                		</div>
	                	</td> 
	                	<td><div class="jqPlot" id="barplot-weights" style="height:60px; width:300px;"></div></td>
	                </tr>	
			</table>
		</div>    	

        <#list response.urlComparison.hintsByWin as hint>
        	<div class="section">
        		<h4>${hint.name}</h4>
	        	<div class="jqPlot" id="plot-${hint.name}" style="height:300px; width:500px;"></div>
        	</div>
        </#list>
        
	</div>
</body>

</html>
