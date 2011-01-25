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
				<#assign sze=stats?size>
				<#if sze < max>
					<#assign max = sze>
				</#if>
				<#list max..1 as i>
					<#assign value = stats[sze-i]!0>
					<#assign avg=avg+value>
					${value}
					<#if i&gt; 1>,</#if>
				</#list>
			</span>
		</td>
		<td>
			${(avg/stats?size)?string("0.##")}ms
		</td>
		<td>
			${stats?last!"Unknown (?) "}ms
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