<#include "../inc/header.ftl" />

<table>
<tr>
	<th>Name</th>
	<th>Values</th>
	<th>Average</th>
	<th>Last</th>
	<th>Actions</th>
</tr>

<#assign keys = statistics?keys>

<#list keys as key>
	<#assign stats = statistics[key]>
	<tr>
		<td>${key}</td>
		<td>
			<#assign avg=0>
			<span class="sparklines">
				<#assign max=10>
				<#if stats.count < 10>
					<#assign max=stats.count>
				</#if>
				<#list max..1 as i>
					${stats.values[stats.count-i]}
					<#if i_has_next>,</#if>
					<#assign avg=avg+stats.values[stats.count-i]>
				</#list>
			</span>
		</td>
		<td>
			${(avg/stats.count)?string("0.##")}ms
		</td>
		<td>
			${stats.values[stats.count-1]}ms
		</td>
		<td><a href="/publicui/dashboard/stats/${key}/show">show</a></td>
		
	</tr>	
</#list>
</table>

<div id="test"></div>

<script type="text/javascript">
	$('.sparklines').sparkline(
		'html',
		{
			type: 'line',
			width: '100px'
		});
</script>
<#include "../inc/footer.ftl" /> 