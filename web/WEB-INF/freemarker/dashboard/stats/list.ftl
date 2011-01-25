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
<tr>
	<td>${key}</td>
	<td>
		<#assign avg=0>
		<span class="sparklines">
			<#assign max=10>
			<#assign sze=statistics[key]?size-1>
			<#if sze < max>
				<#assign max = sze>
			</#if>
			<#list max..1 as i>
				
				<#assign value = statistics[key][sze-i]>
				<#assign avg=avg+value>
				${value}
				<#if i&gt; 1>,</#if>
			</#list>
		</span>
	</td>
	<td>
		${(avg/statistics[key]?size)?string("0.##")}ms
	</td>
	<td>
		${statistics[key][sze]!"Unknown (?) "}ms
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