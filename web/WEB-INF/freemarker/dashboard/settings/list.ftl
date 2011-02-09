<#include "../inc/header.ftl" />

<h1>Processing flow</h1>

<h2>Input Processors</h2>
<ol>
	<#list inputFlow as processor>
		<li>[<a href="input/${processor_index}/remove">remove</a>] ${processor.targetSource.target.class.name}</li>
	</#list>
</ol>

<h2>Data Fetchers</h2>
<ol>
	<#list dataFetchers as fetcher>
		<li>[<a href="data/${fetcher_index}/remove">remove</a>] ${fetcher.targetSource.target.class.name}</li>
	</#list>
</ol>

<h2>Output Processors</h2>
<ol>
	<#list outputFlow as processor>
		<li>[<a href="output/${processor_index}/remove">remove</a>] ${processor.targetSource.target.class.name}</li>
	</#list>
</ol>


<#include "../inc/footer.ftl" /> 