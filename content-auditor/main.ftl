<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<#assign absoluteHtmlUrl>${httpRequest.requestURL}</#assign>
<#assign queryToReport = response.resultPacket.queryAsProcessed?replace("-padrenullquery", "") />
<#if queryToReport?matches("\\s*")>
    <#assign queryToReport = "[All documents]" />
</#if>

<#-- ResultTabsNavigaton -->

	<#macro ResultTabsNavigaton>
    <#assign url = "content-auditor.html?" + changeParam(QueryString, "view", "live") />
    
        <nav role="navigation" class="collapse navbar-collapse navbar-side">
            <ul class="nav navbar-nav">
                <li class="nav-title">content reports</li>
                <li class="nav-live<#if QueryString?contains("view=live")> active</#if>">
                    <a class="text-overflow" href="${url}"><span class="fa-stack fa-lg"><i class="fa fa-square fa-stack-2x"></i><i class="fa fa-flash fa-stack-1x"></i></span> Live View</a>
                </li>
                <#list question.collection.configuration.snapshotIds?sort?reverse as id>
                    <#assign snapshotID = "snapshot" + id?c />  
                    <#assign url = "content-auditor.html?" + changeParam(QueryString, "view", snapshotID)?replace("&_pjax","") />
                 <li class="nav-${snapshotID} <#if question.inputParameterMap["view"] == snapshotID>active</#if>">
                    <a class="text-overflow" href="${url}"> <span class="fa-stack fa-lg"><i class="fa fa-square fa-stack-2x"></i><i class="fa fa-camera-retro fa-stack-1x"></i></span>${question.collection.configuration.value("ui.modern.content-auditor.snapshot_name." + id)!("Snapshot " + id)?html}</a>
                </li>
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
        <div class="tab-pane active" id="collection-1">

        <div class="fb-report-details"></div>
        <#include "/web/templates/modernui/content-auditor/reportdetails.ftl" />

          <div class="tabbable">
            <ul class="nav nav-tabs">
                <li class="active"><a href="#collection-1-tab-1" data-toggle="tab">Attributes</a></li>
                <li><a href="#collection-1-tab-2" data-toggle="tab">Search Results</a></li>
                <li><a href="#collection-1-tab-3" data-toggle="tab">Duplicate Content</a></li>
            </ul>

            <div class="tab-content">
                <div class="tab-pane active clearfix" id="collection-1-tab-1">
                    <div class="fb-after-search-facets"></div>
					    <#include "/web/templates/modernui/content-auditor/facets.ftl" />
                        
                    </div>
                <div class="tab-pane" id="collection-1-tab-2">
                    <#if QueryString?contains("type=")>
                        <#assign afterSearchDocumentsCollectionOne>${absoluteHtmlUrl}?${QueryString?replace("type=index","type=documents")}&start_rank=${question.inputParameterMap["start_rank"]!?html}</#assign>
                    <#else>
                        <#assign afterSearchDocumentsCollectionOne>${absoluteHtmlUrl}?${QueryString}&type=documents&start_rank=${question.inputParameterMap["start_rank"]!?html}</#assign>
                    </#if>                        
                    
                    <!-- Include from ${afterSearchDocumentsCollectionOne} -->
                    
                      <#include "/web/templates/modernui/content-auditor/documents.ftl" />
                    
                </div> 

                <div id="collection-1-tab-3"  class="tab-pane clearfix">
                    <#include "/web/templates/modernui/content-auditor/collapsed_duplicates.ftl" />
                </div> 

            </div>
          </div><!-- .tabbable level 2 tabs -->      
        </div>
        <div class="tab-pane" id="collection-2">
            <div class="tabbable">
                <ul class="nav nav-tabs clearfix">
                    <li class="active"><a href="#collection-2-tab-1" data-toggle="tab">Search Attributes</a></li>
                    <li><a href="#collection-2-tab-2" data-toggle="tab">Search Results</a></li>
                </ul>
                <div class="tab-content">
                    <div class="tab-pane active clearfix" id="collection-2-tab-1">
                        <p>Search attributes are not available</p>
                    </div>
                    <div class="tab-pane clearfix" id="collection-2-tab-2">
                        <p>Search results are not avilable</p>
                    </div>                    
                </div>
            </div>            
        </div>
        <div class="tab-pane" id="collection-3">
            <div class="tabbable">
                <ul class="nav nav-tabs">
                    <li class="active"><a href="#collection-3-tab-1" data-toggle="tab">Search Attributes</a></li>
                    <li><a href="#collection-3-tab-2" data-toggle="tab">Search Results</a></li>
                </ul>
                <div class="tab-content">
                    <div class="tab-pane active clearfix" id="collection-3-tab-1">
                        <p>Search attributes are not available</p>
                    </div>
                    <div class="tab-pane clearfix" id="collection-3-tab-2">
                        <p>Search results are not avilable</p>
                    </div>                    
                </div>
            </div>
        </div>
        <div class="tab-pane" id="collection-4">
            <div class="tabbable ">
                <ul class="nav nav-tabs">
                    <li class="active"><a href="#collection-4-tab-1" data-toggle="tab">Search Attributes</a></li>
                    <li><a href="#collection-4-tab-2" data-toggle="tab">Search Results</a></li>
                </ul>
                <div class="tab-content">
                    <div class="tab-pane active clearfix" id="collection-4-tab-1">
                        <p>Search attributes are not available</p>
                    </div>
                    <div class="tab-pane clearfix" id="collection-4-tab-2">
                        <p>Search results are not avilable</p>
                    </div>                    
                </div>
            </div>         
        </div>
		</div>              
		
		<#-- Start:Macro: main.ftl - Footer -->
	<@design.Footer />
	<#-- End:Macro: main.ftl - Footer -->
</#macro>          



    </section>
	
</div><!-- .tabbable level 1 tabs -->

