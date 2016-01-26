<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />

    <link rel="stylesheet" href="stylesheet.css" type="text/css" />

    <title>File list</title>
</head>

<body class="ftldoc">
<div id="content">

<h3>Macro Libraries</h3>

<p><a href="overview.html" target="main">Overview</a></p>

<#list files as file>
    <a href="${file.name}${suffix}" target="main">${file.name}</a><br />
</#list>

</div>
</body>

</html>
