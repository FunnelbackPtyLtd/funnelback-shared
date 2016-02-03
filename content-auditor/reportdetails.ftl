<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>
<!--BEGINREPORTDETAILS-->
<!-- reportdetails.ftl -->
<!-- Collection: <@s.cfg>service_name</@s.cfg> | Attributes last updated: ${response.resultPacket.details.collectionUpdated?datetime} -->
<div class="tab-summary"> 
  <div class="inner">
    <h2><span>Report Details</span></h2>

    <div class="uri-selector"> <span class="text-muted">URI:</span>

    <div class="dropdown">
              <button class="btn btn-default btn-xs dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                 ${question.inputParameterMap["f.URI|url"]!?html}
                <span class="caret"></span>
              </button>
              <ul class="dropdown-menu" aria-labelledby="dropdownMenu1">
                <@s.FacetedSearch><@s.Facet name="URI">
                  <#assign facetDef = facetedNavigationConfig(question.collection, question.profile).getFacetDefinition(s.facet.name) >
                  <#if QueryString?contains("f." + facetDef.name?url)
                      || urlDecode(QueryString)?contains("f." + facetDef.name)
                      || urlDecode(QueryString)?contains("f." + facetDef.name?url)>
                      <li><a href="${s.FacetAllUrl(facetedNavigationConfig(question.collection, question.profile).getFacetDefinition(s.facet.name), main.contentAuditorLink)?html}">All URIs</a></li>
                  </#if>
                  
                  <@s.Category max=categoryMax tag="li">
                    <@s.CategoryName class="" link=main.contentAuditorLink />&nbsp;<small class="text-muted">(<@s.CategoryCount />)</small>
                  </@s.Category>
                </@s.Facet></@s.FacetedSearch>
              </ul>
            </div>
    </div>

    
    <p>
        <span class="text-muted">Collection:</span><strong id="detail-current-collection"> ${currentCollection}</strong>
        <#if question.collection.type! != "meta">
            &nbsp; <small class="text-muted"><em class="fa fa-lg fa-clock-o link" data-toggle="tooltip" data-placement="top" title="${currentCollection} was last updated on ${response.resultPacket.details.collectionUpdated?datetime}"></em></small>
        </#if>
    </p>

    <p><span class="data-total-doc-count" data-value="<#if response.resultPacket.resultsSummary.totalMatching != 0>${response.resultPacket.resultsSummary.totalMatching?string.number?replace(',','')}<#else>0</#if>"></p>
	
    
    
  </div>
</div>
<!--ENDREPORTDETAILS-->
</@s.AfterSearchOnly>

<#---
Displays all the currently applied facets
 
@param group Group categories by facet. (def=false)
@param class Optional CSS class.
-->
<#macro AppliedFacets class="applied-facets" tag="li" group=false grouptag="div" urlHash="" link=question.collection.configuration.value("ui.modern.search_link")><#compress>
<#list question.selectedCategoryValues?keys as key>
  <#local facetName = key?replace("^f.","","r")?replace("\\|.+$","","r")/>
 
  <#if group>
        <${grouptag} class="appliedFacets"> ${facetName?html}
        <#if tag == "li" || tag == "LI">
          <ul>
        </#if>
  </#if>
 
  <#list question.selectedCategoryValues[key] as value>
    <#if question.selectedCategoryValues?keys?seq_contains(key)>
        <#-- Find the label for this category. For nearly all categories the label is equal
             to the value returned by the query processor, but not for date counts for example.
             With date counts the label is the actual year "2003" or a "past 3 weeks" but the
             value is the constraint to apply like "d=2003" or "d>12Jun2012" -->
        <#-- Use value by default if we can't find a label -->
        <#local valueLabel = urlDecode(question.selectedCategoryValues[key][value_index]) />
 
        <#assign referenceFacet = response.facets/>
        <#if extraSearches?exists
                    && extraSearches[ExtraSearches.FACETED_NAVIGATION]?exists
                    && extraSearches[ExtraSearches.FACETED_NAVIGATION].response?exists
                    && extraSearches[ExtraSearches.FACETED_NAVIGATION].response.facets?exists>
          <#assign referenceFacet = extraSearches[ExtraSearches.FACETED_NAVIGATION].response.facets/>
        </#if>
 
        <#-- Iterate over generated facets -->
        <#list referenceFacet as facet>
            <#if facetName == facet.name>
                <#-- Facet located, find current working category -->
                <#assign fCat = facet.findDeepestCategory([key])!"" />
                <#if fCat != "">
                    <#list fCat.values as catValue>
                        <#-- Find the category value for which the query string param
                             matches the currently selected value -->
                        <#local kv = catValue.queryStringParam?split("=") />
                        <#if valueLabel == urlDecode(kv[1])>
                            <#local valueLabel = catValue.label />
                        </#if>
                    </#list>
                </#if>
            </#if>
        </#list>
    </#if>
  <${tag?html}><a href="${link}?${urlDecode(removeParam(facetScopeRemove(QueryString, key),["start_rank","duplicate_start_rank"]))?replace(key+"="+value,"")?replace("&+","&","r")?replace("&$","","r")?html}${urlHash}" title="Remove refinement - ${facetName?html}: ${valueLabel?html}" class="${class?html}"><#if !group>${facetName?html}: </#if>${valueLabel?html} <span class="glyphicon glyphicon-remove-circle"></span></a></${tag?html}>
  </#list>

        <#if group>
        <#if tag == "li" || tag == "LI">
          </ul>
        </#if>
        </${grouptag}>
  </#if>
 
</#list>
<#if question.inputParameterMap["duplicate_signature"]??>
  <#if group>
        <${grouptag} class="appliedFacets"> Duplicates
        <#if tag == "li" || tag == "LI">
          <ul>
        </#if>
  </#if>

  <#-- And lastly, one for the duplicate_signature -->
  <${tag?html}><a href="${link}?${urlDecode(removeParam(QueryString,["duplicate_signature","start_rank","duplicate_start_rank"]))?html}${urlHash}" title="Remove duplicate constraint" class="${class?html}">${question.inputParameterMap["duplicate_signature"]} <span class="glyphicon glyphicon-remove-circle"></span></a></${tag?html}>

  <#if group>
  <#if tag == "li" || tag == "LI">
    </ul>
  </#if>
  </${grouptag}>
  </#if>


</#if>
</#compress></#macro>

<#---
Displays a link that, when clicked, clears all the facets.

@param clearAllText Optional link text to display.
-->
<#macro ClearFacetsLink clearAllText="Clear all filters" class="clearFacetLink" title="Clear all filters" urlHash="" link=question.collection.configuration.value("ui.modern.search_link")>
<#if question.selectedCategoryValues?has_content || question.inputParameterMap["duplicate_signature"]??>
<a href="${link}?${removeParam(QueryString,question.selectedCategoryValues?keys + ["start_rank","duplicate_start_rank","facetScope","duplicate_signature"])?html}${urlHash}" title="${title}" class="${class}">${clearAllText?html} <span class="glyphicon glyphicon-remove-circle"></span></a>
</#if>
</#macro>
