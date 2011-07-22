<#ftl encoding="utf-8" />
<#setting number_format="computer">
<#import "/share/freemarker/funnelback_legacy.ftl" as s/>
<#include "content-optimiser-common-macros.ftl"/>
<#compress>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
		
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0a/jquery-1.4.4.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0a/jquery.jqplot.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0a/plugins/jqplot.categoryAxisRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0a/plugins/jqplot.canvasTextRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0a/plugins/jqplot.canvasAxisTickRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0a/plugins/jqplot.canvasAxisLabelRenderer.min.js"></script>
	<script type="text/javascript" src="${ContextPath}/content-optimiser/js/jqPlot-1.0.0a/plugins/jqplot.barRenderer.min.js"></script>
	<link rel="stylesheet" type="text/css" href="${ContextPath}/content-optimiser/js/jqPlot-1.0.0a/jquery.jqplot.min.css"/>
	<link rel="stylesheet" type="text/css" href="${ContextPath}/content-optimiser/optimiser.css"/>
	<title>Funnelback Content Optimiser</title>
</head>

<body>
	
    <div id="content-optimiser-pane">
        <div style="margin-bottom: 30px;">
        	<h3 class="header">	Content Optimiser Advanced View</h3>
        	<img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36">
        	
        </div>
        
        <@content_optimiser_warnings/>

		<@content_optimiser_summary/>
		
		<p>Here is a breakdown of the ranking scores of the top documents:</p>
           
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
   
	        	<#list response.urlComparison.hintsByName?keys as hintkey>
	        			<#assign hint = response.urlComparison.hintsByName[hintkey] />
        				var ${hint.name} = new Array();
        				featureNames.push('${hint.longName}');
						<#if response.urlComparison.importantOne?? > var important_${hint.name} = [[${hint.scores[response.urlComparison.importantOne.rank?string]},1]]; </#if>
		        		var weight_${hint.name} = [[${response.urlComparison.weights[hint.name]},1]]
       	    	</#list>
	        	
	        	

        		<#list response.urlComparison.urls as urlinfo>
        			<#list response.urlComparison.hintsByName?keys as hintkey>
       					${response.urlComparison.hintsByName[hintkey].name}.push([${response.urlComparison.hintsByName[hintkey].scores[urlinfo.rank?string]},${response.urlComparison.urls?size?c} + 1 - ${urlinfo.rank}]);
        			</#list>
        		</#list>
      	
				var barplot = $.jqplot('barplot', [
	  			<#list response.urlComparison.hintsByName?keys as hintkey>
	        			${response.urlComparison.hintsByName[hintkey].name},
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
					<#list response.urlComparison.hintsByName?keys as hintkey>
	        			important_${response.urlComparison.hintsByName[hintkey].name},
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
				
				<#assign idx = 0 />
				<#list response.urlComparison.hintsByName?keys as hintkey>
	        			<#assign hint = response.urlComparison.hintsByName[hintkey] />
	        			$("#legend").append('<span class="legend-block"><span class="legend-colour" style="background-color: '+barplot.series[${idx}].color+' ">&nbsp;</span> <span>${hint.longName}</span>');
	        			<#assign idx = idx +1/>
	        	</#list>
	        	
	        	
				<#assign idx = 0 />
				<#list response.urlComparison.hintsByName?keys as hintkey>
	        		<#assign hint = response.urlComparison.hintsByName[hintkey] />
	        		for(var i = 0; i < ${hint.name}.length;i++) {
	        			${hint.name}[i] = [${response.urlComparison.urls?size?c}+1-${hint.name}[i][1],${hint.name}[i][0]/${response.urlComparison.weights[hint.name]}*100];
	        		}
	        		<#if response.urlComparison.importantOne??> 
		        		var line_${hint.name} = [
		        		  <#list 0..response.urlComparison.urls?size as x>
		        		  	[${x},${hint.scores[response.urlComparison.importantOne.rank?string]}/${response.urlComparison.weights[hint.name]}*100],
		        		  </#list>
		        		  [${response.urlComparison.urls?size?c}+1, ${hint.scores[response.urlComparison.importantOne.rank?string]}/${response.urlComparison.weights[hint.name]}*100]
		        		];
		        	</#if>
	        		var plot_${hint.name} = $.jqplot('plot-${hint.name}', [${hint.name},
	        			<#if response.urlComparison.importantOne??> line_${hint.name} </#if>
	        		], {
	        			title: '${hint.longName}',
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
            				{showLine:false, markerOptions:{style:'x'},color: barplot.series[${idx}].color},
            				<#if response.urlComparison.importantOne??>  {showLine:true, color:'#ff9999', showMarker:false}, </#if>
		        		],
    				});
    				<#assign idx = idx+1/>
				</#list>
				
			
				});
        </script>

        <table>
                <tr><th>Rank</th><th>Title</th><th>Ranking caused by</th></tr>
           		<#list response.urlComparison.urls as urlinfo>
	                <tr>
	                	<td>
	                		<div style="overflow: hidden; white-space: nowrap; height: 43px; <#if urlinfo_index == 0> padding-top: 20px; </#if> <#if response.urlComparison.importantOne?? && urlinfo.rank == response.urlComparison.importantOne.rank> background-color: #ffaaaa; </#if>">
	                			${urlinfo.rank}	                			
	                		</div>
	                	</td> 
	                	<td>
	                		<div style="overflow: hidden; white-space: nowrap; width: 300px; height: 43px;<#if urlinfo_index == 0> padding-top: 20px; </#if><#if response.urlComparison.importantOne?? && urlinfo.rank == response.urlComparison.importantOne.rank> background-color: #ffaaaa; </#if> ">	                			
	                			<a href="?query=${question.inputParameterMap["query"]?url}&collection=${question.inputParameterMap["collection"]?url}&profile=${question.profile?url}&optimiser_url=${urlinfo.liveUrl?url}&advanced=1"> 
		                			${urlinfo.title} 
	                			</a>
	                			
	                		</div>
	                	</td> 
	                	<#if urlinfo_index == 0> 
	                		<td rowspan="${response.urlComparison.urls?size?c}" id="barplot-cell">
	                		</td>
	                	</#if>
	                </tr>	
        	    </#list>
        
        </table>
   		<#if response.urlComparison.importantOne??> 
	    	<div <#if ! (response.urlComparison.importantOne.rank?number > 10)> style="display: none;" </#if> >
	    		<p>The document you selected was not in the top 10. It's rank and ranking scores were:</p>
		        <table>
		                <tr>
		                	<td style="width: 38px;">
		                		<div style="overflow: hidden; white-space: nowrap; height: 43px;padding-top: 20px;">
		                				${response.urlComparison.importantOne.rank}
		                		</div>
		                	</td> 
		                	<td>
		                		<div style="overflow: hidden; white-space: nowrap; width: 300px; height: 43px; padding-top: 20px;">
		                			<a href="${response.urlComparison.importantOne.liveUrl}"> ${response.urlComparison.importantOne.title} </a>
		                		</div>
		                	</td> 
		                	<td><div class="jqPlot" id="barplot-important" style="height:60px; width:435px;"></div></td>
		                </tr>	
				</table>
			</div>
		</#if>

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
							        		<li><b>${hint.longName}:</b> ${text}</li>
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
        <div style="clear: both;">
               <@content_optimiser_requery/>
        </div>
	</div>
</div>
	<div id="chartpseudotooltip" style="display: none;"></div>               
</body>

</html>
</#compress>