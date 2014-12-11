<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
<#import "/web/templates/modernui/content-auditor/main.ftl" as main />
<#import "/web/templates/modernui/content-auditor/design.ftl" as design />

<!DOCTYPE html>
<html lang="en-us">

	<#-- Start:Macro: main.ftl - Head -->
	<@design.Head />
	<#-- End:Macro: main.ftl - Head -->
	<body class="after-search<#if layoutSideBar !=1> sidebar-closed sidebar-off</#if>">
		<#-- TODO - Steve, is that class still needed? We now guarantee there is always a search in content auditor. -->

	<#-- Start:Macro: main.ftl - Header -->
	<@design.Header />
	<#-- End:Macro: main.ftl - Header -->
	
	<@fb.ErrorMessage />

	<@s.AfterSearchOnly>
	<#-- Start:Macro: main.ftl - ResultTabs -->
	<@main.ResultTabs />
	<#-- End:Macro: main.ftl - ResultTabs -->
	
	</@s.AfterSearchOnly>
	
	<@design.modalOverlay />
	<#-- Start:Macro: main.ftl - FooterScripts -->
	<@design.FooterScripts />
	<#-- End:Macro: main.ftl - FooterScripts -->
	
</body>
</html>