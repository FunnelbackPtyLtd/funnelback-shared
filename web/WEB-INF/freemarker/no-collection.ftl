<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	
	<style type="text/css">
		body {
			font-family: Verdana;
		}
		
		div.error {
			background-color: #ffcccc;
		}
	</style>
	
		<title>Funnelback Search</title>
</head>

<body>

<p>${allCollections?size} collections</p>

<ul>
	<#list allCollections as oneCollection>
		<li><a href="?collection=${oneCollection.id}">Search ${oneCollection.id}</a></li>
	</#list>
</ul>

</body>

</html>
