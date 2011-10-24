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
        	<h3 class="header">	Content Optimiser Advanced View</h3>
        	<img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36">
        	
        </div>
        
        <#if ! response??>
			<@content_optimiser_big_error/>
		<#elseif ! response.optimiserModel??>
			<@content_optimiser_big_error/>
		<#elseif response.resultPacket.queryAsProcessed == "">
		    <@content_optimiser_warnings/>
		<#else>
	        <@content_optimiser_warnings/>
	
			<@content_optimiser_summary/>
			<#if (response.optimiserModel.topResults?size > 0)>
				<div class="section">
					<h4>Top Ranking Breakdown</h4>
			           
			        <script type="text/javascript">
			        	var graphsDone = 0;
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
							$('#chartpseudotooltip').show();document
				            $('#chartpseudotooltip').css(cssObj);
				        }   
				        
				        function hideTip() {
				        	$('#chartpseudotooltip').fadeOut('slow');
				        }
			
			        	$(function() {
			        		$("#barplot-cell").append('<div class="jqPlot" id="barplot" style="height:' + (50 * ${response.optimiserModel.topResults?size?c}) + 'px; width:435px;"></div>');
			   
				        	<#list response.optimiserModel.hintsByName?keys as hintkey>
				        			<#assign hint = response.optimiserModel.hintsByName[hintkey] />
			        				var ${hint.name} = new Array();
			        				featureNames.push('${hint.longName}');
									<#if response.optimiserModel.selectedDocument?? > var important_${hint.name} = [[${hint.scores[response.optimiserModel.selectedDocument.rank?string]},1]]; </#if>
			       	    	</#list>
				        	
				        	
			
			        		<#list response.optimiserModel.topResults as urlinfo>
			        			<#list response.optimiserModel.hintsByName?keys as hintkey>
			       					${response.optimiserModel.hintsByName[hintkey].name}.push([${response.optimiserModel.hintsByName[hintkey].scores[urlinfo.rank?string]},${response.optimiserModel.topResults?size?c} + 1 - ${urlinfo.rank}]);
			        			</#list>
			        		</#list>
			      	
							var barplot = $.jqplot('barplot', [
				  			<#list response.optimiserModel.hintsByName?keys as hintkey>
				        			${response.optimiserModel.hintsByName[hintkey].name}
				        			<#if hintkey_has_next>,</#if>
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
			        					<#list response.optimiserModel.topResults as urlinfo>' ' <#if urlinfo_has_next>,</#if></#list>
						    	        ]
						        	},	
						        	xaxis: {min: 0, max: 
						        		<#list response.optimiserModel.hintsByWin as hint>
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
										
							<#if response.optimiserModel.selectedDocument??> 
								var barplotImportant = $.jqplot('barplot-important', [
								<#list response.optimiserModel.hintsByName?keys as hintkey>
				        			important_${response.optimiserModel.hintsByName[hintkey].name}
				        			<#if hintkey_has_next>,</#if>
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
							        		<#list response.optimiserModel.hintsByWin as hint>
								        			${hint.name}[0][0] +
							        		</#list> 0, numberTicks:5,  tickRenderer: $.jqplot.CanvasAxisTickRenderer}
							    	}
								});
								$('#barplot-important').bind('jqplotDataHighlight', showTip);
						    	$('#barplot-important').bind('jqplotDataUnhighlight', hideTip);
								
							</#if>
							
							<#assign idx = 0 />
							<#list response.optimiserModel.hintsByName?keys as hintkey>
				        			<#assign hint = response.optimiserModel.hintsByName[hintkey] />
				        			$("#legend").append('<span class="legend-block"><span class="legend-colour" style="background-color: '+barplot.series[${idx}].color+' ">&nbsp;</span> <span>${hint.longName}</span>');
				        			<#assign idx = idx +1/>
				        	</#list>
				        	
				        	
							<#assign idx = 0 />
							<#list response.optimiserModel.hintsByName?keys as hintkey>
				        		<#assign hint = response.optimiserModel.hintsByName[hintkey] />
				        		for(var i = 0; i < ${hint.name}.length;i++) {
				        			${hint.name}[i] = [${response.optimiserModel.topResults?size?c}+1-${hint.name}[i][1],${hint.name}[i][0]/${response.optimiserModel.weights[hint.name]}*100];
				        		}
				        		<#if response.optimiserModel.selectedDocument??> 
					        		var line_${hint.name} = [
					        		  <#list 0..response.optimiserModel.topResults?size as x>
					        		  	[${x},${hint.scores[response.optimiserModel.selectedDocument.rank?string]}/${response.optimiserModel.weights[hint.name]}*100],
					        		  	
					        		  </#list>
					        		  [${response.optimiserModel.topResults?size?c}+1, ${hint.scores[response.optimiserModel.selectedDocument.rank?string]}/${response.optimiserModel.weights[hint.name]}*100]
					        		];
					        	</#if>
					        	<#if response.optimiserModel.selectedDocument??> 
					        		var plot_${hint.name} = $.jqplot('plot-${hint.name}', [${hint.name},
					        			<#if response.optimiserModel.selectedDocument??> line_${hint.name} </#if>
					        		], {
					        			title: '${hint.longName}',
					    				axesDefaults: {       				 	
				       				 			labelRenderer: $.jqplot.CanvasAxisLabelRenderer, 
				       				 			tickRenderer: $.jqplot.CanvasAxisTickRenderer
										},       				 	
				       				 	axes:{
				       				 		xaxis:{
				       				 			ticks: [ <#list 0..response.optimiserModel.topResults?size as x> ${x},</#list> (${response.optimiserModel.topResults?size?c}+1) ],
				       				 			tickOptions:{formatString:'%d'}, 
				       				 			min:0, 
				       				 			max:${response.optimiserModel.topResults?size?c} +1,
				       				 			label: "Rank", 
				       				 			pad: 1
				       				 			},
				       				 		yaxis:{	label:'Score' } 
				       				 	},
				        				series:[
				            				{showLine:false, markerOptions:{style:'x'},color: barplot.series[${idx}].color},
				            				<#if response.optimiserModel.selectedDocument??>  {showLine:true, color:'#ff9999', showMarker:false}</#if>
						        		]
				    				});
				    				
				    			</#if>
				    			<#assign idx = idx+1/>
							</#list>
							
								graphsDone = 1;
							});
							
			        </script>
			
			        <table>
			                <tr><th>Rank</th><th>Title</th><th>Ranking caused by</th></tr>
			           		<#list response.optimiserModel.topResults as urlinfo>
				                <tr>
				                	<td>
				                		<div style="overflow: hidden; white-space: nowrap; height: 43px; <#if urlinfo_index == 0> padding-top: 20px; </#if> <#if response.optimiserModel.selectedDocument?? && urlinfo.rank == response.optimiserModel.selectedDocument.rank> background-color: #ffaaaa; </#if>">
				                			${urlinfo.rank}	                			
				                		</div>
				                	</td> 
				                	<td>
				                		<div style="overflow: hidden; white-space: nowrap; width: 300px; height: 43px;<#if urlinfo_index == 0> padding-top: 20px; </#if><#if response.optimiserModel.selectedDocument?? && urlinfo.rank == response.optimiserModel.selectedDocument.rank> background-color: #ffaaaa; </#if> ">	                			
				                			<a href="runOptimiser.html?query=${question.inputParameterMap["query"]?url}&amp;collection=${question.inputParameterMap["collection"]?url}&amp;profile=${question.profile?url}&amp;optimiser_url=${urlinfo.liveUrl?url}&amp;advanced=1"> 
					                			${urlinfo.title} 
				                			</a>
				                			
				                		</div>
				                	</td> 
				                	<#if urlinfo_index == 0> 
				                		<td rowspan="${response.optimiserModel.topResults?size?c}" id="barplot-cell">
				                		</td>
				                	</#if>
				                </tr>	
			        	    </#list>
			        
			        </table>
			    </div>
		   		<#if response.optimiserModel.selectedDocument??> 
			    	<div <#if ! (response.optimiserModel.selectedDocument.rank?number > 10)> style="display: none;" </#if> >
			    		<p>The document you selected was not in the top 10. It's rank and ranking scores were:</p>
				        <table>
				                <tr>
				                	<td style="width: 38px;">
				                		<div style="overflow: hidden; white-space: nowrap; height: 43px;padding-top: 20px;">
				                				${response.optimiserModel.selectedDocument.rank}
				                		</div>
				                	</td> 
				                	<td>
				                		<div style="overflow: hidden; white-space: nowrap; width: 300px; height: 43px; padding-top: 20px;">
				                			<a href="${response.optimiserModel.selectedDocument.liveUrl}"> ${response.optimiserModel.selectedDocument.title} </a>
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
				
				<#if response.optimiserModel.selectedDocument??>
					<div class="section">
						<p>The relative weights of the ranking features above are controlled by the administrator of your search system. If they appear to be incorrect, consider asking the search administrator to tune the ranking weightings.</p>
 
					</div>
					<#assign first=1/>
					<#assign suggestionsStartAt=1/>
					<#list response.optimiserModel.hintCollections as hc>
						
					    <div class="section" style="clear: both;">
					    	<#if first ==1 >
					    		<h4>Step-by-step Guide to Optimise Ranking</h4>
					    		<#assign first=0/>
					    	</#if> 
					    	<div style="float: left; width:420px;">
					    		<#if (hc.win <= 0) && hc.name != "content">
					    			<p class="tip">The content optimiser can't see an easy way to improve the ${hc.name} features</p>
					    		<#else>
							    	<h4 style="text-align: center;">Advice for improving ${hc.name} features</h4>
							    	<ol>
								    	<#list hc.hints as hint>
								    		<#if (hint.hintTexts?size > 0) && (hint.win > 0)>
								    			
										        	<#list hint.hintTexts as text>
										        		<li style="text-align: justify;" value=${suggestionsStartAt}>${text}</li>
										        		<#assign suggestionsStartAt = suggestionsStartAt+1/>
										        	</#list>
									        		
								       		</#if>
										</#list>
										<#if hc.name == "content">
									      	<li style="text-align: justify;" value=${suggestionsStartAt}>
									      	       		<#assign suggestionsStartAt = suggestionsStartAt+1/>
											 The most common words in the page are <span class="highlight">${response.optimiserModel.content.commonWords}</span>.
											 These words should be an indicator of the subject of the page. If the words don't accurately reflect the subject of the page, consider re-wording the page, or preventing sections of the page from being indexed by wrapping the section with <span style="display: inline-block">&lt;!--noindex--&gt;</span> and <span style="display: inline-block">&lt;!--endnoindex--&gt;</span> tags.
										 	</li>
							        	</#if>
									</ol>
								</#if>
							</div>   				
					    	<div style="float: right; width:350px;">
					    		<#assign shownHide = 0/>
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
							    			<#assign shownHide =1/>
							    		</#if>
							    		
					    				<div class="boring-${hc_index}">
					    			</#if>
							    	<div class="jqPlot" id="plot-${hint.name}" style="height:300px; width:350px;"></div>
						    		<p class="tip" style="padding-top: 0px; margin-top: 0px; padding-left: 70px;"><img src="${ContextPath}/content-optimiser/red-bar.png"/> Score of selected page</p>
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
		    </#if>
		    <@content_optimiser_stemming/>
	     	<div style="clear: both;" class="section">
	        		<h4>Optimise Another Page</h4>
	               <@content_optimiser_requery/>
	        </div>
		</div>
	</#if>

	<div id="chartpseudotooltip" style="display: none;"></div>               
</body>

</html>
</#compress>