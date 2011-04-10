<#include "../../inc/header.ftl" />

<h1>Method Stats</h1>

<table>
<tr>
	<th>Name</th>
	<th>Values</th>
	<th>Average</th>
	<th>Last</th>
	<th>Peak</th>
	<th>Actions</th>
</tr>

<#assign keys = statistics?keys>

<#list keys as key>
	<#assign stats = statistics[key]>
	<tr>
		<td>${key}</td>
		<td class="sparklines">
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
		<td class="value">${(avg/stats.count)?string("0.##")}ms</td>
		<td class="value">${stats.values[stats.count-1]}ms</td>
		<td class="value">${stats.peakValue}ms</td>
		<td class="action"><a href="${key}/show">show</a></td>
		
	</tr>	
</#list>
</table>

<div id="test"></div>

<script type="text/javascript">
	$('span.sparklines').sparkline(
		'html',
		{
			type: 'line',
			width: '100px',
			height: '18px'
		});
</script>
<#include "../../inc/footer.ftl" /> 