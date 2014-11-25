<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>
<!--BEGINDOCUMENTS-->

<!-- Collection: <@s.cfg>service_name</@s.cfg> | Attributes last updated: ${response.resultPacket.details.collectionUpdated?datetime} -->
<div class="tab-header clearfix">
	<p class="pull-left">        
	<#if response.resultPacket.resultsSummary.totalMatching == 0>
		<strong class="fb-result-count" id="fb-total-matching">0</strong> search results for <strong><@s.QueryClean /></strong>
	</#if>
	<#if response.resultPacket.resultsSummary.totalMatching != 0>
		Displaying <strong class="fb-result-count" id="fb-page-start">${response.resultPacket.resultsSummary.currStart}</strong> -
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
<#if response.resultPacket.resultsSummary.totalMatching != 0>       
<!-- CSV DOWNLOAD -->
<a data-toggle="tooltip" data-placement="left" title="Download a CSV File of these results. Results limited to the first 10,000 only." class="btn btn-sm btn-primary pull-left btn-upload" href="/s/search.html?${QueryString?replace("form=documents","form=csv_export")}&num_ranks=10000"><span class="fa fa-lg fa-cloud-download"></span> Download Results</a>          
</#if>
</div>    
    <!-- START RESULTS -->
    <#-- Hide the table if there are no results -->
    <table class="table table-hover table-responsive table-striped table-row-clickable <#if response.resultPacket.resultsSummary.totalMatching == 0>hidden</#if>">

    <thead>
        <tr>
            
			<th scope="col"><span class="sr-only">Page</span></th>
			<th scope="col"><span class="sr-only">Actions</span></th> 
			<!--<th scope="col">URL/Path</th>-->          
			<th scope="col">Last Updated</th>
			<th scope="col">DC.Format</th>
			<th scope="col">Detected Format</th>
			<th scope="col">Subjects</th>
        </tr>
    </thead>
    <tfoot class="hidden">
        <tr>
           
            <th scope="col">Page</th>
			 <th scope="col"><span class="sr-only">Actions</span></th> 
            <!--<th scope="col">URL/Path</th>-->            
            <th scope="col">Last Updated</th>
            <th scope="col">DC.Format</th>
            <th scope="col">Detected Format</th>
            <th scope="col">Subjects</th>                               
        </tr>
    </tfoot>
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
				<div class="pull-left table-hide" style="vertical-align: middle;
  width: 15px ;
  height: 100%;
  display: table-cell;
  text-align: center;
  margin-right: 30px;
  padding-top:7px;">
		<span class="fa-stack fa-xs">
		<i class="fa fa-circle fa-stack-2x"></i>
		<i class="fa fa-wrench fa-stack-1x fa-inverse"></i>
		</span>
		
  
  </div>  
					<div class="pull-left">
					<a class="clickable-link" href="${s.result.liveUrl?html}" title="${s.result.title}"><strong><@s.Truncate 150>${s.result.title}</@s.Truncate></strong></a> 
						<#if s.result.liveUrl??>
						<!-- SITE (Z) -->
						<br><span class="text-muted" href="#add link here">${s.result.liveUrl?html?replace("http://www.canberra.edu.au","")}</span><#else> &nbsp;
						</#if> 
					</div> 
					
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
				  	
					
					<a href="/s/anchors.html?collection=${question.inputParameterMap["collection"]!?html}&amp;docnum=${s.result.docNum?c}" data-toggle="tooltip" data-placement="bottom" title="Analyse page anchor links">
					<span class="fa-stack fa-xs">
					<i class="fa fa-circle fa-stack-2x"></i>
					<i class="fa fa-anchor fa-stack-1x fa-inverse"></i>
					</span>
					</a>
					
					<a href="${httpRequest.requestURL}/../../content-optimiser/optimise.html?optimiser_url=${s.result.liveUrl?html?replace("http://","")}&collection=business-gov-internet&profile=_default&name=optimiser_ts=1401176844554&query=${question.inputParameterMap["query"]!?html}" data-toggle="tooltip" data-placement="bottom" title="Send to Content Optimiser">
					<span class="fa-stack fa-xs">
					<i class="fa fa-circle fa-stack-2x"></i>
					<i class="fa fa-wrench fa-stack-1x fa-inverse"></i>
					</span>
					</a>
					&nbsp;
					

				<!--<form action="${httpRequest.requestURL}/../../content-optimiser/optimise.html">
						<input type="hidden" name="optimiser_url" value="${s.result.liveUrl?html?replace("http://","")}" />
						<input type="hidden" name="collection" value="business-gov-internet" />
						<input type="hidden" name="profile" value="_default" />
						<input type="hidden" name="optimiser_ts" value="1401176844554" />
						<label for="query"><strong>Content optimiser</strong></label>  
						<div class="input-group">
							<input type="text" name="query" value="${question.inputParameterMap["query"]!?html}" class="form-control" />
							<span class="input-group-btn">
								<button type="submit" class="btn btn-primary">Run</button>
							</span>
						</div>  
					</form>
					
					
					
				  
				</td>	
				
                
                <!-- DATE -->
                <td class="nowrap">
                <#if s.result.date??>
                    ${s.result.date?date?string("dd MMM, yyyy")}                    
                <#else>
                    No Date
                </#if>
                </td>
                <!-- DC.FORMAT -->
                <td>
                <#if s.result.metaData["f"]??>
                    ${s.result.metaData["f"]}
                <#else>
                    &nbsp;
                </#if>
                </td>

                <!-- DETECTED FORMAT  -->
                <td>
                <#if s.result.fileType??>
                    ${s.result.fileType}
                <#else>
                    &nbsp;
                </#if>
                </td>
             
                <!-- AUTHOR -->
                <#--
                <td>
                <#if s.result.metaData["a"]??>
                    ul>
                    <#list s.result.metaData["a"]?split("|") as value>
                      <#if (value?length > 0)><li>${value}</li></#if>
                    </#list>  
                    </ul>
                <#else>
                    &nbsp;
                </#if>
                </td>
                -->
                
                
                <!-- SUBJECT (s) -->
                <td>
                <#if s.result.metaData["s"]??>
                    <ul>
                    <#list s.result.metaData["s"]?split(";") as value>
                        <#if (value?length > 0)><li>${value}</li></#if>
                    </#list>  
                    </ul>
                <#else>
                    &nbsp;
                </#if>
                </td>
                

            </tr>
            </#if>
        </@s.Results>                
    </tbody>
</table>

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
<p class="<@s.FacetedSearch>fb-with-faceting</@s.FacetedSearch>">        

    <#if response.resultPacket.resultsSummary.totalMatching == 0>
        <#-- Display nothing - no need to display 0 results twice.
          <strong class="fb-result-count" id="fb-total-matching">0</strong> search results for <strong><@s.QueryClean /></strong>
        -->
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


<!-- PAGINATION -->
<#if response.resultPacket.resultsSummary.totalMatching != 0>
<div>
  <ul class="pagination pagination-large">
    <@fb.Prev><li><a href="${fb.prevUrl?replace('&form=documents','')?html}">Prev</a></li></@fb.Prev>
    <@fb.Page>
    <li <#if fb.pageCurrent> class="active"</#if>><a href="${fb.pageUrl?replace('&form=documents','')?html}">${fb.pageNumber}</a></li>
    </@fb.Page>
    <@fb.Next><li><a href="${fb.nextUrl?replace('&form=documents','')?html}">Next</a></li></@fb.Next>
  </ul>
</div>
</#if>
    
<!--ENDDOCUMENTS-->
</@s.AfterSearchOnly>