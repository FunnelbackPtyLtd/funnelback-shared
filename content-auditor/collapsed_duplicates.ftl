<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>

	<#list filterList(response.resultPacket.results, 'collapsed')?sort_by(['collapsed','count'])?reverse as result>
        <p>
        	<a class="search-collapsed" href="?${QueryString}&amp;s=%3F:${result.collapsed.signature}&amp;fmo=on&amp;collapsing=off#collection-1-tab-2">
        		 ${result.title?html} (${result.liveUrl?html}) has ${result.collapsed.count} duplicate<#if result.collapsed.count != 1>s</#if>.
    		</a>
		</p>
	</#list>


</@s.AfterSearchOnly>