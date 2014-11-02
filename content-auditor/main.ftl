<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<#-- Assign global variables -->

<#assign serviceName0>${question.inputParameterMap["collection"]!?html}</#assign>
<#assign serviceName1>TODO</#assign>
<#assign serviceName2>TODO</#assign>
<#assign serviceName3>TODO</#assign>

<#assign collectionId0>${question.inputParameterMap["collection"]!?html}</#assign>
<#assign collectionId1>TODO</#assign>
<#assign collectionId2>TODO</#assign>
<#assign collectionId3>TODO</#assign>

<#assign absoluteHtmlUrl>${httpRequest.requestURL}</#assign>

<#-- InitialTabs -->
<#macro InitialTabs>

<!-- TABBED NAVIGATION -->
  <div class="tabbable lvl-1">
      <ul class="nav nav-tabs lvl-1">
          <li class="active"><a href="#collection-1" data-toggle="tab"><#-- ${serviceName0} --> Sample client</a></li>
          <li><a href="#collection-2" data-toggle="tab">${serviceName1}</a></li>
          <li><a href="#collection-3" data-toggle="tab">${serviceName2}</a></li>
          <li><a href="#collection-4" data-toggle="tab">${serviceName3}</a></li>
      </ul>
      <div class="tab-content">
        <div class="tab-pane active" id="collection-1">
          <#include "/web/templates/modernui/content-auditor/reportdetails.ftl" />

          <div class="tabbable lvl-2">
              <ul class="nav nav-tabs lvl-2">
                  <li class="active"><a href="#collection-1-tab-1" data-toggle="tab">Content Attributes</a></li>
                  <li><a href="#collection-1-tab-2" data-toggle="tab">Content Documents</a></li>
              </ul>
              <div class="tab-content lvl-2">
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
          <div class="tabbable lvl-2">
              <ul class="nav nav-tabs lvl-2">
                  <li class="active"><a href="#collection-2-tab-1" data-toggle="tab">Content Attributes</a></li>
                  <li><a href="#collection-2-tab-2" data-toggle="tab">Content Documents</a></li>
              </ul>
              <div class="tab-content lvl-2">
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
          <div class="tabbable lvl-2">
              <ul class="nav nav-tabs lvl-2">
                  <li class="active"><a href="#collection-3-tab-1" data-toggle="tab">Content Attributes</a></li>
                  <li><a href="#collection-3-tab-2" data-toggle="tab">Content Documents</a></li>
              </ul>
              <div class="tab-content lvl-2">
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
          <div class="tabbable lvl-2">
              <ul class="nav nav-tabs lvl-2">
                  <li class="active"><a href="#collection-4-tab-1" data-toggle="tab">Content Attributes</a></li>
                  <li><a href="#collection-4-tab-2" data-toggle="tab">Content Documents</a></li>
              </ul>
              <div class="tab-content lvl-2">
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

<#-- ResultTabs -->
<#macro ResultTabs>

    <!-- TABBED NAVIGATION -->
    <div class="tabbable lvl-1">
      <ul class="nav nav-tabs lvl-1">
        <li class="active"><a href="#collection-1" data-toggle="tab">Sample report <#--${serviceName0}--> </a></li>
        <li><a href="#collection-2" data-toggle="tab">${serviceName1}</a></li>
        <li><a href="#collection-3" data-toggle="tab">${serviceName2}</a></li>
        <li><a href="#collection-4" data-toggle="tab">${serviceName3}</a></li>
      </ul>
      <div class="tab-content lvl-1">
        <div class="tab-pane active" id="collection-1">

        <div class="fb-report-details"></div>
        <#include "/web/templates/modernui/content-auditor/reportdetails.ftl" />

          <div class="tabbable lvl-2">
            <ul class="nav nav-tabs lvl-2">
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
                    <div class="fb-after-search-documents">
                      <#include "/web/templates/modernui/content-auditor/documents.ftl" />
                    </div>
                </div>                    
            </div>
          </div><!-- .tabbable level 2 tabs -->      
        </div>
        <div class="tab-pane" id="collection-2">
            <div class="tabbable lvl-2">
                <ul class="nav nav-tabs lvl-2">
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
            <div class="tabbable lvl-2">
                <ul class="nav nav-tabs lvl-2">
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
            <div class="tabbable lvl-2">
                <ul class="nav nav-tabs lvl-2">
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
    </div><!-- .tab-content level 1 tabs -->   
</div><!-- .tabbable level 1 tabs -->

</#macro>
