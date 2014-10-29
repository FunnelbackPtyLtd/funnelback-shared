<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>
<!--BEGINFACETS-->

<!-- RESULTS SUMMARY -->
<p>        
<#if response.resultPacket.resultsSummary.totalMatching == 0>
    No attributes are availiable as there are <strong class="fb-result-count" id="fb-total-matching">0</strong> search results for <strong><@s.QueryClean /></strong>
</#if>
<#if response.resultPacket.resultsSummary.totalMatching != 0>
    Attributes attached to the <strong class="fb-result-count" class="fb-total-matching">${response.resultPacket.resultsSummary.totalMatching?string.number}</strong>
    search results for <strong><@s.QueryClean /></strong>
</#if>
</p>

<!-- Collection: <@s.cfg>service_name</@s.cfg> | Attributes last updated: ${response.resultPacket.details.collectionUpdated?datetime} -->
<div id="fb-facet-graph"></div>

<@s.FacetedSearch>                        
<div id="fb-facets">
    <@s.Facet>
        <div class="facet-container">
        <div class="facet-header">
          <h4><@s.FacetLabel summary=false /></h4>
          <#assign assignFacetSummary><@s.FacetSummary /></#assign>
          <#if (assignFacetSummary?length > 11)>
            <p class="chosen-facets">chosen attributes <span>${assignFacetSummary?replace("&amp;type=facets","")}</span>&nbsp;</p>
          </#if>
          <span class="facet-graph"><a href="#fb-facet-graph" class="facet-graph-button">(View Graph)</a></span>
        </div>
        <@s.Category max=1000>    
          <#assign assignCategoryName><@s.CategoryName /></#assign>
          <span class="fb-facet-count"><span><@s.CategoryCount /></span></span>&nbsp;${assignCategoryName?replace("&amp;type=facets","")}
        </@s.Category>
        </div>
    </@s.Facet>
</div>
</@s.FacetedSearch>

<!--ENDFACETS-->

</@s.AfterSearchOnly>