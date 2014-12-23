<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>
<!--BEGINDOCUMENTS-->

<!-- Collection: <@s.cfg>service_name</@s.cfg> | Attributes last updated: ${response.resultPacket.details.collectionUpdated?datetime} -->
<div class="tab-header clearfix">
	<p class="pull-left">        
	<#if response.resultPacket.resultsSummary.totalMatching == 0>
		<strong class="fb-total-matching fb-result-count">0</strong> search results for <strong><@s.QueryClean /></strong>
	</#if>
	<#if response.resultPacket.resultsSummary.totalMatching != 0>
		Displaying <strong class="fb-result-count" id="fb-page-start">${response.resultPacket.resultsSummary.currStart}</strong> -
		<strong class="fb-result-count fb-page-end">${response.resultPacket.resultsSummary.currEnd}</strong> of
		<strong class="fb-result-count fb-total-matching">${response.resultPacket.resultsSummary.totalMatching?string.number}</strong>
		search results for <strong>${queryToReport}</strong>
	</#if>
	<#if response.resultPacket.resultsSummary.partiallyMatching != 0>
		where
		<span class="fb-result-count" id="fb-fully-matching">
			${response.resultPacket.resultsSummary.fullyMatching?string.number}
		</span>
		match all words and
		<span class="fb-result-count" id="fb-partially-matching">
			${response.resultPacket.resultsSummary.partiallyMatching?string.number}
		</span>
		match some words.
	</#if>
	</p>
<#if response.resultPacket.resultsSummary.totalMatching != 0>       
<!-- CSV DOWNLOAD -->
<a data-toggle="tooltip" data-placement="left" title="Download a CSV File of these results. Results limited to the first 10,000 only." class="btn btn-sm btn-primary pull-left btn-upload" href="content-auditor.html?${QueryString}&amp;type=csv_export&amp;num_ranks=10000"><span class="fa fa-lg fa-angle-double-down"></span> Export CSV Data</a>          
</#if>

<div class="form-field select field-sort pull-right" data-url="?${QueryString}">     
      <div class="form-inline"><small>Sort by &nbsp;</small> <@s.Select class="form-control input-sm" name="sort" id="sort" options=["=Relevance", "date=Date (Newest First)", "adate=Date (Oldest First)", "url=URL", "title=Title (A-Z)", "dtitle=Title (Z-A)"] /></div>
</div>


</div>

    <#-- applied facets block -->
    <#if question.selectedCategoryValues?has_content> 
        <div class="drill-filters"><span class="fa fa-filter"></span>
        <@AppliedFacets class="btn btn-xs btn-warning" group=true urlHash="#collection-${currentCollection}-tab-2" />
        <@ClearFacetsLink  class="btn btn-xs btn-danger" urlHash="#collection-${currentCollection}-tab-2" />
        </div>
    </#if>

    <!-- START RESULTS -->
    <#-- Hide the table if there are no results -->
    <table id="report-details" class="table table-hover table-responsive table-striped table-row-clickable <#if response.resultPacket.resultsSummary.totalMatching == 0>hidden</#if>">

    <thead>
        <tr>
            <#assign url = "content-auditor.html?" + changeParam(QueryString, "sort",'dtitle') />
			<th scope="col"><span><a href="${url}#collection-${currentCollection}-tab-2">Page </a> </span></th>
			<th scope="col"><span class="sr-only">Actions</span></th> 
            <#list response.customData.displayMetadata?values as value>
                <#assign heading = value?replace("^\\d*\\.","","r")>
                 <#assign url = "content-auditor.html?" + changeParam(QueryString, "sort", heading)?replace(' ','_')?lower_case />
                <th scope="col"><a href ="${url}#collection-${currentCollection}-tab-2">${heading?html}</a></th>
            </#list>
        </tr>
    </thead>
    <#-- Unnecessasary
    <tfoot class="hidden">
        <tr>
            <th scope="col">Page</th>
			<th scope="col"><span class="sr-only">Actions</span></th> 
            <#list response.customData.displayMetadata?values as value>
                Remove the sorting prefix on the heading 
                <#assign heading = value?replace("^\\d*\\.","","r")>
                <th scope="col">${heading?html}</th>
            </#list>
        </tr>
    </tfoot>
    -->
    <tbody>
        <!-- EACH RESULT -->
        <@s.Results>
            <#if s.result.class.simpleName == "TierBar">
                <#-- A tier bar -->
                <#if s.result.matched != s.result.outOf>
                    <tr>
                        <td colspan="11">Search results that match ${s.result.matched} of ${s.result.outOf} words</td>
                    </tr>
                </#if>
            <#else>
            <tr>
                
                <!-- PAGE and URL -->
				<td>
				
					<div class="pull-left">
					<a class="clickable-link" target="_blank" href="${s.result.liveUrl?html}" title="${s.result.title}"><strong><@s.Truncate 150>${s.result.title}</@s.Truncate></strong></a> 
						<#if s.result.liveUrl??>
						<!-- SITE (Z) -->
						<br><span class="text-muted" href="#add link here">${s.result.liveUrl?html?replace("http://www.canberra.edu.au","")}</span><#else> &nbsp;
						</#if> 


                        
					
					<!--<div style="clear:both; margin-left:45px" class="pull-left">
					
						<form action="${httpRequest.requestURL}/../../content-optimiser/optimise.html">
							<input type="hidden" name="optimiser_url" value="${s.result.liveUrl?html?replace("http://","")}" />
							<input type="hidden" name="collection" value="business-gov-internet" />
							<input type="hidden" name="profile" value="_default" />
							<input type="hidden" name="optimiser_ts" value="1401176844554" />
							<label for="query"><strong>Content optimiser</strong></label>  
							<div class="input-group">
								<input type="text" name="query" value="" class="form-control" />
								<span class="input-group-btn">
									<button type="submit" class="btn btn-primary">Run</button>
								</span>
							</div>  
						</form>
						
					</div>-->
				   </td>
				
				
				<!-- ACTIONS -->
               
                  <td class="table-hide">
				  	
					
					<a class="open-anchors pass" target="_blank" data-modal="overlay" href="/s/anchors.html?collection=${question.inputParameterMap["collection"]?url}&docnum=${s.result.docNum?c}&ajax=1" data-toggle="tooltip" data-placement="bottom" title="Analyse Anchor Tags">
					<span class="fa-stack fa-xs">
    					<i class="fa fa-square fa-stack-2x"></i>
    					<i class="fa fa-anchor fa-stack-1x fa-inverse"></i>
					</span>
					</a>
					
					<a class="open-content-optimiser pass" target="_blank" href="/s/content-optimiser.html?collection=${question.inputParameterMap["collection"]?url}&amp;profile=${question.inputParameterMap["profile"]!?url}&amp;optimiser_url=${s.result.liveUrl?replace("http://","")?url}&amp;query=${response.resultPacket.queryAsProcessed?url}" data-toggle="tooltip" data-placement="bottom" title="Optimise with Content Optimiser">
					<span class="fa-stack fa-xs">
    					<i class="fa fa-square fa-stack-2x"></i>
    					<i class="fa fa-wrench fa-stack-1x fa-inverse"></i>
					</span>
					</a>
					
                    <a class="open-wcag pass" target="_blank" href="/search/admin/fareporter/doc-check?url=${s.result.liveUrl?url}" data-toggle="tooltip" data-placement="bottom" title="Check Content Accessibility with WCAG Auditor">
					
                    <span class="fa-stack fa-xs">
                        <i class="fa fa-square fa-stack-2x"></i>
                        <i class="fa fa-wheelchair fa-stack-1x fa-inverse"></i>
                    </span>
                    
					</a>
					
				
				</td>	
				
                <#list response.customData.displayMetadata?keys as key>
                    <td>
                    <#if key == "d">
                        <#-- Special-case date -->
                        <#if s.result.date??>
                           ${s.result.date?date?string("dd MMM, yyyy")?replace(" ", "&nbsp;")}                   
                        <#else>
                            (No Value)
                        </#if>
                    <#elseif key == "f">
                        <#-- Special-case format -->
                        <#if s.result.fileType??>
                            ${s.result.fileType}                    
                        <#else>
                            &nbsp;
                        </#if>
                    <#else>
                        <#if s.result.metaData[key]??>
                            <#if (s.result.metaData[key]?split("|")?size > 1)>
                                <ul>
                                    <#list s.result.metaData[key]?split("|") as value>
                                        <#if (value?length > 0)><li>${value?html}</li></#if>
                                    </#list>  
                                </ul>
                            <#else>
                                ${s.result.metaData[key]?html}
                            </#if>
                        <#else>
                            (No Value)
                        </#if>
                    </#if>
                    </td>
                </#list>

            </tr>
            </#if>
        </@s.Results>                
    </tbody>
</table>
<div class="tab-footer clearfix">



<!-- NO RESULTS -->
<#if response.resultPacket.resultsSummary.totalMatching == 0>
    <p id="fb-no-results">Your search for <strong>${question.inputParameterMap["query"]!?html}</strong> did not return any results. <span>Please ensure that you:</span>
        <ul>
            <li>are <strong>not</strong> using any advanced search operators like: <strong>+ - | "</strong> etc.</li> 
            <li>expect this document to exist within the <em><strong><@s.cfg>service_name</@s.cfg></strong></em><@s.IfDefCGI name="scope"> and within <em><@s.Truncate length=80>${question.inputParameterMap["scope"]!?html}</@s.Truncate></em></@s.IfDefCGI></li>
            <li>have permission to see any documents that may match your query</li>
        </ul>
    </p> 
</#if>

<!-- RESULTS SUMMARY -->
       
<#-- Display nothing - no need to display 0 results twice.
    <#if response.resultPacket.resultsSummary.totalMatching == 0>
        
          <strong class="fb-result-count" id="fb-total-matching">0</strong> search results for <strong><@s.QueryClean /></strong>
        
    </#if>
    <#if response.resultPacket.resultsSummary.totalMatching != 0>
        <strong class="fb-result-count" id="fb-page-start">${response.resultPacket.resultsSummary.currStart}</strong> -
        <strong class="fb-result-count" id="fb-page-end">${response.resultPacket.resultsSummary.currEnd}</strong> of
        <strong class="fb-result-count" id="fb-total-matching">${response.resultPacket.resultsSummary.totalMatching?string.number}</strong>
        search results for <strong><@s.QueryClean /></strong>
    </#if>

    <#if response.resultPacket.resultsSummary.partiallyMatching != 0>
        where
        <span class="fb-result-count" id="fb-fully-matching">
            ${response.resultPacket.resultsSummary.fullyMatching?string.number}
        </span>
        match all words and
        <span class="fb-result-count" id="fb-partially-matching">
            ${response.resultPacket.resultsSummary.partiallyMatching?string.number}
        </span>
        match some words.
    </#if>


</p>

-->
<!-- PAGINATION -->
<#if response.resultPacket.resultsSummary.totalMatching != 0>

<div>
  <ul class="pagination">
    <@fb.Prev><li><a href="${fb.prevUrl?replace('&form=documents','')?html}#collection-${currentCollection}-tab-2">Prev</a></li></@fb.Prev>
    <@fb.Page>
    <li <#if fb.pageCurrent> class="active"</#if>><a href="${fb.pageUrl?replace('&form=documents','')?html}#collection-${currentCollection}-tab-2">${fb.pageNumber}</a></li>
    </@fb.Page>
    <@fb.Next><li><a href="${fb.nextUrl?replace('&form=documents','')?html}#collection-${currentCollection}-tab-2">Next</a></li></@fb.Next>
  </ul>
</div>

</div>
</#if>
    
<!--ENDDOCUMENTS-->
</@s.AfterSearchOnly>