<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>
<!--BEGINREPORTDETAILS-->
<!-- reportdetails.ftl -->

<!-- Collection: <@s.cfg>service_name</@s.cfg> | Attributes last updated: ${response.resultPacket.details.collectionUpdated?datetime} -->

<div class="tab-summary">
  <div class="inner">
    <h3>Report details</h3>
    <p><span>Scoping query:</span> ${response.resultPacket.queryAsProcessed?html}</p>
    <p><span>Last updated:</span> ${response.resultPacket.details.collectionUpdated?datetime}</p>
    <p><span class="data-total-doc-count" data-value="<#if response.resultPacket.resultsSummary.totalMatching != 0>${response.resultPacket.resultsSummary.totalMatching?string.number?replace(',','')}<#else>0</#if>">Total document count:</span> <#if response.resultPacket.resultsSummary.totalMatching != 0>${response.resultPacket.resultsSummary.totalMatching?string.number}<#else>There was a problem retrieving the document count</#if></p>
    <div id="metadata-health-chart" class="bar-chart"></div>
    <div id="content-age-chart" class="pie-chart"></div>
  </div>
</div>
    
<!--ENDREPORTDETAILS-->
</@s.AfterSearchOnly>