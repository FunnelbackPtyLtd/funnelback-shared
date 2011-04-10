<#include "../../inc/header.ftl" />

<h1>Searches stats</h1>

<#assign keys = statistics?keys>

<#list keys as key>
	${key}: ${statistics[key]}<br />
</#list>

<div id="chartdiv" style="text-align: center; margin:auto; height:500px;width:800px; "></div> 

<script type="text/javascript">
	jQuery.jqplot('chartdiv',
		[[
			<#list keys as key>	
				${statistics[key]}<#if key_has_next>,</#if>
			</#list>
		]],
		{
			title: 'Collection distribution',
			seriesDefaults:{renderer:jQuery.jqplot.BarRenderer},
			series: [ {label:'Searches'} ],
			axes:{
				xaxis:{
					renderer:jQuery.jqplot.CategoryAxisRenderer, 
					ticks:[
						<#list keys as key>
							'${key}'<#if key_has_next>,</#if>
						</#list>
					]
				}, 
				yaxis:{min:0}
			}, 
			highlighter: {show: true}
		}
	);
</script>

<#include "../../inc/footer.ftl" /> 