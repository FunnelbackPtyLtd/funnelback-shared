<#ftl encoding="utf-8" />
<#setting number_format="computer">
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#include "content-optimiser-common-macros.ftl"/>
<#compress>
<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8" />
		<link rel="stylesheet" type="text/css" href="${ContextPath}/content-optimiser/optimiser.css"/>
		<title>Funnelback Content Optimiser</title>
		<script type="text/javascript" src="/search/js/jquery/jquery-1.4.2.min.js"></script>
		<script type="text/javascript" src="/search/js/jquery/jquery-ui-1.8.14.dialog-only.min.js"></script>
	</head>
	
	<body>
		
	    <div id="content-optimiser-pane">
	        <div style="margin-bottom: 15px;">
	        	<h3 class="header">	Content Optimiser</h3>
	        	<img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36">
	        </div>
	
		    <#if ! response??>
				<@content_optimiser_big_error/>
			<#elseif ! response.optimiserModel?? || response.resultPacket.error??>
				<@content_optimiser_big_error/>
			<#elseif response.resultPacket.queryAsProcessed == "">
			    <@content_optimiser_warnings/>
			<#else>
				
		        <@content_optimiser_warnings/>
		               
				<@content_optimiser_summary/>
		           
				<#if response.optimiserModel.selectedDocument??>
					<div class="section">
						<p>Consider the following steps to optimise the ranking of this page:</p> 
					</div>
						<ol>
					    	<#list response.optimiserModel.hintsByWin as hint>
					    		<#if (hint.hintTexts?size > 0) && (hint.win > 0)>
							       	<#list hint.hintTexts as text>
						        		<li>${text}</li>
						        	</#list>
					      		</#if>
							</#list>
					      	<li>
							 The most common words in the page are <span class="highlight">${response.optimiserModel.content.commonWords}</span>.
							 These words should be an indicator of the subject of the page. If the words don't accurately reflect the subject of the page, consider re-wording the page, or preventing sections of the page from being indexed by wrapping the section with <span style="display: inline-block">&lt;!--noindex--&gt;</span> and <span style="display: inline-block">&lt;!--endnoindex--&gt;</span> tags.
						 	</li>
						</ol>
				</#if>
		
			    <div style="clear: both;" class="section">
			    	<p style="margin-bottom: 2px;">Optimise another page:</p>
			    	<@content_optimiser_requery/>
			 	</div>
			</#if>
		</div>
		<@content_optimiser_loading/>	
	</body>
	
</html>
</#compress>