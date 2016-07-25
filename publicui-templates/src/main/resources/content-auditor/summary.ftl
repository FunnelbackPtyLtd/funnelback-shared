<#ftl encoding="utf-8" />
<#--
    JSON summary template for the content auditor data.
    Its output is saved during the update cycle and then used in the
    Marketing dashboard -->
{
    "updatedDate": ${.now?long?c},
    "facets":
<#if (response.facets)!?size &gt; 0>
    [
    <#assign x = 0> 
    <#list response.facets as f>
        <#if f.categories?size &gt; 0>
            <#if x == 1>,</#if>
            <#assign x = 1>
            {
                "name": "${f.name?json_string}",
                "categories": [
                    <#-- Content Auditor facets always have only one category -->
                    <#list f.categories[0].values as v>
                        { "category": "${v.label?json_string}", "count": ${v.count?c} }<#if v_has_next>,</#if>
                    </#list>
                ]
            }
        </#if>
    </#list>
    ]
<#else>
    [ ]
</#if>
}
