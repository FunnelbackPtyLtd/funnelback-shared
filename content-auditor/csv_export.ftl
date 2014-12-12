<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
<@compress single_line=true>
    Page, URL
    <#list response.customData.displayMetadata?values as value>
        <#assign heading = value?replace("^\\d*\\.","","r")>
        ,${heading?replace(",", " ")}
    </#list>
</@compress>

<@s.Results>
    <#if s.result.class.simpleName != "TierBar">
        <@compress single_line=true>
            ${s.result.title?replace(",", " ")}
            ,
            ${s.result.liveUrl?replace(",", "%2C")}
            <#list response.customData.displayMetadata?keys as key>
                ,
                <#if key == "d">
                    <#-- Special-case date -->
                    <#if s.result.date??>
                        ${s.result.date?date?string("dd MMM yyyy")}                    
                    <#else>
                        No Date
                    </#if>
                <#elseif key == "f">
                    <#-- Special-case format -->
                    <#if s.result.fileType??>
                        ${s.result.fileType}                    
                    </#if>
                <#else>
                    <#if s.result.metaData[key]??>
                        ${s.result.metaData[key]?replace(",", " ")}
                    </#if>
                </#if>
            </#list>
        </@compress>
        
    </#if>
</@s.Results>