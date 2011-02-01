<#include "../inc/header.ftl" />

<h1>Profiling for <span class="method">${statsId}</span></h1>

<div id="graphdiv" style="width:700px"></div>

<script type="text/javascript">
  g = new Dygraph(

    // containing div
    document.getElementById("graphdiv"),

    // CSV or path to a CSV file.
    "Number,Time(ms)\n" +
    <#list statistics as value>
		<#if value?exists>
			"${value_index},${value?string("#")!"0"}\n" +
		<#else>
			"${value_index},0\n" +
		</#if>
    </#list>
    ""
  );
</script>

<#include "../inc/footer.ftl" />