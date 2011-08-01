<#import "ftl_highlight.ftl" as ftl>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />

    <link rel="stylesheet" href="../help/style-v2.css" type="text/css" />

    <title>Documentation for ${filename}</title>

    <style>
        div.sourcecode {
            display: none;
            border : 1px solid Black;
            background-color : #DDDDDD; /* #E8E8E8; */
            padding : 3px;
            margin-top : 8px;
        }


        span {font-family:Courier; font-size:12px}
        span.directive {color:blue}
        span.userdirective {color:red}
        span.interpolation {color:green}
        span.textblock {color:black}
        span.comment {color:brown}
    </style>

    <script language="javascript">
        function toggle(id) {
            elem = document.getElementById(id);
            if(elem.style.display=="block") {
                elem.style.display="none";
            } else {
                elem.style.display="block";
            }
        }

        function setTitle() {
            parent.document.title=document.title;
        }
    </script>
</head>

<body class="ftldoc" onLoad="setTitle();">
<div id="content">

<#include "nav.ftl">

<#-- start prolog -->
<h1>${filename}</h1>
<div id="page-contents">
    <h3>Description</h3><br />
    <#if comment.comment?has_content>
        ${comment.comment}
    </#if>
    <#if comment.@author?exists || comment.@version?exists>
        <dl>
            <@printOptional comment.@author?if_exists, "Author" />
            <@printOptional comment.@version?if_exists, "Version" />
        </dl>
    </#if>
</div>
<#-- end prolog -->

<#-- start summary -->
<table border="1" cellspacing="0" cellpadding="4">
    <tr>
        <td colspan="2" class="heading">Macro and Function Summary</td>
    </tr>

    <#list categories?keys as category>
        <#if categories[category]?has_content>
            <tr>
                <td colspan="2" class="category">
                <#if category?has_content>
                    Category <em>${category}</em>
                <#else>
                    Uncategorized
                </#if>
                </td>
            </tr>

            <#list categories[category] as macro>
                <tr>
                    <td>
                        <code>${macro.type}</code>
                    </td>
                    <td>
                        <dl>
                            <dt>
                                <code>
                                    <strong><a href="#${macro.name}">${macro.name}</a></strong>
                                </code>
                                <@signature macro />

                            </dt>
                            <dd>
                                ${macro.short_comment?if_exists}
                            </dd>
                        </dl>
                    </td>
                </tr>
            </#list>
            </#if>
        </#list>
</table>

<#-- end summary -->
<br />
<#-- start details -->

<table border="1" cellpadding="4" cellspacing="0">
    <tr><td colspan="2" class="heading">Macro and Function Detail</td></tr>
</table>

<#list macros as macro>
    <dl>
        <dt>
            ${macro.type} 
            <tt><a name="${macro.name}">${macro.name}</a></tt>
            <@signature macro />
            <br /><br />
        </dt>
        <dd>
            <#if macro.comment?has_content>
                ${macro.comment}
            </#if>
            <dl>
                <@printParameters macro />
                <@printOptional macro.@nested?if_exists, "Nested" />
                <@printOptional macro.@return?if_exists, "Return value" />
                <@printOptional macro.category?if_exists, "Category" />
                <@printSourceCode macro />
            </dl>
        </dd>
    </dl>
    <#if macro_has_next><hr></#if>
</#list>

<#-- end details -->

</div>
</body>
</html>

<#macro printParameters macro>
<#if macro.@param?has_content>
<dt><b>Parameters</b></dt>
<dd>
<#list macro.@param as param>
<code>${param.name}</code> - ${param.description}<br>
</#list>
</dd>
</#if>
</#macro>

<#macro printSourceCode macro>
<dt><a href="javascript:toggle('sc_${macro.name}');">Source Code</a></dt>
<dd>
<div class="sourcecode" id="sc_${macro.name}">
<@ftl.print root=macro.node/>
</div>
</dd>
</#macro>

<#macro printOptional value label>
<#if value?has_content>
<dt><b>${label}</b></dt>
<dd>${value}</dd>
</#if>
</#macro>

<#macro signature macro>
    <#if macro.isfunction>
        (
        <#list macro.arguments as argument>
            <code>${argument}</code>
            <#if argument_has_next>,</#if>
        </#list>
        )
    <#else>
        <#list macro.arguments as argument>
            <code>${argument}</code>
        </#list>
    </#if>
</#macro>
