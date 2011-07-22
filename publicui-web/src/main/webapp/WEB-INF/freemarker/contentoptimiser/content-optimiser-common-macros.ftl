<#ftl encoding="utf-8" />
<#import "/share/freemarker/funnelback_legacy.ftl" as s/>

<#macro content_optimiser_requery>
	<form method="GET" action="optimise.html">
		<p style="margin-bottom: 2px;">Optimise another document or query:</p>
		
		<div class="requery_line">
			<label>Query</label> <input type="text"  class="query"  name="query" value="<@s.QueryClean/>"/>
		</div>
		<div style="position: absolute; left: 730px; margin-top: -12px; z-index: 100;">
			<input style="height: 40px;" type="submit" value="optimise"/>					
		</div>	
		
		<div class="requery_line">
			<label>URL</label> <input type="text" class="optimiser_url" name="optimiser_url" value="<#if (response.urlComparison.importantOne??)>${response.urlComparison.importantOne.liveUrl}</#if>"/>
		</div>
		<input type="hidden" name="collection" value="${question.inputParameterMap["collection"]}"/>
		<input type="hidden" name="profile" value="${question.profile}"/>
		<#if question.inputParameterMap["advanced"]??>
			<input type="hidden" name="advanced" value="1"/>
		</#if>
	</form>
</#macro>

<#macro content_optimiser_warnings>
        <#if (response.urlComparison.messages?size > 0)>
			<div class="messages">
				<ul>
				 	<#list response.urlComparison.messages as message>
				 		<li>${message}</li>	
				 	</#list>
				</ul>
			</div>        
        </#if>
</#macro>

<#macro content_optimiser_summary>
        <div class="summary">
        	<#if (response.urlComparison.urls?size > 0)>
        		<p>There are ${response.resultPacket.resultsSummary.fullyMatching?string.number} fully matching documents 
        		for the query &quot;<b><@s.QueryClean/></b>&quot;. 
        		The top document (rank 1) is titled <a href="${response.urlComparison.urls[0].liveUrl}">${response.urlComparison.urls[0].title}</a>.
	        	<#if (response.urlComparison.importantOne??)>
					<p>The selected document (<strong>${response.urlComparison.importantOne.liveUrl}</strong>):
						<ul>
							<li>is ranked <span class="highlight">${response.urlComparison.importantOne.rank}</span> in the results </li>
							<li>is titled <a href="${response.urlComparison.importantOne.liveUrl}">${response.urlComparison.importantOne.title}</a>.</li>
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
				 	<p>Funnelback's <a href="${response.urlComparison.importantOne.cacheUrl}">cached copy of the document is available</a>, and you can also view the <a href="${ContextPath}/anchors.html?collection=${response.urlComparison.importantOne.collection}&docnum=${response.urlComparison.importantOne.docNum}">anchors information for the document.</a></p>
	        	</#if>
	        	
	        	 
        	</#if>
        </div>   
</#macro>
