<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
<#assign absoluteHtmlUrl>${httpRequest.requestURL}</#assign>
<#assign queryToReport = response.resultPacket.queryAsProcessed!''?replace("-padrenullquery", "") />
<#if queryToReport?matches("\\s*")>
${queryToReport}
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
        <li class="nav-live<#if QueryString?contains("view=live")> active</#if>"> <a class="text-overflow" href="${url}" title="View Live View report"><span class="fa-stack fa-lg"><i class="fa fa-square fa-stack-2x"></i><i class="fa fa-flash fa-stack-1x"></i></span> Live View</a> </li>
        <#list question.collection.configuration.snapshotIds?sort?reverse as id>
        <#assign snapshotID = "snapshot" + id?c />
        <#assign url = "content-auditor.html?" + changeParam(QueryString, "view", snapshotID)?replace("&_pjax","") />
        <li class="nav-${snapshotID} <#if question.inputParameterMap["view"] == snapshotID>active</#if>"> <a class="text-overflow" href="${url}" title="View ${question.collection.configuration.value("ui.modern.content-auditor.snapshot_name." + id)!("Snapshot " + id)?html} report"> <span class="fa-stack fa-lg"><i class="fa fa-square fa-stack-2x"></i><i class="fa fa-photo fa-stack-1x"></i></span>${question.collection.configuration.value("ui.modern.content-auditor.snapshot_name." + id)!("Snapshot " + id)?html}</a> </li>
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
                        <li class="active"><a href="#collection-${currentCollection}-tab-0" data-toggle="tab" id="tab-nav-overview" title="View Overview">Overview</a></li>
                        <li><a class="tab-switch-1" href="#collection-${currentCollection}-tab-1" data-toggle="tab" id="tab-nav-attributes" title="View Attributes">Attributes</a></li>
                        <li><a href="#collection-${currentCollection}-tab-2" data-toggle="tab" id="tab-nav-results" title="View Search Results">Search Results</a></li>
                        <li><a href="#collection-${currentCollection}-tab-3" data-toggle="tab" id="tab-nav-duplicates" title="View Duplicate Content">Duplicate Content</a></li>
                    </ul>
                    
                    <div class="tab-content">

                        <div class="tab-pane active clearfix" id="collection-${currentCollection}-tab-0">
                           
                               



                            <div id="facet-container-wrapper" style="margin:0" class="row">
                                <div class="col-md-12 no-border no-padding">
                                    <div class="fb-facet-header"> <h3><span class="facetLabel">Overview</span></h3></div>

                                                
                                <div class="overviews">


                                <@s.FacetedSearch>
                                    <@s.Facet> 
                                      <#assign categoryCount = 0 />
                                      <#assign sep = '' />

                                        <div class="col-md-6">                                    
                                        <div class="panel panel-default">
                                          <div class="panel-heading">
                                            <h3 class="panel-title"><@s.FacetLabel tag="strong"/></h3>
                                          </div>
                                          <div class="panel-body">
                                            
											
											<@s.Category max=4 tag="div">
                                            <#assign categoryCount = categoryCount + 1 />
                                           
                                              ${sep} <@s.CategoryName class="" />&nbsp;<small class="text-muted">(<@s.CategoryCount />)</small>
											
											<#assign sep = '' />                    
                                            <#if categoryCount == 4>
                                                 <div class="panel-footer"><a class="btn btn-xs btn-primary" data-toggle="tab" href="#collection-${currentCollection}-tab-1" aria-expanded="true" title="View more">View More</a>
                                                </div>
                                            </#if>
                                          </@s.Category>
										  
										  
                                          </div>
                                        </div>
                                        </div>


                                      
                                    </@s.Facet>
                                    
                                </@s.FacetedSearch>
								
								
								



                                <#--<@s.FacetedSearch>
                                    <@s.Facet class="dl-horizontal" > 
                                      <#assign categoryCount = 0 />
                                      <#assign sep = '' />
                                      <dt><@s.FacetLabel tag="b"/>:</dt>
                                      <dl class="col-xs-6"><@s.Category tag="span">
                                        <#assign categoryCount = categoryCount + 1 />
                                        <#if categoryCount &lt; 4>
                                          ${sep} <@s.CategoryName class="" />&nbsp;<small class="text-muted">(<@s.CategoryCount />)</small>
                                        </#if>
                                        <#assign sep = '<br>' />                    
                                        <#if categoryCount == 4>
                                             <br><a class="btn btn-xs btn-primary" data-toggle="tab" href="#collection-${currentCollection}-tab-1" aria-expanded="true" title="View more...">View More...</a>
                                        </#if>
                                      </@s.Category>
                                    </dl>
                                    </@s.Facet>
                                    
                                </@s.FacetedSearch>
                            -->

                                </div>

                                </div>
                            </div>


                                
                                
                           
                        </div>

                        <#if (response.resultPacket.resultsSummary.totalMatching > 0)>
                        <div class="tab-pane clearfix switch-2" id="collection-${currentCollection}-tab-1">
                            
                        <#include "/web/templates/modernui/content-auditor/facets.ftl" />
                        </div>
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