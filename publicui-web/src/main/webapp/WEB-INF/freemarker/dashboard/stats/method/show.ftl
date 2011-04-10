<#include "../../inc/header.ftl" />

<h1>Profiling for <span class="method">${statsId}</span></h1>

<div id="graphdiv" style="width:700px"></div>

<script type="text/javascript">
  g = new Dygraph(

    // containing div
    document.getElementById("graphdiv"),

    // CSV or path to a CSV file.
    "Number,Time(ms)\n" +
    // ${statistics.count} stats
    <#list 0..statistics.count-1 as i>
		"${statistics.dates[i]?datetime?string("yyyy/MM/dd HH:mm:ss.SSS")},${statistics.values[i]?string("#")}\n" +
    </#list>
    ""
  );
</script>

<div id="chartdiv" style="text-align: center; margin:auto; height:400px;width:700px; "></div> 

<script type="text/javascript">
	jQuery.jqplot('chartdiv',
		[[
    		<#list 0..statistics.count-1 as i>
				['${statistics.dates[i]?datetime?string("yyyy-MM-dd HH:mm:ss.SSS")}',${statistics.values[i]?string("#")}]<#if i_has_next>,</#if>
    		</#list>
 		]],
 		{
 			legend: {show: false},
 			axes:{
 				xaxis:{
 					renderer:$.jqplot.DateAxisRenderer,
 					
 					tickOptions:{formatString: '%H:%M:%S.%N'}
 				},
				
 			},
 			highlighter: {show: true, sizeAdjust: 7.5},
 			cursor: {
 				show: true,  
				showVerticalLine:true,
				showHorizontalLine:false,
				showTooltip: false,
				zoom:true
			} 
 		}
	);
</script>


<#include "../../inc/footer.ftl" />