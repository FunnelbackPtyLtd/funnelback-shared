<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
Title , URL , Date , DC.Format , Detected Format , Subjects
<@s.Results>
    <#if s.result.class.simpleName != "TierBar">
        <@compress single_line=true>
            ${s.result.title?replace(",", " ")}
            ,
            ${s.result.liveUrl?replace(",", "%2C")}
            ,
            <#if s.result.date??>
                ${s.result.date?date?string("dd MMM yyyy")}
            <#else>
                No Date
            </#if>
            ,
            <#if s.result.metaData["f"]??>
                ${s.result.metaData["f"]}
            <#else>
                None
            </#if>
            ,
            <#if s.result.fileType??>
                ${s.result.fileType}
            <#else>
                Unknown
            </#if>
            ,
            <#if s.result.metaData["s"]??>
                <#list s.result.metaData["s"]?split(";") as value>
                    <#if (value?length > 0)>
                        ${value?replace(",", " ")}
                    </#if>
                </#list>  
            <#else>
                None
            </#if>
        </@compress>

    </#if>
</@s.Results>