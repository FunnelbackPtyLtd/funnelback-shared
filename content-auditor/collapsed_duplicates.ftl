<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
<@s.AfterSearchOnly>
<#macro duplicateTable totalDuplicates  >

<div class="facet-container col-md-8 boxed">

	<table id="duplicates" class="table table-striped">
		<thead>
			<tr>
				<th>Instances</th>
				<th>Filesize</th>
				<th>Total</th>
				<th>Document</th>
			</tr>
		</thead>
		<tbody>
			<#assign duplicatesRowCounter = 0 />
			<#list filterList(extraSearches.duplicates.response.resultPacket.results, 'collapsed')?sort_by(['collapsed','count'])?reverse as result>
			<tr>
				<#assign duplicatesRowCounter = duplicatesRowCounter + 1 />
				
				<td class="text-center">
					<a class="text-muted duplicates-count" href="?${QueryString}&amp;duplicate_signature=%3F:${result.collapsed.signature}#collection-${currentCollection}-tab-2">
						<div class="badge badge-danger"> x <strong>${result.collapsed.count + 1}</strong>
		                        </div>
				</td>
		                <td class="text-center">
                            ${ fb.renderSize(result.fileSize) }
                        </td>
                        <td class="text-center">
                        <i>${fb.renderSize((result.collapsed.count + 1) * result.fileSize)}</i>
				</td>
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
	
	<#assign totalDuplicates=filterList(extraSearches.duplicates.response.resultPacket.results, 'collapsed')?size />

	<@appliedFacetsBlock urlHash="#collection-${currentCollection}-tab-3" />

	<#if filterList(extraSearches.duplicates.response.resultPacket.results, 'collapsed')?size < 1 >
		<div class="tab-header clearfix">
			<p class="pull-left">No duplicate content was found.</strong></p>
		</div>
	<#else>
		<div class="tab-header clearfix">
			<p class="pull-left">  <strong>${totalDuplicates}</strong> URL(s) contain content duplicated elsewhere in this collection.</strong></p>
		</div>

		<@duplicateTable totalDuplicates=totalDuplicates />
	</#if>
	
	</@s.AfterSearchOnly>