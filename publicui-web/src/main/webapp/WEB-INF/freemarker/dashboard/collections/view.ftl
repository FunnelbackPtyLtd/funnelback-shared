<#include "../inc/header.ftl" />

<#include "collections-list.ftl" />

<h1>${collection.id} <span>(${collection.configuration.value('service_name')})</span></h1>

<table>
<tr>
	<th>Key</th>
	<th>Value</th>
</tr>
<#list collection.configuration.valueKeys()?sort as key>
	<tr>
		<td>${key}</td>
		<td>${collection.configuration.value(key)}</td>
	</tr>
</#list>
</table>


<#include "../inc/footer.ftl" /> 