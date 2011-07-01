<#ftl encoding="utf-8" />
<#import "/share/freemarker/funnelback_legacy.ftl" as s/>
<#setting number_format="computer">
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
	
		th {
			color: #444444;
			font-weight: normal;		
		}


		li {
			margin-left: 1em;
			margin-top: 1em;
			margin-right: 1em;
			padding: 0;
		}
		
		.messages {
			background-color: #ffaaaa;
			border: 1px solid #ff0000;
		}
				
		.warn {
			color: #ff0000;
		}
		
		.tip {
			color: #444444;
			margin-bottom: 20px;
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
		.jqplot-xaxis-label {
			padding-bottom: 20px;
		}
	 	#content-optimiser-pane {
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
		
		#chartpseudotooltip {
			padding: 1px; border: 1px solid #aaaaaa; background-color: #F6E3CE;
		}
		
		.section {
			width: 780px; border-top: 1px solid; margin: 10px;
		}
	</style>
	
	<script type="text/javascript" src="${ContextPath}/dashboard/resources/js/jqPlot-1.0.0a/jquery-1.4.4.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/dashboard/resources/js/jqPlot-1.0.0a/jquery.jqplot.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.categoryAxisRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.canvasTextRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.canvasAxisTickRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.canvasAxisLabelRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/dashboard/resources/js/jqPlot-1.0.0a/plugins/jqplot.barRenderer.min.js"></script>
	<link rel="stylesheet" type="text/css" href="${ContextPath}/dashboard/resources/js/jqPlot-1.0.0a/jquery.jqplot.min.css"/>
		<title>Funnelback Content Optimiser</title>
</head>

<body>
	
    <div id="content-optimiser-pane">
        <div style="margin-bottom: 30px;">
        	<h3 style="position: relative; top: 34px; left: 195px; margin: 0px; padding: 0px;">	Content Optimiser</h3>
        	<img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36">
        	
        </div>
        <#if (response.urlComparison.messages?size > 0)>
			<div class="messages">
				<ul>
				 	<#list response.urlComparison.messages as message>
				 		<li>${message}</li>	
				 	</#list>
				</ul>
			</div>        
        </#if>
        <div class="summary">
        	<#if (response.urlComparison.urls?size > 0)>
        		<p>Showing results 1-${response.urlComparison.urls?size} of ${response.resultPacket.resultsSummary.fullyMatching?string.number} fully matching documents for the query &quot;<b><@s.QueryClean/></b>&quot;.
        						<p>Top document (rank 1) is titled <a href="${response.urlComparison.urls[0].url}">${response.urlComparison.urls[0].title}</a>.
	        	<#if (response.urlComparison.importantOne??)>
					<p>Selected document is at rank <span style="color: #ff0000;">${response.urlComparison.importantOne.rank}</span> and is titled <a href="${response.urlComparison.importantOne.url}">${response.urlComparison.importantOne.title}</a>. 
					 You can view it in the cache <a href="${response.urlComparison.importantOne.cacheUrl}">here</a></p>
	        	</#if> 
	        		<p>Here is a breakdown of the ranking for the top ${response.urlComparison.urls?size} documents:</p>
        	</#if>
        </div>   
           
        <script type="text/javascript">
			var featureNames = new Array();
        	function showTip (ev, seriesIndex, pointIndex, data ) {
			    mouseX = ev.pageX; //these are going to be how jquery knows where to put the div that will be our tooltip
			    mouseY = ev.pageY;
	            $('#chartpseudotooltip').html(featureNames[seriesIndex]);
	            var cssObj = {
	                  'position' : 'absolute',
	                  'font-weight' : 'bold',
	                  'left' : (mouseX + 20) + 'px',
	                  'top' : mouseY + 'px'
	                };
	            $('#chartpseudotooltip').stop(true,true);
				$('#chartpseudotooltip').show();
	            $('#chartpseudotooltip').css(cssObj);
	        }   
	        
	        function hideTip() {
	        	$('#chartpseudotooltip').fadeOut('slow');
	        }

        	$(function() {
        		$("#barplot-cell").append('<div class="jqPlot" id="barplot" style="height:' + (50 * ${response.urlComparison.urls?size?c}) + 'px; width:435px;"></div>');
        		
        		
	     //   	$.jqplot.config.enablePlugins = true;
	        

	        	<#list response.urlComparison.hintsByWin as hint>
        				var ${hint.name} = new Array();
        				featureNames.push('${hint.name}');
						<#if response.urlComparison.importantOne?? > var important_${hint.name} = [[${hint.scores[response.urlComparison.importantOne.rank]},1]]; </#if>
		        		var weight_${hint.name} = [[${response.urlComparison.weights[hint.name]},1]]
       	    	</#list>
	        	
	        	

        		<#list response.urlComparison.urls as urlinfo>
        			<#list response.urlComparison.hintsByWin as hint>
       					${hint.name}.push([${hint.scores[urlinfo.rank]},${response.urlComparison.urls?size?c} + 1 - ${urlinfo.rank}]);
        			</#list>
        		</#list>
      	
				var barplot = $.jqplot('barplot', [
	        	<#list response.urlComparison.hintsByWin as hint>
	        			${hint.name},
	        	</#list>
				
				], {
	    			stackSeries: true, 
	    			legend: {show: false},
	    			seriesDefaults: {
	        			renderer: $.jqplot.BarRenderer,
						showTooltip: true, 
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
			        	xaxis: {min: 0, max: 
			        		<#list response.urlComparison.hintsByWin as hint>
				        			${hint.name}[0][0] +
			        		</#list> 0
	        	, numberTicks:5,  tickRenderer: $.jqplot.CanvasAxisTickRenderer}
			    	}
				});
				$('#barplot').bind('jqplotDataHighlight', showTip);
			    $('#barplot').bind('jqplotDataUnhighlight', hideTip);

			    $('body').bind('mousemove', 
			        function (ev) {
			            var cssObj = {
			                  'position' : 'absolute',
			                  'font-weight' : 'bold',
			                  'left' : (ev.pageX + 20) + 'px', //usually needs more offset here
			                  'top' : ev.pageY + 'px'
			                };
			             $('#chartpseudotooltip').css(cssObj);   
			        }
			    );
							
				<#if response.urlComparison.importantOne??> 
					var barplotImportant = $.jqplot('barplot-important', [
		        	<#list response.urlComparison.hintsByWin as hint>
		        			important_${hint.name},
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
				        	xaxis: {min: 0, max: 
				        		<#list response.urlComparison.hintsByWin as hint>
					        			${hint.name}[0][0] +
				        		</#list> 0, numberTicks:5,  tickRenderer: $.jqplot.CanvasAxisTickRenderer}
				    	}
					});
					$('#barplot-important').bind('jqplotDataHighlight', showTip);
			    	$('#barplot-important').bind('jqplotDataUnhighlight', hideTip);
					
				</#if>
				
				var barplotWeight = $.jqplot('barplot-weights', [
	        	<#list response.urlComparison.hintsByWin as hint>
	        			weight_${hint.name},
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
				$('#barplot-weights').bind('jqplotDataHighlight', showTip);
		    	$('#barplot-weights').bind('jqplotDataUnhighlight', hideTip);

				
				<#list response.urlComparison.hintsByWin as hint>
	        			$("#legend").append('<span style="display: inline-block; padding: 2px; padding-left: 5px; padding-right: 5px;"><span style="display: inline-block;  width: 12px; height: 10px; background-color: '+barplot.series[${hint_index}].color+' ">&nbsp;</span> <span>${hint.name}</span>');
	        	</#list>
	        	
	        	
				<#list response.urlComparison.hintsByWin as hint>
	        		for(var i = 0; i < ${hint.name}.length;i++) {
	        			${hint.name}[i] = [${response.urlComparison.urls?size?c}+1-${hint.name}[i][1],${hint.name}[i][0]/${response.urlComparison.weights[hint.name]}*100];
	        		}
	        		<#if response.urlComparison.importantOne??> 
		        		var line_${hint.name} = [
		        		  <#list 0..response.urlComparison.urls?size as x>
		        		  	[${x},${hint.scores[response.urlComparison.importantOne.rank]}/${response.urlComparison.weights[hint.name]}*100],
		        		  </#list>
		        		  [${response.urlComparison.urls?size?c}+1, ${hint.scores[response.urlComparison.importantOne.rank]}/${response.urlComparison.weights[hint.name]}*100]
		        		];
		        	</#if>
	        		var plot_${hint.name} = $.jqplot('plot-${hint.name}', [${hint.name},
	        			<#if response.urlComparison.importantOne??> line_${hint.name} </#if>
	        		], {
	        			title: '${hint.name}',
	    				axesDefaults: {       				 	
       				 			labelRenderer: $.jqplot.CanvasAxisLabelRenderer, 
       				 			tickRenderer: $.jqplot.CanvasAxisTickRenderer,
						},       				 	
       				 	axes:{
       				 		xaxis:{
       				 			ticks: [ <#list 0..response.urlComparison.urls?size as x> ${x}, </#list> (${response.urlComparison.urls?size?c}+1) ],
       				 			tickOptions:{formatString:'%d'}, 
       				 			min:0, 
       				 			max:${response.urlComparison.urls?size?c} +1,
       				 			label: "Rank", 
       				 			pad: 1,
       				 			},
       				 		yaxis:{ 
       				 			label:'Score',
 
 							   }
       				 		},
        				series:[
            				{showLine:false, markerOptions:{style:'x'},color: barplot.series[${hint_index}].color},
            				<#if response.urlComparison.importantOne??>  {showLine:true, color:'#ff9999', showMarker:false}, </#if>
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
	                		<div style="overflow: hidden; white-space: nowrap; height: 43px; <#if urlinfo_index == 0> padding-top: 20px; </#if> <#if response.urlComparison.importantOne?? && urlinfo.rank == response.urlComparison.importantOne.rank> background-color: #ffaaaa; </#if>">
	                			${urlinfo.rank}	                			
	                		</div>
	                	</td> 
	                	<td style="vertical-align: center;">
	                		<div style="overflow: hidden; white-space: nowrap; width: 300px; height: 43px;<#if urlinfo_index == 0> padding-top: 20px; </#if><#if response.urlComparison.importantOne?? && urlinfo.rank == response.urlComparison.importantOne.rank> background-color: #ffaaaa; </#if> ">	                			
	                			<a href="?query=${question.inputParameterMap["query"]!?first!?url}&collection=${question.inputParameterMap["collection"]!?first!?url}&profile=${question.inputParameterMap["profile"]!?first!?url}&optimiser_url=${urlinfo.url?url}"> 
		                			${urlinfo.title} 
	                			</a>
	                			
	                		</div>
	                	</td> 
	                	<td <#if urlinfo_index == 0> rowspan="${response.urlComparison.urls?size?c}} " id="barplot-cell"</#if> >
	                	</td>
	                </tr>	
        	    </#list>
        
        </table>
   		<#if response.urlComparison.importantOne??> 
	    	<div <#if ! (response.urlComparison.importantOne.rank?number > 10)> style="display: none;" </#if> >
	    		<p>The document you selected was not in the top 10. It's rank and ranking scores were:</p>
		        <table>
		                <tr>
		                	<td style="vertical-align: center; width: 38px;">
		                		<div style="overflow: hidden; white-space: nowrap; height: 43px;padding-top: 20px;">
		                				${response.urlComparison.importantOne.rank}
		                		</div>
		                	</td> 
		                	<td style="vertical-align: center;">
		                		<div style="overflow: hidden; white-space: nowrap; width: 300px; height: 43px; padding-top: 20px;">
		                			<a href="${response.urlComparison.importantOne.url}"> ${response.urlComparison.importantOne.title} </a>
		                		</div>
		                	</td> 
		                	<td><div class="jqPlot" id="barplot-important" style="height:60px; width:435px;"></div></td>
		                </tr>	
				</table>
			</div>
		</#if>

        <div class="section">
	        <table>
	                <tr>
	                	<td style="vertical-align: center;">
	                		<div style="text-align: right; overflow: hidden; white-space: nowrap; width: 340px; height: 43px; padding-top: 15px;">
	                			Weighting of each score:
	                		</div>
	                	</td> 
	                	<td><div class="jqPlot" id="barplot-weights" style="height:60px; width:435px;"></div></td>
	                </tr>	
			</table>
		</div>    	
		<div class="section">
		    <h4 style="float: left; padding-right: 30px; padding-bottom: 0px; margin-bottom: 0px;">Key</h4>
			<div id="legend"></div>
		</div>
		<#if response.urlComparison.importantOne??>
			<div class="section">
				<p>Here is a breakdown of the best ways to improve the ranking of the selected page. Categories are sorted by the potential improvement to the ranking - so improvement suggestions listed first will be the most effective. Red lines in the graphs indicate the current score for the selected document.</p> 
			</div>
			<#list response.urlComparison.hintCollections as hc>
			    <div class="section" style="clear: both;">
			    	<div style="float: left; width:400px;">
			    		<#if (hc.win <= 0)>
			    			<p class="tip">The content optimiser can't see an easy way to improve the ${hc.name} features</p>
			    		<#else>
					    	<h4 style="text-align: center;">Advice for improving ${hc.name} features</h4>
					    	<#list hc.hints as hint>
					    		<#if (hint.hintTexts?size > 0) && (hint.win > 0)>
					    			<ul>
							        	<#list hint.hintTexts as text>
							        		<li><b>${hint.name}:</b> ${text}</li>
							        	</#list>
						        	</ul>
					       		</#if>
							</#list>
						</#if>
					</div>   				
			    	<div style="float: right; width:350px;">
			    		<#assign shownHide = 0>
			    		<h4 style="text-align: center;" <#if (hc.win <= 0)>class="tip"</#if>>Individual features</h4>
					    <#list hc.hints as hint>
					    	<#if (hint.win <= 0)>
					    		<#if shownHide == 0>
					    			<div class="boring-${hc_index} tip" style="display: none;">Several unimprovable features hidden. 
					    				<a href="show features" onclick="$('.boring-${hc_index}').toggle(); return false;">Show features</a>
					    			</div>
					    			<div class="boring-${hc_index} tip">The features below are unimprovable. 
					    				<a href="hide features" onclick="$('.boring-${hc_index}').toggle(); return false;">Hide features</a>
					    			</div>
					    			<#assign shownHide =1>
					    		</#if>
			    				<div class="boring-${hc_index}">
			    			</#if>
					    	<div class="jqPlot" id="plot-${hint.name}" style="height:300px; width:350px;"></div>
					    	<#if (hint.win <= 0)>
					    		</div>
					    	</#if>
					    </#list>
					    <script type="text/javascript">
					    	// Start the features hidden.
					    	$(function() {
					    		$('.boring-${hc_index}').toggle();
					    	});
					    </script>
					</div>
	        	</div>
		   </#list>
        </#if>
        <div style="clear: both;">&nbsp;</div>
	</div>
	               	<div id="chartpseudotooltip" style="display: none;"/>
</body>

</html>
