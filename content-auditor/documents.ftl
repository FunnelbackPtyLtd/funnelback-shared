<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>
<!--BEGINDOCUMENTS-->

<div class="tab-header clearfix">
	<p class="pull-left">        
	<#if response.resultPacket.resultsSummary.totalMatching != 0>
		Displaying <strong class="fb-result-count" id="fb-page-start">${response.resultPacket.resultsSummary.currStart}</strong> -
		<strong class="fb-result-count fb-page-end">${response.resultPacket.resultsSummary.currEnd}</strong> of
		<strong class="fb-result-count fb-total-matching">${(response.resultPacket.resultsSummary.totalMatching - response.resultPacket.resultsSummary.collapsed)?string.number}</strong>
		search results
	</#if>
	</p>
<#if response.resultPacket.resultsSummary.totalMatching != 0>       
<!-- CSV DOWNLOAD -->
<a data-toggle="tooltip" data-placement="left" title="Download a CSV File of these results. Results limited to the first 10,000 only." class="btn btn-sm btn-primary pull-left btn-upload" href="${main.contentAuditorLink}?${QueryString}&amp;form=csv_export&amp;num_ranks=10000"><span class="fa fa-lg fa-angle-double-down"></span> Export CSV Data</a>          
</#if>

<div class="form-field select field-sort pull-right" data-url="?${QueryString}">   
    <#assign optionsList=["=Relevance", "title=Title (A-Z)", "dtitle=Title (Z-A)", "url=URL (A-Z)", "durl=URL (Z-A)", "date=Date (New to Old)", "ddate=Date (Old to New)"]>
    <#list response.customData.displayMetadata?keys as key>
        <#assign heading = response.customData.displayMetadata[key]?replace("^\\d*\\.","","r")>
        <#if key != "d" && key != "t"> <#-- d and t are handled specially above -->
            <#assign optionsList = optionsList + [ "meta" + key + "=" + heading + " (A-Z)" ] />
            <#assign optionsList = optionsList + [ "dmeta" + key + "=" + heading + " (Z-A)" ] />
        </#if>
    </#list>
    <div class="form-inline"><small>Sort by &nbsp;</small> <@s.Select class="form-control input-sm" name="sort" id="sort" options=optionsList /></div>
</div>


</div>

    <@appliedFacetsBlock urlHash="#collection-${currentCollection}-tab-2"/>

    <!-- START RESULTS -->
    <#-- Hide the table if there are no results -->
    <table id="report-details" class="table table-hover table-responsive table-striped table-row-clickable <#if response.resultPacket.resultsSummary.totalMatching == 0>hidden</#if>">

    <thead>
        <tr>
            <#assign url = main.contentAuditorLink + "?" + changeParam(QueryString, "sort",'dtitle') />
			<th scope="col"><span>Page</span></th>
			<th scope="col"><span class="sr-only">Actions</span></th> 
            <#list response.customData.displayMetadata?values as value>
                <#assign heading = value?replace("^\\d*\\.","","r")>
                <th scope="col">${heading?html}</th>
            </#list>
        </tr>
    </thead>
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
            <tr <#if s.result.fileType == 'html'>class="previewable"</#if>>
                
                <!-- PAGE and URL -->
				<td>
				
					<div class="pull-left">
					<a class="clickable-link" target="_blank" href="${s.result.liveUrl?html}" title="${s.result.title}"><strong><@s.Truncate 150>${s.result.title}</@s.Truncate></strong></a> 
						<#if s.result.liveUrl??>
    						<br><span class="text-muted">${s.result.liveUrl?html}</span>
                        <#else>
                            &nbsp;
						</#if> 
				   </td>
				
				
				<!-- ACTIONS -->
               
                  <td class="table-hide">
				  	
					
					<a class="open-anchors pass" target="_blank" data-modal="overlay" href="anchors.html?collection=${question.inputParameterMap["collection"]?url}&docnum=${s.result.docNum?c}&ajax=1" data-toggle="tooltip" data-placement="bottom" title="Analyse Anchor Tags">
					<span class="fa-stack fa-xs">
    					<i class="fa fa-square fa-stack-2x"></i>
    					<i class="fa fa-anchor fa-stack-1x fa-inverse"></i>
					</span>
					</a>
					
					<a class="open-content-optimiser pass" target="_blank" href="content-optimiser.html?collection=${question.inputParameterMap["collection"]?url}&amp;profile=${question.inputParameterMap["profile"]!?url}&amp;optimiser_url=${s.result.liveUrl?replace("http://","")?url}&amp;query=${response.resultPacket.queryAsProcessed?url}" data-toggle="tooltip" data-placement="bottom" title="Optimise with Content Optimiser">
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


<!-- PAGINATION -->
<#if response.resultPacket.resultsSummary.totalMatching != 0>

<div>
  <ul class="pagination">
    <@fb.Prev link=main.contentAuditorLink><li><a href="${fb.prevUrl?replace('&form=documents','')?html}#collection-${currentCollection}-tab-2">Prev</a></li></@fb.Prev>
    <#if response?exists && response.resultPacket?exists && response.resultPacket.resultsSummary?exists>
        <#if response.resultPacket.resultsSummary.nextStart?exists || response.resultPacket.resultsSummary.prevStart?exists>
            <@fb.Page link=main.contentAuditorLink>
            <li <#if fb.pageCurrent> class="active"</#if>><a href="${fb.pageUrl?replace('&form=documents','')?html}#collection-${currentCollection}-tab-2">${fb.pageNumber}</a></li>
            </@fb.Page>
        </#if>
    </#if>
    <@fb.Next link=main.contentAuditorLink><li><a href="${fb.nextUrl?replace('&form=documents','')?html}#collection-${currentCollection}-tab-2">Next</a></li></@fb.Next>
  </ul>
</div>

</div>
</#if>
    
<!--ENDDOCUMENTS-->
</@s.AfterSearchOnly>