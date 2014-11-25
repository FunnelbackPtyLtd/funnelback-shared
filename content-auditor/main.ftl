<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<#-- Assign global variables -->

<#assign serviceName0>${question.inputParameterMap["collection"]!?html}</#assign>
<#assign serviceName1>Funnelback Sample Report 2014 Testing Truncation </#assign>
<#assign serviceName2>Search Report one</#assign>
<#assign serviceName3>Search Report two</#assign>
<#assign serviceName4>Search Report 4</#assign>

<#assign collectionId0>${question.inputParameterMap["collection"]!?html}</#assign>
<#assign collectionId1>TODO</#assign>
<#assign collectionId2>TODO</#assign>
<#assign collectionId3>TODO</#assign>

<#assign absoluteHtmlUrl>${httpRequest.requestURL}</#assign>

<#-- InitialTabs -->
<#macro InitialTabs>

<!-- TABBED NAVIGATION -->
  <div class="tabbable">
      <ul class="nav nav-tabs">
          <li class="active"><a href="#collection-1" data-toggle="tab"><#-- ${serviceName0} --> Sample client</a></li>
          <li><a href="#collection-2" data-toggle="tab">${serviceName1}</a></li>
          <li><a href="#collection-3" data-toggle="tab">${serviceName2}</a></li>
          <li><a href="#collection-4" data-toggle="tab">${serviceName3}</a></li>
      </ul>
      <div class="tab-content">
        <div class="tab-pane active" id="collection-1">
          <#include "/web/templates/modernui/content-auditor/reportdetails.ftl" />

          <div class="tabbable">
              <ul class="nav nav-tabs">
                  <li class="active"><a href="#collection-1-tab-1" data-toggle="tab">Content Attributes</a></li>
                  <li><a href="#collection-1-tab-2" data-toggle="tab">Content Documents</a></li>
              </ul>
              <div class="tab-content row">
                
				  <div class="tab-pane active" id="collection-1-tab-1">
                    <#include "/web/templates/modernui/content-auditor/facetsinitial.ftl" />
                  </div>
				  
                  <div class="tab-pane" id="collection-1-tab-2">
                    <#assign initialSearchDocumentsCollection0>${absoluteHtmlUrl}?collection=${collectionId0}&type=documentsinitial&start_rank=${question.inputParameterMap["start_rank"]!?html}</#assign>
                    <!-- Retrieve documents from ${initialSearchDocumentsCollection0} -->
                    <div class="fb-initial-documents">
                      <@fb.IncludeUrl url="${initialSearchDocumentsCollection0}" start="<!--BEGINDOCUMENTS-->" end="<!--ENDDOCUMENTS-->" expiry=0 />
                    </div>
                  </div>
				              
              </div>
          </div><!-- .tabbable level 2 tabs -->
        </div>
        <div class="tab-pane" id="collection-2">
          <div class="tabbable">
              <ul class="nav nav-tabs">
                  <li class="active"><a href="#collection-2-tab-1" data-toggle="tab">Content Attributes</a></li>
                  <li><a href="#collection-2-tab-2" data-toggle="tab">Content Documents</a></li>
              </ul>
              <div class="tab-content">
                  <div class="tab-pane active" id="collection-2-tab-1">
                    <p>Content attributes are not available</p>
                  </div>
                  <div class="tab-pane" id="collection-2-tab-2">
                    <p>Content documents are not available</p>
                  </div>                    
              </div>
          </div>            
        </div>
        <div class="tab-pane" id="collection-3">
          <div class="tabbable">
              <ul class="nav nav-tabs">
                  <li class="active"><a href="#collection-3-tab-1" data-toggle="tab">Content Attributes</a></li>
                  <li><a href="#collection-3-tab-2" data-toggle="tab">Content Documents</a></li>
              </ul>
              <div class="tab-content">
                  <div class="tab-pane active" id="collection-3-tab-1">
                    <p>Content attributes are not available</p>
                  </div>
                  <div class="tab-pane" id="collection-3-tab-2">
                    <p>Content documents are not available</p>
                  </div>                    
              </div>
          </div>
        </div>
        <div class="tab-pane" id="collection-4">
          <div class="tabbable">
              <ul class="nav nav-tabs">
                  <li class="active"><a href="#collection-4-tab-1" data-toggle="tab">Content Attributes</a></li>
                  <li><a href="#collection-4-tab-2" data-toggle="tab">Content Documents</a></li>
              </ul>
              <div class="tab-content">
                  <div class="tab-pane active" id="collection-4-tab-1">
                    <p>Content attributes are not available</p>
                  </div>
                  <div class="tab-pane" id="collection-4-tab-2">
                    <p>Content documents are not available</p>
                  </div>                    
              </div>
          </div>          
        </div>                        
      </div><!-- .tab-content level 1 tabs -->   
  </div><!-- .tabbable level 1 tabs -->

</#macro>


<#-- ResultTabsNavigaton -->

	<#macro ResultTabsNavigaton>
		<li class="active"><a href="#collection-1" data-toggle="tab">Sample Search Report <#--${serviceName0}--> </a></li>
		<li><a class="text-overflow" href="#collection-2" data-toggle="tab">${serviceName1}</a></li>
		<li><a class="text-overflow" href="#collection-3" data-toggle="tab">${serviceName2}</a></li>
		<li><a class="text-overflow" href="#collection-4" data-toggle="tab">${serviceName3}</a></li>
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

