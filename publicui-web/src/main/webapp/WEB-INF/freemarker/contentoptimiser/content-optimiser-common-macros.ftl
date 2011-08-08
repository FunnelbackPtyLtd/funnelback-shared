<#ftl encoding="utf-8" />
<#import "/web/templates/publicui/funnelback_classic.ftl" as s/>

<#macro content_optimiser_requery>
	<form method="GET" action="optimise.html">
		
		
		<div class="requery_line">
			<label>Query</label> <input type="text"  tabindex="1" class="query"  name="query" value="<@s.QueryClean/>"/>
		</div>
		<div style="position: absolute; left: 730px; margin-top: -16px; z-index: 100;">
			<input tabindex="3" style="height: 45px;" type="submit" value="optimise"/>					
		</div>	
		
		<div class="requery_line">
			<label>URL</label> <input tabindex="2" type="text" class="optimiser_url" name="optimiser_url" value="<#if question??>${question.inputParameterMap["optimiser_url"]?html}</#if>"/>
		</div>
		<input type="hidden" name="collection" value="<#if question??>${question.inputParameterMap["collection"]!}<#else>${collection}</#if>"/>
		<input type="hidden" name="profile" value="<#if question??>${question.profile}<#else>_default</#if>"/>
		<#if question??>
			<#if question.inputParameterMap["advanced"]??>
				<input type="hidden" name="advanced" value="1"/>
			</#if>
		</#if>
	</form>
</#macro>

<#macro content_optimiser_loading>
	<div id="dialog-modal" style="height:200px;">
		<img style="float: right; top: 25px; position: relative;" src="/search/optimiser-loading.gif" alt="loading">
		<p>Please be patient - this can take some time.</p>
	</div>
 	<script>
 		$(function() {
 			$( "#dialog-modal" ).dialog({
				title: "Examining the ranking....",
				modal: true,
				closeOnEscape: false,
				resizeable: false,
				autoOpen: false
			});
			$("form").submit(function() {
				$( "#dialog-modal" ).dialog('open');
			});
		});		
 	</script>
</#macro>

<#macro content_optimiser_warnings>
		<#if response??>
	        <#if (response.urlComparison.messages?size > 0)>
				<div class="messages">
					<ul>
					 	<#list response.urlComparison.messages as message>
					 		<li>${message}</li>	
					 	</#list>
					</ul>
				</div>        
	        </#if>
	    </#if>
</#macro>

<#macro content_optimiser_summary>
        <div class="summary">
        	<#if (response.urlComparison.urls?size > 0)>
        		<p>There are ${response.resultPacket.resultsSummary.fullyMatching?string.number} fully matching documents 
        		for the query &quot;<b><@s.QueryClean/></b>&quot;. 
        		The top document (rank 1) is titled <a href="${response.urlComparison.urls[0].liveUrl}"><@s.boldicize>${response.urlComparison.urls[0].title}</@s.boldicize></a>.
	        	<#if (response.urlComparison.importantOne??)>
					<p>The selected document (<strong style="word-break: break-all;">${response.urlComparison.importantOne.liveUrl}</strong>):
						<ul>
							<li>is ranked <span class="highlight">${response.urlComparison.importantOne.rank}</span> in the results </li>
							<li>is titled <a href="${response.urlComparison.importantOne.liveUrl}"><@s.boldicize>${response.urlComparison.importantOne.title}</@s.boldicize></a>.</li>
							<li>contains <span class="highlight">${response.urlComparison.content.totalWords?string.number}</span> total words, <span class="highlight">${response.urlComparison.content.uniqueWords?string.number}</span> of which are unique.</li>
						</ul> 
					</p>
					<#if response.urlComparison.content.termsToStemEquivs?keys?size != 0>
						<p>Stemming is turned on in the query processor options, causing some of the query terms to match similar words. This means that:
						<ul>
							<#list response.urlComparison.content.termsToStemEquivs?keys as key>
								<li>The terms [
										<#list response.urlComparison.content.termsToStemEquivs[key] as word>
											<span class="highlight">${word}</span> 
										</#list> 
										]
									are counted as matching <span class="highlight">${key}</span>
								</li>
							</#list>
						</ul>
						</p>
						
					</#if>
					 <p>
						 The most common words in the document are <span class="highlight">${response.urlComparison.content.commonWords}</span>. 
						 <ul>
						 	<li>These words should be an indicator of the subject of the document. If the words don't accurately reflect the subject of the document, consider re-wording the document, or preventing sections of the document from being indexed by wrapping the section with <span style="display: inline-block">&lt;!--noindex--&gt;</span> and <span style="display: inline-block">&lt;!--endnoindex--&gt;</span> tags</li>
						 </ul> 
					 </p>
				 	<p>Funnelback's <a href="${response.urlComparison.importantOne.cacheUrl}">cached copy of the document is available</a>, and you can also view the <a href="${ContextPath}/anchors.html?collection=${response.urlComparison.importantOne.collection}&docnum=${response.urlComparison.importantOne.docNum}">anchors information for the document.</a>
				 	You can also <a href="${ContextPath}/search.html?query=${question.inputParameterMap["query"]?url}&collection=${question.inputParameterMap["collection"]?url}&profile=${question.profile?url}">view the result page from this search</a>.</p>
		        <#else>
		        	<#if (question.inputParameterMap["optimiser_url"]??)>
		        		<p>The selected document (<strong style="word-break: break-all;">${question.inputParameterMap["optimiser_url"]?html}</strong>) was not found in the results.</p>
		        	</#if>
		        	<p>You can <a href="${ContextPath}/search.html?query=${question.inputParameterMap["query"]?url}&collection=${question.inputParameterMap["collection"]?url}&profile=${question.profile?url}">view the result page from this search</a>.</p>
	        	</#if>
	        
	        </#if>
      			
        </div>   
</#macro>
