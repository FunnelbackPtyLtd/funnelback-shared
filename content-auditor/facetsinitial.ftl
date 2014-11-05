<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>
<!--BEGINFACETS-->

<!-- Collection: <@s.cfg>service_name</@s.cfg> | Attributes last updated: ${response.resultPacket.details.collectionUpdated?datetime} -->
<div id="fb-facet-graph"></div>

<#assign facet_counter = 0 />

<@s.FacetedSearch>                        
    <div id="fb-facets-<@s.cfg>collection</@s.cfg>">
        <@s.Facet>
            <div class="facet-container">
            <div class="facet-header">
              <h4><@s.FacetLabel summary=false /><@s.FacetSummary /></h4>
            </div>
            <@s.Category max=1000>    
                <#assign assignCategoryName><@s.CategoryName /></#assign>
                <span class="fb-facet-count"><span><@s.CategoryCount /></span></span>&nbsp;${assignCategoryName?replace("&amp;type=facets","")}
            </@s.Category>
            <@s.MoreOrLessCategories />
            </div>
            <#assign other_counter = response.resultPacket.resultsSummary.totalMatching />
            <div id="facet-chart-${facet_counter}" style="width : 100%; height : 500px; font-size : 11px;"></div>
            <script type="text/javascript">
                var chart = AmCharts.makeChart("facet-chart-${facet_counter}", {
                    "type": "pie",
                    "dataProvider": [
                      <#list s.facet.categories as c><#list c.values as cv>
                      {
                        "label": "${cv.label}",
                        "count": "${cv.count?c}"<#assign other_counter = other_counter - cv.count />
                      }
                      <#if cv_index &gt; 5>
                        <#if other_counter &gt; 0>
                        ,
                        {
                          "label": "Other",
                          "count": "${other_counter?c}"
                        }
                        </#if>
                        <#break>
                      </#if>
                      <#if cv_has_next>,</#if>
                      </#list></#list>
                    ],
                    "valueField": "count",
                    "titleField": "label"
                  }
                );
            </script>
            <#assign facet_counter = facet_counter + 1 />        </@s.Facet>
    </div>
</@s.FacetedSearch>

<!--ENDFACETS-->

</@s.AfterSearchOnly>