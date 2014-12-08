<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<@s.AfterSearchOnly>


<div class="facet-container col-md-4 boxed">
	<div class="facet-header">
		<h3>Duplicate Content</h3>
		<p class="text-muted"><span class="fa fa-exclamation-triangle "></span> The following pages need to be fixed</p>
	</div>
<table id="duplicates" class="table table-striped">
	<thead>
		<tr>
			<th><!--Count--></th>		
			<th><!--Page--></th>
		</tr>
	</thead>
	<tbody>
		<#list filterList(response.resultPacket.results, 'collapsed')?sort_by(['collapsed','count'])?reverse as result>
		<tr>
				<td class="text-center">
					<a class="text-muted duplicates-count" href="?${QueryString}&amp;s=%3F:${result.collapsed.signature}&amp;fmo=on&amp;collapsing=off#collection-1-tab-2"> <div>x <strong>${result.collapsed.count}</div></strong> </td>
				<td>
				<div class="pull-left">
						<a href="?${QueryString}&amp;s=%3F:${result.collapsed.signature}&amp;fmo=on&amp;collapsing=off#collection-1-tab-2" title="${result.title?html}" class="clickable-link"><strong>${result.title?html} </strong></a> 
							<span class="fa fa-open"></span>
							<br>
							<!-- SITE (Z) -->
							<a class="text-muted" href="?${QueryString}&amp;s=%3F:${result.collapsed.signature}&amp;fmo=on&amp;collapsing=off#collection-1-tab-2"> ${result.liveUrl?html} 
							<!-- has ${result.collapsed.count} duplicate<#if result.collapsed.count != 1>s</#if>) -->
							</a>
				</div>	
				</td>
		</tr>
		</#list>
	</tbody>
</table>
</div>
</@s.AfterSearchOnly>