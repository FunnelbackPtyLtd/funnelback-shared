<#include "../inc/header.ftl" />

<h1>Processing flow</h1>


<h2>Input Processors</h2>
<form action="input/add">
	Add a
	<select name="clazz">
		<option value="com.funnelback.publicui.search.lifecycle.input.processors.QuickLinks">QuickLinks</option>
	</select>
	&nbsp; processor at position <input type="text" name="index" value="0" size="2"/>
	<input type="submit" value="ok" />
</form>
<ol>
	<#list inputFlow as clazz>
		<li>[<a href="input/${clazz_index}/remove">remove</a>] ${clazz.name}</li>
	</#list>
</ol>

<h2>Data Fetchers</h2>
<ol>
	<#list dataFetchers as clazz>
		<li>[<a href="data/${clazz_index}/remove">remove</a>] ${clazz.name}</li>
	</#list>
</ol>

<h2>Output Processors</h2>
<form action="output/add">
	Add a
	<select name="clazz">
		<option value="com.funnelback.publicui.search.lifecycle.output.processors.FixPseudoLiveLinks">FixPseudoLiveLinks</option>
	</select>
	&nbsp; processor at position <input type="text" name="index" value="0" size="2"/>
	<input type="submit" value="ok" />
</form>
<ol>
	<#list outputFlow as clazz>
		<li>[<a href="output/${clazz_index}/remove">remove</a>] ${clazz.name}</li>
	</#list>
</ol>


<#include "../inc/footer.ftl" /> 