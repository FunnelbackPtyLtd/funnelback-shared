<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>
<!--BEGINDOCUMENTS-->
<!-- documentsinitial.ftl -->

<!-- Collection: <@s.cfg>service_name</@s.cfg> | Attributes last updated: ${response.resultPacket.details.collectionUpdated?datetime} -->

<!-- RESULTS SUMMARY -->
<p class="<@s.FacetedSearch>fb-with-faceting</@s.FacetedSearch>">        
    <#if response.resultPacket.resultsSummary.totalMatching == 0>
        <strong class="fb-result-count" id="fb-total-matching">0</strong> documents
    </#if>
    <#if response.resultPacket.resultsSummary.totalMatching != 0>
        <strong class="fb-result-count" id="fb-page-start">${response.resultPacket.resultsSummary.currStart}</strong> -
        <strong class="fb-result-count" id="fb-page-end">${response.resultPacket.resultsSummary.currEnd}</strong> of
        <strong class="fb-result-count" id="fb-total-matching">${response.resultPacket.resultsSummary.totalMatching?string.number}</strong>
        documents</strong>
    </#if>
</p>

    <!-- CSV DOWNLOAD -->
    <a class="btn" href="/s/search.html?${QueryString?replace("form=documentsinitial","form=csv_export")}&num_ranks=10000">Download as CSV (Maximum 10K Records)</a>          
  
    <!-- START RESULTS -->
    <table class="table table-striped">
    <thead>
        <tr>
            <th scope="col">Actions</th> 
            <th scope="col">Title</th>
            <th scope="col">URL/Path</th>            
            <th scope="col">Date Last Updated</th>
            <th scope="col">DC.Format</th>
            <th scope="col">Subjects</th>
        </tr>
    </thead>
    <tfoot>
        <tr>
            <th scope="col">Actions</th>    
            <th scope="col">Title</th>
            <th scope="col">URL/Path</th>            
            <th scope="col">Date Last Updated</th>
            <th scope="col">DC.Format</th>
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
                <!-- ACTIONS -->
                <td>
                  <p><strong>Anchor report</strong></p>
                  <p>
                    <a href="/s/anchors.html?collection=${question.inputParameterMap["collection"]!?html}&amp;docnum=${s.result.docNum?c}">View Anchors</a></li>
                  </p>
                  <form action="${httpRequest.requestURL}/../../content-optimiser/optimise.html">
                    <p><strong>Content optimiser</strong></p>
                    <input type="text" name="query" value="" placeholder="Query to match against result" />
                    <input type="hidden" name="optimiser_url" value="${s.result.liveUrl?html?replace("http://","")}" />
                    <input type="hidden" name="collection" value="business-gov-internet" />
                    <input type="hidden" name="profile" value="_default" />
                    <input type="hidden" name="optimiser_ts" value="1401176844554" />
                    <input type="submit" name="submit" value="Run" />
                  </form>
                </td>
            
                <!-- TITLE -->
                <td><a href="${s.result.liveUrl?html}" title="${s.result.title}"><@s.Truncate 150>${s.result.title}</@s.Truncate></a></td>
                
                <!-- SITE (Z) -->
                <td>
                <#if s.result.liveUrl??>
                    ${s.result.liveUrl?html?replace("http://www.canberra.edu.au","")}
                <#else>                        
                    &nbsp;
                </#if>                 
                </td>
                
                <!-- DATE -->
                <td class="nowrap">
                <#if s.result.date??>
                    ${s.result.date?date?string("yyyy-MM-dd")}                    
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
            <li>are not using any advanced search operators like + - | " etc.</li> 
            <li>expect this document to exist within the <em><@s.cfg>service_name</@s.cfg></em><@s.IfDefCGI name="scope"> and within <em><@s.Truncate length=80>${question.inputParameterMap["scope"]!?html}</@s.Truncate></em></@s.IfDefCGI></li>
            <li>have permission to see any documents that may match your query</li>
        </ul>
    </p> 
</#if>

<!-- RESULTS SUMMARY -->
<p class="<@s.FacetedSearch>fb-with-faceting</@s.FacetedSearch>">        
    <#if response.resultPacket.resultsSummary.totalMatching == 0>
        <strong class="fb-result-count" id="fb-total-matching">0</strong> documents
    </#if>
    <#if response.resultPacket.resultsSummary.totalMatching != 0>
        <strong class="fb-result-count" id="fb-page-start">${response.resultPacket.resultsSummary.currStart}</strong> -
        <strong class="fb-result-count" id="fb-page-end">${response.resultPacket.resultsSummary.currEnd}</strong> of
        <strong class="fb-result-count" id="fb-total-matching">${response.resultPacket.resultsSummary.totalMatching?string.number}</strong>
        documents</strong>
    </#if>
</p>


<!-- PAGINATION -->
<div class="pagination pagination-large">
  <ul>
    <@fb.Prev><li><a href="${fb.prevUrl?replace('&query=-padrenull&form=documentsinitial','')?html}">Prev</a></li></@fb.Prev>
    <@fb.Page>
    <li <#if fb.pageCurrent> class="active"</#if>><a href="${fb.pageUrl?replace('&query=-padrenull&form=documentsinitial','')?html}">${fb.pageNumber}</a></li>
    </@fb.Page>
    <@fb.Next><li><a href="${fb.nextUrl?replace('&query=-padrenull&form=documentsinitial','')?html}">Next</a></li></@fb.Next>
  </ul>
</div>
    
<!--ENDDOCUMENTS-->
</@s.AfterSearchOnly>