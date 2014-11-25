<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<#assign absoluteHtmlUrl>${httpRequest.requestURL}</#assign>

<#-- ResultTabsNavigaton -->

	<#macro ResultTabsNavigaton>
    <#assign url = "content-auditor.html?" + changeParam(QueryString, "view", "live") />
    <li class="active"><a class="text-overflow" href="${url}" data-toggle="tab">Latest data</a></li>
    <#list question.collection.configuration.snapshotIds?sort?reverse as id>
      <#assign url = "content-auditor.html?" + changeParam(QueryString, "view", "snapshot" + id) />
      <li><a class="text-overflow" href="${url}" data-toggle="tab">
        ${question.collection.configuration.value("ui.modern.content-auditor.snapshot_name." + id)!("Snapshot ID:" + id)?html}
      </a></li>
    </#list>
	</#macro>
	
		  
	<#-- ResultTabs -->
	<#macro ResultTabs>

    <!-- TABBED NAVIGATION -->
    <section id="result-tabs">
	<div class="container-fluid">
     
      <div class="tab-content">
        <div class="tab-pane active" id="collection-1">

        <div class="fb-report-details"></div>
        <#include "/web/templates/modernui/content-auditor/reportdetails.ftl" />

          <div class="tabbable">
            <ul class="nav nav-tabs">
                <li class="active"><a href="#collection-1-tab-1" data-toggle="tab">Search Attributes</a></li>
                <li><a href="#collection-1-tab-2" data-toggle="tab">Search Results</a></li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="collection-1-tab-1">
                    <div class="fb-after-search-facets"></div>
					    <#include "/web/templates/modernui/content-auditor/facets.ftl" />
              <#include "/web/templates/modernui/content-auditor/collapsed_duplicates.ftl" />
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
            </div>
          </div><!-- .tabbable level 2 tabs -->      
        </div>
        <div class="tab-pane" id="collection-2">
            <div class="tabbable">
                <ul class="nav nav-tabs">
                    <li class="active"><a href="#collection-2-tab-1" data-toggle="tab">Search Attributes</a></li>
                    <li><a href="#collection-2-tab-2" data-toggle="tab">Search Results</a></li>
                </ul>
                <div class="tab-content">
                    <div class="tab-pane active" id="collection-2-tab-1">
                        <p>Search attributes are not available</p>
                    </div>
                    <div class="tab-pane" id="collection-2-tab-2">
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
                    <div class="tab-pane active" id="collection-3-tab-1">
                        <p>Search attributes are not available</p>
                    </div>
                    <div class="tab-pane" id="collection-3-tab-2">
                        <p>Search results are not avilable</p>
                    </div>                    
                </div>
            </div>
        </div>
        <div class="tab-pane" id="collection-4">
            <div class="tabbable">
                <ul class="nav nav-tabs">
                    <li class="active"><a href="#collection-4-tab-1" data-toggle="tab">Search Attributes</a></li>
                    <li><a href="#collection-4-tab-2" data-toggle="tab">Search Results</a></li>
                </ul>
                <div class="tab-content">
                    <div class="tab-pane active" id="collection-4-tab-1">
                        <p>Search attributes are not available</p>
                    </div>
                    <div class="tab-pane" id="collection-4-tab-2">
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

