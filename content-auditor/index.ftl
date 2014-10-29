<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
<#import "/web/templates/modernui/content-auditor/main.ftl" as main />
<#import "/web/templates/modernui/content-auditor/design.ftl" as design />

<#--

audit.ftl

Intended for general use metadata auditing, link checking, collection debugging,
etc. 

Uses jQuery and Twitter Bootstap for styling and behaviour.

INSTRUCTIONS:
- Ensure the bootstrap CSS and JS files are located in your collection's resources folder
- Update the <s.InitialFormOnly> <@fb.IncludeUrl url> value to be hard-coded. 
- Modify sort drop-down, <thead>, <tfoot> and <tbody> placeholders to align with metadata mapping 
- Using the 'Subjects' example, adapt the character used for separating multi-value metadata fields

POTENTIAL IMPROVEMENTS:
- Table column sorting?
- Fix broken CSS image references
- Add Funnelback branding
- Expand query syntax output
- Server-side thumbnail re-sizing
- Display description metadata on hover
- Large number of facets can result in 'jagged' layout
- Fix RSS subscription service
- Determine optimal maximum facet category output values (currently hard-coded to 100)
- Include query completion

-->

<#-- Assign global variables -->

<#assign serviceName0>${question.inputParameterMap["collection"]!?html}</#assign>
<#assign serviceName1>TODO</#assign>
<#assign serviceName2>TODO</#assign>
<#assign serviceName3>TODO</#assign>

<#assign collectionId0>${question.inputParameterMap["collection"]!?html}</#assign>
<#assign collectionId1>TODO</#assign>
<#assign collectionId2>TODO</#assign>
<#assign collectionId3>TODO</#assign>

<!DOCTYPE html>
<html lang="en-us">

<#-- Start:Macro: main.ftl - Head -->
<@design.Head />
<#-- End:Macro: main.ftl - Head -->

<@s.InitialFormOnly>
<body class="initial-form">
</@s.InitialFormOnly>

<@s.AfterSearchOnly>
<body class="after-search">
</@s.AfterSearchOnly>

<#-- Start:Macro: main.ftl - Header -->
<@design.Header />
<#-- End:Macro: main.ftl - Header -->

<@s.InitialFormOnly>
<#-- Start:Macro: main.ftl - InitialTabs -->
<@main.InitialTabs />
<#-- End:Macro: main.ftl - InitialTabs -->
</@s.InitialFormOnly>

<@fb.ErrorMessage />

<@s.AfterSearchOnly>
<#-- Start:Macro: main.ftl - ResultTabs -->
<@main.ResultTabs />
<#-- End:Macro: main.ftl - ResultTabs -->
</@s.AfterSearchOnly>

<#-- Start:Macro: main.ftl - FooterScripts -->
<@design.FooterScripts />
<#-- End:Macro: main.ftl - FooterScripts -->

</body>
</html>