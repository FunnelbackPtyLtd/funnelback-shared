<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
<#assign absoluteHtmlUrl>${httpRequest.requestURL}</#assign>
<#assign queryToReport = response.resultPacket.queryAsProcessed?replace("-padrenullquery", "") />
<#if queryToReport?matches("\\s*")>
<#assign queryToReport = "[All documents]" />
</#if>
<#assign currentCollection>${RequestParameters.collection}</#assign>


<#-- errorNoResults -->
 <#macro errorNoResults>
 <div id="error-no-results">
      <p id="fb-no-results">Your search for <strong>${question.inputParameterMap["query"]!?html}</strong> did not return any results. <span>Please ensure that you:</span>
        <ul>
            <li>are <strong>not</strong> using any advanced search operators like: <strong>+ - | "</strong> etc.</li> 
            <li>expect this document to exist within the <em><strong><@s.cfg>service_name</@s.cfg></strong></em><@s.IfDefCGI name="scope"> and within <em><@s.Truncate length=80>${question.inputParameterMap["scope"]!?html}</@s.Truncate></em></@s.IfDefCGI></li>
            <li>have permission to see any documents that may match your query</li>
        </ul>
      </p> 
</div>      
</#macro>
<#-- ResultTabsNavigaton -->
<#macro ResultTabsNavigaton>
<#assign url = "content-auditor.html?" + changeParam(QueryString, "view", "live") />
<nav role="navigation" class="collapse navbar-collapse navbar-side">
    <ul class="nav navbar-nav">
        <li class="nav-title">content reports</li>
        <li class="nav-live<#if QueryString?contains("view=live")> active</#if>"> <a class="text-overflow" href="${url}"><span class="fa-stack fa-lg"><i class="fa fa-square fa-stack-2x"></i><i class="fa fa-flash fa-stack-1x"></i></span> Live View</a> </li>
        <#list question.collection.configuration.snapshotIds?sort?reverse as id>
        <#assign snapshotID = "snapshot" + id?c />
        <#assign url = "content-auditor.html?" + changeParam(QueryString, "view", snapshotID)?replace("&_pjax","") />
        <li class="nav-${snapshotID} <#if question.inputParameterMap["view"] == snapshotID>active</#if>"> <a class="text-overflow" href="${url}"> <span class="fa-stack fa-lg"><i class="fa fa-square fa-stack-2x"></i><i class="fa fa-photo fa-stack-1x"></i></span>${question.collection.configuration.value("ui.modern.content-auditor.snapshot_name." + id)!("Snapshot " + id)?html}</a> </li>
        </#list>
    </ul>
</nav>
</#macro>
<#-- ResultTabs -->
<#macro ResultTabs>
<!-- TABBED NAVIGATION -->
<section id="result-tabs">
    <div class="container-fluid">
        <div class="tab-content clearfix">
            <div class="tab-pane active" id="collection-${currentCollection}">
                <div class="fb-report-details"></div>
                <#include "/web/templates/modernui/content-auditor/reportdetails.ftl" />
                <div class="tabbable">
                    <ul class="nav nav-tabs">
                        <li class="active"><a href="#collection-${currentCollection}-tab-1" data-toggle="tab" id="tab-nav-attributes">Attributes</a></li>
                        <li><a href="#collection-${currentCollection}-tab-2" data-toggle="tab" id="tab-nav-results">Search Results</a></li>
                        <li><a href="#collection-${currentCollection}-tab-3" data-toggle="tab" id="tab-nav-duplicates">Duplicate Content</a></li>
                    </ul>
                    
                    <div class="tab-content">
                        <#if (response.resultPacket.resultsSummary.totalMatching > 0)>
                        <div class="tab-pane active clearfix" id="collection-${currentCollection}-tab-1">
                            <div class="fb-after-search-facets"></div>
                        <#include "/web/templates/modernui/content-auditor/facets.ftl" /> </div>
                        <div class="tab-pane" id="collection-${currentCollection}-tab-2"> <#if QueryString?contains("type=")>
                            <#assign afterSearchDocumentsCollectionOne>${absoluteHtmlUrl}?${QueryString?replace("type=index","type=documents")}&start_rank=${question.inputParameterMap["start_rank"]!?html}</#assign>
                            <#else>
                            <#assign afterSearchDocumentsCollectionOne>${absoluteHtmlUrl}?${QueryString}&type=documents&start_rank=${question.inputParameterMap["start_rank"]!?html}</#assign>
                            </#if>
                            <!-- Include from ${afterSearchDocumentsCollectionOne} -->
                        <#include "/web/templates/modernui/content-auditor/documents.ftl" /> </div>
                        <div id="collection-${currentCollection}-tab-3"  class="tab-pane clearfix"> <#include "/web/templates/modernui/content-auditor/collapsed_duplicates.ftl" /> </div>
                        
                        <#else>
                        
                        <!-- NO RESULTS -->
                        
                            <@errorNoResults />
                        

                        </#if>

                    </div>
                    
                </div>
                <!-- .tabbable level 2 tabs -->
            </div>
        </div>
        <#-- Start:Macro: main.ftl - Footer -->
        <@design.Footer />
        <#-- End:Macro: main.ftl - Footer -->
        </#macro>
    </section>
</div>
<!-- .tabbable level 1 tabs -->