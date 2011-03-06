<#include "../inc/header.ftl" />

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

<#include "../inc/footer.ftl" />