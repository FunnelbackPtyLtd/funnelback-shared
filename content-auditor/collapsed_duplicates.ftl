<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
<@s.AfterSearchOnly>
<#macro duplicateTable totalDuplicates  >

	<div class="tab-header clearfix">
		<p class="pull-left">  <strong>${totalDuplicates}</strong> URL(s) contain content duplicated elsewhere in this collection.</strong></p>
	</div>

	<#-- applied facets block -->
    <#if question.selectedCategoryValues?has_content> 
    	<div class="drill-filters"><span class="fa fa-filter"></span>
        <@AppliedFacets class="btn btn-xs btn-warning" group=true urlHash="#facet-${facet_counter}.tab-pane"/>
        <@ClearFacetsLink  class="btn btn-xs btn-danger" urlHash="#facet-${facet_counter}.tab-pane"/>
    	</div>
    </#if>

<div class="facet-container col-md-4 boxed">

	<table id="duplicates" class="table table-striped">
		<thead>
			<tr>
				<th><!--Count--></th>
				<th><!--Page--></th>
			</tr>
		</thead>
		<tbody>
			<#assign duplicatesRowCounter = 0 />
			<#list filterList(response.resultPacket.results, 'collapsed')?sort_by(['collapsed','count'])?reverse as result>
			<tr>
				<#assign duplicatesRowCounter = duplicatesRowCounter + 1 />
				
				<td class="text-center">
					<a class="text-muted duplicates-count" href="?${QueryString}&amp;duplicate_signature=%3F:${result.collapsed.signature}#collection-${currentCollection}-tab-2"> <div class="badge badge-danger"> x <strong>${result.collapsed.count + 1}</div></strong> </td>
					<td>
						<div class="pull-left">
							<a href="?${QueryString}&amp;duplicate_signature=%3F:${result.collapsed.signature}#collection-${currentCollection}-tab-2" title="${result.title?html}" class="clickable-link"><strong>${result.title?html} </strong></a>
							<span class="fa fa-open"></span>
							<br>
							<!-- SITE (Z) -->
							<a class="text-muted" href="?${QueryString}&amp;duplicate_signature=%3F:${result.collapsed.signature}#collection-${currentCollection}-tab-2"> ${result.liveUrl?html}
								<!-- has ${result.collapsed.count} duplicate<#if result.collapsed.count != 1>s</#if>) -->
							</a>
						</div>
					</td>
				</tr>
				</#list>
			</tbody>
		</table>
	</div>
	</#macro>
	
	<#if filterList(response.resultPacket.results, 'collapsed')?size < 1 >
	<div class="alert<#-- alert-success bg-success-->"><h4><strong><#--<span class="fa fa-check-circle-o"></span>--> Content Duplicates</strong></h4> No duplicate content could be found for this collection.</div>
	<#else>
	<@duplicateTable totalDuplicates =  filterList(response.resultPacket.results, 'collapsed')?size />
	</#if>
	
	</@s.AfterSearchOnly>