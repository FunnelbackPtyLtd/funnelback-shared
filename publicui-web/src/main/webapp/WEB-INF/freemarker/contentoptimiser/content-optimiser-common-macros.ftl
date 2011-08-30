<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>

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
		<#if Request.advanced??><input type="hidden" name="advanced" value="1"/></#if>
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
		<img style="top: 25px; position: relative; left: 50%; margin-left: -20px;" src="/search/optimiser-loading.gif" alt="loading"/>
	</div>
 	<script>
 		$(function() {
 			$( "#dialog-modal" ).dialog({
				title: "Examining the ranking. This will take a few seconds",
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

<#macro content_optimiser_big_error>
	<div class="messages">
		<ul>
			<#if response?? && response.resultPacket?? && response.resultPacket.error??><li>${response.resultPacket.error.userMsg}</li></#if>
			<li>An error has occured in the content optimiser. 
			Refer to the logs for more information</li>
		</ul>			
	</div>
</#macro>


<#macro content_optimiser_warnings>
		<#if response??>
	        <#if (response.optimiserModel.messages?size > 0)>
				<div class="messages">
					<ul>
					 	<#list response.optimiserModel.messages as message>
					 		<li>${message}</li>	
					 	</#list>
					 	<#if response.resultPacket.queryAsProcessed == "">
					 		<li>No query terms entered</li>
					 	</#if>
					</ul>
				</div>        
	        </#if>
	    </#if>
</#macro>

<#macro content_optimiser_summary>
        <div class="summary">
      			<p>Ranking summary:</p>
        		 
				<#if (response.optimiserModel.topResults?size> 0) && response.resultPacket.queryAsProcessed != "">        				    	
		        	<#if (response.optimiserModel.selectedDocument??)>
						<div style="float: left; padding-left: 20px; padding-right: 20px; padding-top: 10px; padding-bottom: 10px; border: 1px solid; margin-right: 10px; text-align: center; 
						<#if response.optimiserModel.selectedDocument.rank?number == 1>
							background-color: #aaffaa; 
						<#elseif response.optimiserModel.selectedDocument.rank?number < 11>
							background-color: #ffffaa;
						<#else>
							background-color: #ffaaaa;
						</#if>
						">
							<small>Rank</small>
							<div style="font-size: 30px;">${response.optimiserModel.selectedDocument.rank}</div>
						</div> 
						<p>There are ${response.resultPacket.resultsSummary.fullyMatching?string.number} fully matching pages 
        		for the query &quot;<b><@s.QueryClean/></b>&quot;:</p>
						<p>The selected page <a href="${response.optimiserModel.selectedDocument.liveUrl}"><@s.boldicize>${response.optimiserModel.selectedDocument.title}</@s.boldicize></a> 
						contains <span class="highlight">${response.optimiserModel.content.totalWords?string.number}</span> total words, <span class="highlight">${response.optimiserModel.content.uniqueWords?string.number}</span> of which are unique.
						</p>
						<#if response.optimiserModel.content.termsToStemEquivs?keys?size != 0>
							<p>Stemming is turned on in the query processor options, causing some of the query terms to match similar words. This means that:
							<ul>
								<#list response.optimiserModel.content.termsToStemEquivs?keys as key>
									<li>The terms [
											<#list response.optimiserModel.content.termsToStemEquivs[key] as word>
												<span class="highlight">${word}</span> 
											</#list> 
											]
										are counted as matching <span class="highlight">${key}</span>
									</li>
								</#list>
							</ul>
							</p>
							
						</#if>
					 	<p><a href="${response.optimiserModel.selectedDocument.cacheUrl}">cached copy</a> <a href="${ContextPath}/anchors.html?collection=${response.optimiserModel.selectedDocument.collection}&docnum=${response.optimiserModel.selectedDocument.docNum}">link information</a>
					 
<a href="${ContextPath}/search.html?query=${question.inputParameterMap["query"]?url}&collection=${question.inputParameterMap["collection"]?url}&profile=${question.profile?url}">result page</a></p>
			        <#else>
						<p>There are ${response.resultPacket.resultsSummary.fullyMatching?string.number} fully matching pages 
    		    		for the query &quot;<b><@s.QueryClean/></b>&quot;:</p>
			        	<#if (question.inputParameterMap["optimiser_url"]?? && question.inputParameterMap["optimiser_url"] != "") >
			        		<p>The selected page (<strong style="word-break: break-all;">${question.inputParameterMap["optimiser_url"]?html}</strong>) was not found in the results.</p>
			        	</#if>
			        	<p><a href="${ContextPath}/search.html?query=${question.inputParameterMap["query"]?url}&collection=${question.inputParameterMap["collection"]?url}&profile=${question.profile?url}">result page</a></p>
		        	</#if>
	        <#else>
				<p>
					There are ${response.resultPacket.resultsSummary.fullyMatching?string.number} fully matching pages 
	    			for the query &quot;<b><@s.QueryClean/></b>&quot;:
	    		</p>
	        	<p><a href="${ContextPath}/search.html?query=${question.inputParameterMap["query"]?url}&collection=${question.inputParameterMap["collection"]?url}&profile=${question.profile?url}">result page</a></p>
        	</#if>
        	<#if nonAdminLink?? && onAdminPort??>
        		You can use <a href="${nonAdminLink}">this link</a> to share this page with non-administrators.
        	<#elseif onAdminPort??>
    			Non administrators cannot see this page.
        	</#if>
      			
        </div>   
</#macro>
