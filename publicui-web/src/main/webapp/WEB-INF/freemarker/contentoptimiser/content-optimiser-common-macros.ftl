<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>

<#macro content_optimiser_requery>
	<form method="GET" action="runOptimiser.html">
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

<#macro content_optimiser_stemming>
	<#if response.optimiserModel.content.termsToStemEquivs?keys?size != 0>
		<div class="section" style="clear: both;">
			<h4>Note</h4>
			<p>Stemming is enabled in the query processor options, causing some of the query terms to match similar words. This means that:
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
		</div>			
	</#if>
</#macro>

<#macro content_optimiser_summary>
        <div class="summary">
      			<h4>Ranking Summary:</h4>
        		 
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
							<div style="font-size: 30px;" class="rank">${response.optimiserModel.selectedDocument.rank}</div>
						</div> 
						<p>The selected page, <a href="${response.optimiserModel.selectedDocument.liveUrl}"><@s.boldicize>${response.optimiserModel.selectedDocument.title}</@s.boldicize></a>, 
						contains <span class="highlight">${response.optimiserModel.content.totalWords?string.number}</span> total words, <span class="highlight">${response.optimiserModel.content.uniqueWords?string.number}</span> of which are unique.
						</p>
						<p>There are <span>${response.resultPacket.resultsSummary.fullyMatching?string.number}</span> fully matching pages 
        				for the query &quot;<b><@s.QueryClean/></b>&quot;:</p>
					
					 	<p>You can also view Funnelback's <a href="${response.optimiserModel.selectedDocument.cacheUrl}">cached copy</a>,
					 	view the <a href="${ContextPath}/anchors.html?collection=${response.optimiserModel.selectedDocument.collection}&docnum=${response.optimiserModel.selectedDocument.docNum}">link information</a> for the page;
					 	or view the 
						<a href="${ContextPath}/search.html?query=${question.inputParameterMap["query"]?url}&collection=${question.inputParameterMap["collection"]?url}&profile=${question.profile?url}">result page</a> from this query</p>
			        <#else>
						<p>There are <span>${response.resultPacket.resultsSummary.fullyMatching?string.number}</span> fully matching pages 
    		    		for the query &quot;<b><@s.QueryClean/></b>&quot;:</p>
			        	<#if (question.inputParameterMap["optimiser_url"]?? && question.inputParameterMap["optimiser_url"] != "") >
			        		<p>The selected page (<strong style="word-break: break-all;">${question.inputParameterMap["optimiser_url"]?html}</strong>) was not found in the results.</p>
			        	</#if>
			        	<p>
			        	You can view the <a href="${ContextPath}/search.html?query=${question.inputParameterMap["query"]?url}&amp;collection=${question.inputParameterMap["collection"]?url}&amp;profile=${question.profile?url}">result page</a> from this search</p>
		        	</#if>
	        <#else>
				<p>
					There are <span>${response.resultPacket.resultsSummary.fullyMatching?string.number}</span> fully matching pages 
	    			for the query &quot;<b><@s.QueryClean/></b>&quot;:
	    		</p>
	        	<p>You can view the <a href="${ContextPath}/search.html?query=${question.inputParameterMap["query"]?url}&amp;collection=${question.inputParameterMap["collection"]?url}&amp;profile=${question.profile?url}">result page</a> from this query</p>
        	</#if>
        	<#if nonAdminLink?? && onAdminPort??>
        		You can use <a href="${nonAdminLink}">this link</a> to share this page with non-administrators.
        	<#elseif onAdminPort??>
    			Non administrators cannot see this page.
        	</#if>
      			
        </div>   
</#macro>
