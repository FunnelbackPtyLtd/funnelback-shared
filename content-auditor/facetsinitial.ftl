<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>
<!--BEGINFACETS-->

<!-- Collection: <@s.cfg>service_name</@s.cfg> | Attributes last updated: ${response.resultPacket.details.collectionUpdated?datetime} -->
<div id="fb-facet-graph"></div>

<@s.FacetedSearch>                        
    <div id="fb-facets-<@s.cfg>collection</@s.cfg>">
        <@s.Facet>
            <div class="facet-container">
            <div class="facet-header">
              <h4><@s.FacetLabel summary=false /><@s.FacetSummary /></h4>
              <span class="facet-graph"><a href="#fb-facet-graph" class="facet-graph-button">(View Graph)</a></span>
            </div>
            <@s.Category max=1000>    
                <#assign assignCategoryName><@s.CategoryName /></#assign>
                <span class="fb-facet-count"><span><@s.CategoryCount /></span></span>&nbsp;${assignCategoryName?replace("&amp;type=facets","")}
            </@s.Category>
            <@s.MoreOrLessCategories />
            </div>
        </@s.Facet>
    </div>
</@s.FacetedSearch>

<!--ENDFACETS-->

</@s.AfterSearchOnly>