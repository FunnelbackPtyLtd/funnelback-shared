<#ftl encoding="utf-8" />
<#import "modelDump.ftl" as dumper>
<#compress>
	<html>
		<body>
		<#assign foo = response />
			<pre>
				<@dumper.dump foo />
			</pre>
		</body>
	</html>
</#compress >