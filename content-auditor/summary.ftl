<#ftl encoding="utf-8" strip_text="true" />
<#--
    JSON summary template for the content auditor data.
    Its output is saved during the update cycle and then used in the
    Marketing dashboard -->

<#if (response.facets)!?size &gt; 0>
    [
    <#list response.facets as f>
        <#if f.categories?size &gt; 0>
            {
                "${f.name?js_string}": {
                    <#-- Content Auditor facets always have only one category -->
                    <#list f.categories[0].values as v>
                        "${v.label?js_string}": ${v.count} <#if v_has_next>,</#if>
                    </#list>
                }
            }
            <#if f_has_next>,</#if>
        </#if>
    </#list>
    ]
<#else>
    [ ]
</#if>
