<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />

    <link rel="stylesheet" href="stylesheet.css" type="text/css" />

    <title>Index (alphabetical)</title>

    <script language="javascript">
        function setTitle() { parent.document.title="ftldoc - Index"; }
    </script>
</head>

<body class="ftldoc" onLoad="setTitle();">
<div id="content">
<#include "nav.ftl">

<#assign lastLetter = "" />

<ul class="letters">
    <#list macros as macro>
        <#if macro.name[0]?cap_first != lastLetter>
            <#assign lastLetter = macro.name[0]?cap_first />

            <li><a href="#${lastLetter}">${lastLetter}</a></li>
        </#if>
    </#list>
</ul>

<hr />
 
<#assign lastLetter = "" />
<#list macros as macro>

    <#if macro.name[0]?cap_first != lastLetter>
        <#assign lastLetter = macro.name[0]?cap_first />
        <a name="${lastLetter}" /><h2>${lastLetter}</h2>
    </#if>

    ${macro.type?cap_first} <a href="${macro.filename}.html#${macro.name}">${macro.name}</a>
    <#if macro.short_comment?exists>: ${macro.short_comment}</#if>
    <br />
</#list>
                
</div>
</body>
</html>
