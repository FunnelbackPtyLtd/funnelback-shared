...

<#if SearchTransaction.question?exists && SearchTransaction.question.collection?exists>
  <title>${SearchTransaction.question.collection.id}, Funnelback Search</title>
<#else>
  <title>Funnelback Search</title>
</#if>

...


<ol id="fb-results">
<#list SearchTransaction.response.resultPacket.results as result>
  <li>
    <#if SearchTransaction.question.collection.configuration.valueAsBoolean("click_tracking")>
      <h3><a href="${result.clickTrackingUrl}" title="${result.liveUrl}">${result.title?html}</a></h3>
    <#else>
      <h3><a href="${result.liveUrl}">${result.title?html}</a></h3>
    </#if>

    <p>
      <#if result.date?exists><span class="fb-date">${result.date?date?string.medium}:</span></#if>
      <span class="fb-summary">${result.summary}</span>
    </p>
			
    ...
           
    <#if result.quickLinks?exists>
      <ul class="fb-quicklinks">
        <#list result.quickLinks.quickLinks as ql> 
        <li><a href="http://${ql.url?url}" title="${ql.text}">${ql.text?html}</a></li>
        </#list> 
      </ul> 
    </#if>

    ...

  </li>
</#list>
</ol>