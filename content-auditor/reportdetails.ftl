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
    <p>
	<span class="text-muted">Scoping query:</span><strong> ${queryToReport?html}</strong> &nbsp;<span class="text-muted">  </span>&nbsp; <span class="text-muted">Last updated:</span> <strong>${response.resultPacket.details.collectionUpdated?datetime}</strong></p>
	<p></p>
    <p><span class="data-total-doc-count" data-value="<#if response.resultPacket.resultsSummary.totalMatching != 0>${response.resultPacket.resultsSummary.totalMatching?string.number?replace(',','')}<#else>0</#if>"><span class="text-muted">Total documents:</span></span> <#if response.resultPacket.resultsSummary.totalMatching != 0><strong>${response.resultPacket.resultsSummary.totalMatching?string.number}</strong><#else><strong class="text-danger">Unable to retrieve document count.</strong> </#if></p>

    <#-- applied facets block -->
    <#if question.selectedCategoryValues?has_content>
        <@AppliedFacets class="btn btn-xs btn-warning" group=true/>
    </#if>
  </div>
</div>
<!--ENDREPORTDETAILS-->
</@s.AfterSearchOnly>

<#---
Displays all the currently applied facets
 
@param group Group categories by facet. (def=false)
@param class Optional CSS class.
-->
<#macro AppliedFacets class="applied-facets" tag="li" group=false grouptag="div"><#compress>
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
 
  <${tag?html}><a href="${question.collection.configuration.value("ui.modern.search_link")}?${urlDecode(removeParam(facetScopeRemove(QueryString, key),["start_rank"]))?replace(key+"="+value,"")?replace("&+","&","r")?replace("&$","","r")?html}" title="Remove refinement - ${facetName?html}: ${valueLabel?html}" class="${class?html}"><#if !group>${facetName?html}: </#if>${valueLabel?html} <span class="glyphicon glyphicon-remove-circle"></span></a></${tag?html}>
  </#list>
 
        <#if group>
        <#if tag == "li" || tag == "LI">
          </ul>
        </#if>
        </${grouptag}>
  </#if>
 
</#list>
</#compress></#macro>