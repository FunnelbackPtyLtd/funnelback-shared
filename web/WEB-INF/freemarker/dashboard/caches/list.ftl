<#include "../inc/header.ftl" />

<table>
<tr>
	<th>Name</th>
	<th>Elements</th>
	<th>Actions</th>
</tr>

<#list statistics as stats>
<tr>
	<td>${stats.associatedCache.name}</td>
	<td>
		<table>
		<tr>
			<th>Name</th>
			<th>Creation time</th>
			<th>Actions</th>
		</tr>		
		<#list stats.associatedCache.keys as key>
			<tr>
				<th>${key}</th>
				<td>${stats.associatedCache.get(key).creationTime}</td>
				<td><a href="/publicui/dashboard/caches/${stats.associatedCache.name}/remove/${key}/">Remove</a></td>
			</tr>
		</#list>

		</table>
	</td>
	<td><a href="/publicui/dashboard/caches/${stats.associatedCache.name}/flush/">Flush</a></td>
</tr>	
</#list>

<#include "../inc/footer.ftl" /> 