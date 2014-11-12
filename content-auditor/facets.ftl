<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>
<!--BEGINFACETS-->

<!-- RESULTS SUMMARY -->
<p>        
<#if response.resultPacket.resultsSummary.totalMatching == 0>
    No attributes are availiable as there are <strong class="fb-result-count" id="fb-total-matching">0</strong> search results for <strong>${response.resultPacket.queryAsProcessed?html}</strong>
</#if>
<#if response.resultPacket.resultsSummary.totalMatching != 0>
    Attributes attached to the <strong class="fb-result-count" class="fb-total-matching">${response.resultPacket.resultsSummary.totalMatching?string.number}</strong>
    search results for <strong>${response.resultPacket.queryAsProcessed?html}</strong>
</#if>
</p>

<!-- Collection: <@s.cfg>service_name</@s.cfg> | Attributes last updated: ${response.resultPacket.details.collectionUpdated?datetime} -->
<div id="fb-facet-graph"></div>

<#assign facet_counter = 0 />

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
        </div>
        <@s.Category max=1000>    
          <#assign assignCategoryName><@s.CategoryName /></#assign>
          <span class="fb-facet-count"><span><@s.CategoryCount /></span></span>&nbsp;${assignCategoryName?replace("&amp;type=facets","")}
        </@s.Category>
        </div>
            <#assign other_counter = response.resultPacket.resultsSummary.totalMatching />
            <div id="facet-chart-${facet_counter}" style="width : 100%; height : 500px; font-size : 11px;"></div>
            <script type="text/javascript">
                var chart = AmCharts.makeChart("facet-chart-${facet_counter}", {
                    "type": "pie",
                    "dataProvider": [

                      <#assign categoriesToList = s.facet.categories>
                      <#if (s.facet.categories[0].categories?size > 0)>
                        <#assign categoriesToList = s.facet.categories[0].categories />
                      </#if>

                      <#assign separator = ''>
                      <#assign counter = 0>
                      <#list categoriesToList as c>
                        <#list c.values as cv>
                          <#if cv.data != "d" || cv.label?matches("\\d*|Uncertain")>

                            ${separator}
                            {
                              "label": "${cv.label?js_string}",
                              "count": "${cv.count?c}"<#assign other_counter = other_counter - cv.count />
                            }

                            <#assign separator = ','>
                            
                            <#if counter &gt; 5>
                              <#if other_counter &gt; 0>
                              ${separator}
                              {
                                "label": "Other",
                                "count": "${other_counter?c}"
                              }
                              </#if>
                              <#break>
                            <#else>
                              <#assign counter = counter + 1>
                            </#if>
                          </#if>
                        </#list>
                      </#list>
                    ],
                    "valueField": "count",
                    "titleField": "label"
                  }
                );
            </script>
            <#assign facet_counter = facet_counter + 1 />
    </@s.Facet>
</div>
</@s.FacetedSearch>

<!--ENDFACETS-->

</@s.AfterSearchOnly>