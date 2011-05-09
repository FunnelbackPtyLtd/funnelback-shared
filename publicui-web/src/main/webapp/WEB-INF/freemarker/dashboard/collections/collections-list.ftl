<ul class="inline">
	<#list collections as collection>
		<li><a href="${contextPath}/dashboard/collections/${collection.id}/view">${collection.id}</a></li>
	</#list>
</ul>