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
		}
		
		li {
			margin: 1em;
		}		
	</style>
	
		<title>Funnelback Search</title>
</head>

<body>

	<a href="http://www.funnelback.com/">
		<img src="/search/funnelback.png" alt="Funnelback Enterprise Search" />
	</a>
	
	<p>Welcome to the Funnelback search service.</p>

	<p>${allCollections?size} collections:</p>
	
	<ul>
		<#list allCollections as oneCollection>
			<li><a href="?collection=${oneCollection.id}">Search ${oneCollection.configuration.value("service_name")}</a></li>
		</#list>
	</ul>

</body>

</html>
