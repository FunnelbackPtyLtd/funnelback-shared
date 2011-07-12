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
		.highlight {
			font-weight: bold;
		}
			
		.section {
			width: 780px; border-top: 1px solid; 
			margin: 10px;
		}
	</style>
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
        		<p>There are ${response.resultPacket.resultsSummary.fullyMatching?string.number} fully matching documents for the query &quot;<b><@s.QueryClean/></b>&quot;. The top document (rank 1) is titled <a href="${response.urlComparison.urls[0].liveUrl}">${response.urlComparison.urls[0].title}</a>.
	        	<#if (response.urlComparison.importantOne??)>
					<p>The selected document (<strong>${response.urlComparison.importantOne.displayUrl}</strong>):
						<ul>
							<li>is ranked <span class="highlight">${response.urlComparison.importantOne.rank}</span> in the results </li>
							<li>is titled <a href="${response.urlComparison.importantOne.liveUrl}">${response.urlComparison.importantOne.title}</a>.</li>
							<li>contains <span class="highlight">${response.urlComparison.content.totalWords?string.number}</span> total words, <span class="highlight">${response.urlComparison.content.uniqueWords?string.number}</span> of which are unique.</li>
						</ul> 
					 Funnelback's cached copy of the document is available <a href="${response.urlComparison.importantOne.cacheUrl}">here</a>.</p>
					 <p>The most common words in the document are <span class="highlight">${response.urlComparison.content.commonWords}</span>. 
					 <ul>
					 	<li>These words should be an indicator of the subject of the document. If the words don't accurately reflect the subject of the document, consider re-wording the document, or preventing sections of the document from being indexed by wrapping the section with <span style="display: inline-block">&lt;!--noindex--&gt;</span> and <span style="display: inline-block">&lt;!--endnoindex--&gt;</span> tags</li></ul> </p>
	        	</#if> 
        	</#if>
        </div>   
           
		<#if response.urlComparison.importantOne??>
			<div class="section">
				<p>Here is a breakdown of the best ways to improve the ranking of the selected page. Improvement suggestions listed first will be the most effective. </p> 
			</div>
				<ul>
			    	<#list response.urlComparison.hintsByWin as hint>
			    		<#if (hint.hintTexts?size > 0) && (hint.win > 0)>
					       	<#list hint.hintTexts as text>
				        		<li><b>${hint.name}:</b> ${text}</li>
				        	</#list>
			      		</#if>
					</#list>
				</ul>
		</#if>
	    <div style="clear: both;">&nbsp;</div>
	</div>
	
</body>

</html>
