<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />

    <link rel="stylesheet" href="../help/style-v2.css" type="text/css" />

    <title>Index (categories)</title>

    <script language="javascript">
        function setTitle() { parent.document.title=document.title; }
    </script>
</head>

<body class="ftldoc" onLoad="setTitle();">
<div id="content">
<#include "nav.ftl">

<table border="1" cellspacing="0" cellpadding="4">
<#list categories?keys as category>
    <#if categories[category]?has_content>
        <tr><td colspan="3" class="heading">
        <#if category?has_content>
            Category <em>${category}</em>
        <#else>
            Uncategorized
        </#if>
        </td></tr>

        <tr>
            <td class="category">Name</td>
            <td class="category">Description</td>
            <td class="category">File</td>
        </tr>

        <#list categories[category] as macro>
            <tr>
                <td>
                    ${macro.type?cap_first} <a href="${macro.filename}.html#${macro.name}">${macro.name}</a>
                </td>
                <td>${macro.short_comment!""}</td>
                <td><a href="${macro.filename}.html">${macro.filename}</a></td>
            </tr>
        </#list>
    </#if>
</#list>
<table>

</div>
</body>
</html>
