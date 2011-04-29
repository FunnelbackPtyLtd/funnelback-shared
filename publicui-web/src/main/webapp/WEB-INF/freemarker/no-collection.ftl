<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	
	<style type="text/css">
		body {
			font-family: Verdana;
			text-align: center;
		}

		ul {
			list-style-type: none;
			margin: 0;
			padding: 0;
		}
		
		li {
			margin: 1em;
			padding: 0;
		}		
	</style>
	
		<title>Funnelback Search</title>
</head>

<body>

	<a href="http://www.funnelback.com/">
		<img src="/search/funnelback.png" alt="Funnelback Enterprise Search" />
	</a>
	
	<p>Welcome to the Funnelback search service.</p>

	<p>${AllCollections?size} collections:</p>
	
	<ul>
		<#list AllCollections as oneCollection>
			<li><a href="?collection=${oneCollection.id}">Search ${oneCollection.configuration.value("service_name")}</a></li>
		</#list>
	</ul>

</body>

</html>
