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
	<span class="text-muted">Scoping query:</span><strong> ${response.resultPacket.queryAsProcessed?html}</strong> &nbsp;<span class="text-muted">  </span>&nbsp; <span class="text-muted">Last updated:</span> <strong>${response.resultPacket.details.collectionUpdated?datetime}</strong></p>
	<p></p>
    <p><span class="data-total-doc-count" data-value="<#if response.resultPacket.resultsSummary.totalMatching != 0>${response.resultPacket.resultsSummary.totalMatching?string.number?replace(',','')}<#else>0</#if>"><span class="text-muted">Total documents:</span></span> <#if response.resultPacket.resultsSummary.totalMatching != 0><strong>${response.resultPacket.resultsSummary.totalMatching?string.number}</strong><#else><strong class="text-danger">Unable to retrieve document count.</strong> </#if></p>
    <div id="metadata-health-chart" class="bar-chart"></div>
    <div id="content-age-chart" class="pie-chart"></div>
  </div>
</div>
<!--ENDREPORTDETAILS-->
</@s.AfterSearchOnly>