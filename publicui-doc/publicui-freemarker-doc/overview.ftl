<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />

    <link rel="stylesheet" href="../style-v2.css" type="text/css" />

    <title>Overview</title>
</head>

<body class="ftldoc">
<div id="content">

<#include "nav.ftl">

<h3>Overview</h3>

<br />

<table border="1" cellspacing="0" cellpadding="4">
    <tr>
        <th>Library</th>
        <th>Summary</th>
    </tr>

	<#list files as file>
	<tr>
		<td><a href="${file.filename}.html">${file.filename}</a></td>
		<td>${file.comment.short_comment?if_exists}&nbsp;</td>
	</tr>
	</#list>
</table>

</div>
</body>
</html>
