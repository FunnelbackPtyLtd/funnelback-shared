<#ftl encoding="utf-8" />
<#setting number_format="computer">
<#import "/share/freemarker/funnelback_legacy.ftl" as s/>
<#include "content-optimiser-common-macros.ftl"/>
<#compress>
	<!DOCTYPE html>
	<html lang="en">
	<head>
		<meta charset="utf-8" />
		<link rel="stylesheet" type="text/css" href="${ContextPath}/content-optimiser/optimiser.css"/>
		<title>Funnelback Content Optimiser</title>
	</head>
	
	<body>
		
	    <div id="content-optimiser-pane">
	        <div style="margin-bottom: 15px;">
	        	<h3 class="header">	Content Optimiser</h3>
	        	<img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36">
	        </div>
	
	        <@content_optimiser_warnings/>
	               
			<@content_optimiser_summary/>
	           
			<#if response.urlComparison.importantOne??>
				<div class="section">
					<p>Here is a breakdown of the best ways to improve the ranking of the selected page. Improvement suggestions listed first will be the most effective. </p> 
				</div>
					<ul>
				    	<#list response.urlComparison.hintsByWin as hint>
				    		<#if (hint.hintTexts?size > 0) && (hint.win > 0)>
						       	<#list hint.hintTexts as text>
					        		<li><b>${hint.longName}:</b> ${text}</li>
					        	</#list>
				      		</#if>
						</#list>
					</ul>
			</#if>
	
		    <div style="clear: both; ">
		    	<@content_optimiser_requery/>
		 	</div>
		</div>
		
	</body>
	
	</html>
</#compress>