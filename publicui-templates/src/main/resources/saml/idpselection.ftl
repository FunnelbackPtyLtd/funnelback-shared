<#ftl output_format="HTML">
<!DOCTYPE html>
<html>
    <head>
        <title>Please select your identity provider</title>
    </head>
    <body>
        <h1>Please select your identity provider:</h1>
		<form action="${idpDiscoReturnURL}" method="get">
			<#list idps as idp>
				<div>
					<input type="radio" name="${idpDiscoReturnParam}" id="idp_${idp}" value="${idp}" />
					<label for="idp_${idp}">${idp}</label>
				</div>
			</#list>
			<p>
				<input type="submit" value="Login" />
			</p>
		</form>
    </body>
</html>