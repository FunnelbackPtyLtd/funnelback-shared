<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
<@s.AfterSearchOnly>
<!--BEGINFACETS-->
<!-- RESULTS SUMMARY -->
<p class="lead-in">
<#if response.resultPacket.resultsSummary.totalMatching == 0>
<strong>No </strong>attributes are availiable as there are <strong class="fb-result-count" id="fb-total-matching">0</strong> search results for <strong>${response.resultPacket.queryAsProcessed?html}</strong>
</#if>
<#if response.resultPacket.resultsSummary.totalMatching != 0>
Attributes attached to the <strong class="fb-result-count" class="fb-total-matching">${response.resultPacket.resultsSummary.totalMatching?string.number}</strong>
search results for <strong>${queryToReport?html}</strong>
</#if>
</p>
<!-- Collection: <@s.cfg>service_name</@s.cfg> | Attributes last updated: ${response.resultPacket.details.collectionUpdated?datetime} -->
<div id="tabbable-content" class="row">
	<!--<div id="fb-facets" class="col-md-3 col-lg-2">
					<ul class="nav nav-pills nav-stacked">
								<li class="active" role="presentation"><a href="#">Home</a></li>
								<li role="presentation"><a href="#">Profile</a></li>
								<li role="presentation"><a href="#">Messages</a></li>
					</ul>
	</div>-->
	<#macro FacetAttributesPanels>
	<#assign facet_counter = 0 />
	
	<@s.Facet class="tab-pane active clearfix">
	
	<#assign FacetLabel><@s.FacetLabel /></#assign>
	<#assign FacetSummary><@s.FacetSummary /></#assign>
	<#assign FacetSummaryClean><@s.FacetSummary /></#assign>
	<#assign FacetSummaryLabel>${FacetSummaryClean?replace("<[^>]*>","eeee","g")?replace(":","")?replace("all","")?replace("&rarr;","")}</#assign>
	 
	<div>
		<div id="btn-facet-container"><span class="fa fa-bars fa-lg"></span></div>
		<div id="facet-container-wrapper" class="row">
			
			<div id="fb-facet-details" class="col-md-10"role="tabpanel">
				<div class="fb-facet-header">
				<h3><@s.FacetLabel summary=false /></h3>
				
				<p class="fb-facet-breadcrumb text-muted">${(FacetSummary?replace(":",""))?replace("all",('All ' + s.facet.name)?replace("<[^>]*>", "", "r"))?replace("&rarr;"," &nbsp;<i class=\"fa fa-angle-right text-muted\"></i>&nbsp; ")}</p>
				<div class="set-all-anchor hidden">${FacetSummaryLabel}</div>
			</div>
				<#-- Nav tabs
				<ul class="nav nav-tabs" role="tablist">
					<li role="presentation" class="active"><a href="#facet-tab-chart-${facet_counter}" aria-controls="Content Attributes" role="tab" data-toggle="tab">Attributes</a></li>
					<li role="presentation"><a href="#facet-tab-urls-${facet_counter}" aria-controls="Pages" role="tab" data-toggle="tab">Pages </a></li>
					
					<li role="presentation"><a href="#facet-tab-page-${facet_counter}" aria-controls="Spare Page" role="tab" data-toggle="tab">Spare</a></li>
				</ul>-->
				
				<!-- Tab panes -->
				<div class="tab-content">
					
					<div role="tabpanel" class="tab-pane active" id="facet-tab-chart-${facet_counter}">
						<!--<h3><@s.FacetLabel summary=false /><@s.FacetSummary /> Attribute</h3>-->
						<div class="facet-search-chart facets-chart-wrapper col-md-12 ">
							<h4 class="inline-block"><#if (FacetSummaryLabel)?trim != "">Breakdown of: <strong>${FacetSummaryLabel} <!--<a class="btn btn-xs btn-primary inline-block"><span class="fa fa-times fa-times"></span> Clear</a>--> </strong><#else>${s.facet.name} Breakdown</#if> </h4>
							<div id="facet-chart-${facet_counter}" style="height : 700px"></div>
						</div>
						<!--URLS-->
						<div class="facet-search-details boxed no-border-lr facet-container col-md-12 col-md-pull-0">
							
							<div class="facet-header">
								<h4><#if (FacetSummaryLabel)?trim !="">Attributes of: <strong>${FacetSummaryLabel}</strong><#else>${s.facet.name} Attributes</#if></h4>
							</div>

							<table id="chart-attr-${facet_counter}" class="table table-striped" <#if facet_counter == 0>data-toggle="table"</#if> data-height="100">
								<thead>
									<tr>
										<#--<th></th>-->
										<th>${s.facet.name}</th>
										<th >Occurance</th>
									</tr>
								</thead>
								<tbody>
									<#assign catTableCounter = 0 />
									<@s.Category max=1000>
									<#assign catTableCounter = catTableCounter + 1>
									<#assign assignCategoryName><@s.CategoryName /></#assign>
									<tr>
										<#--<td>&nbsp;${catTableCounter}</td>-->
										<td>${assignCategoryName?replace("&amp;type=facets","")?replace('">', '#facet-' + facet_counter + '.tab-pane">')}</td>
										<td><span class="badge"><@s.CategoryCount /></span> </td>
										
									</tr>
									</@s.Category>
								</tbody>
							</table>

							<div id="chart-attr-count-${facet_counter}" class="hide row-count" data-row-count="${catTableCounter}"></div>
						</div>
						<!--end URLS-->
					</div>
					<div role="tabpanel" class="tab-pane" id="facet-tab-urls-${facet_counter}"></div>
					<div role="tabpanel" class="tab-pane" id="facet-tab-dupes-${facet_counter}">
						<#include "/web/templates/modernui/content-auditor/collapsed_duplicates.ftl" />
					</div>
					<div role="tabpanel" class="tab-pane" id="facet-tab-page-${facet_counter}">...</div>
				</div>
			</div>
			<#assign other_counter = response.resultPacket.resultsSummary.totalMatching />
			
			
			<script type="text/javascript">
				content_auditor.chart_${s.facet_index} = AmCharts.makeChart("facet-chart-${facet_counter}", {
					"type": "pie",
					"startAngle": "225",
					"startDuration": 0,

// Add in legend					
					"legend": {
						"markerType": "circle",
						"position": "bottom",
						"align": "center"
					},
					"dataProvider": [
			
					<#assign categoriesToList = s.facet.categories>
					<#if (s.facet.categories[0].categories?size > 0)>
						<#assign categoriesToList = s.facet.categories[0].categories />
					</#if>
			
					<#assign separator = ''>
					<#assign counter = 0>
					<#list categoriesToList as c>
						<#list c.values as cv>
							${separator}
							{
								"label": "${cv.label?js_string}",
								"count": "${cv.count?c}"
							}
							<#assign separator = ','>
						</#list>
					</#list>
					],
					"valueField": "count",
					"titleField": "label",
					"groupPercent": "1",
					"labelsEnabled": false
				}
				);
			</script>
			<#assign facet_counter = facet_counter + 1 />
			</div><!--END: tab panel-->
		</div>
		</@s.Facet>
		</#macro>
		
		<#macro FacetAttributesNavigation>
		<#assign facet_counter = 0 />
		<#assign facetNavigation = ''>
		<@s.Facet class="hide remove" >
		<#if s.facet_index == 0 >
		<#assign facetNavigationClass = 'class="active"'>
		<#else>
		<#assign facetNavigationClass = "">
		</#if>
		<#assign FacetLabel><@s.FacetLabel summary=false /></#assign>
		<#assign facetNavigationItem>
		<li ${facetNavigationClass} role="presentation">
			<a href="#facet-${s.facet_index}.tab-pane" class="" title="" data-toggle="tab" role="tab" aria-controls="profile" data-chart_ref="chart_${s.facet_index}"><span class="fa fa-circle-thin"></span>&nbsp;${FacetLabel}</a>
		</li>
		</#assign>
		<#assign facetNavigation = facetNavigation + facetNavigationItem >
		
		</@s.Facet>
		${facetNavigation}
		</#macro>
		<@s.FacetedSearch>
		<#-- <div id="fb-facets-navigation" class="fb-facets col-sm-12 col-md-2">
		<ul class="nav nav-pills nav-stacked"><@FacetAttributesNavigation /></ul>
	</div> -->
	<div id="fb-facets-content">
		<div id="my-tab-content" class="tab-content row">
			<div id="fb-facets-navigation" class="fb-facets col-sm-12 col-md-2">
			<ul class="nav nav-pills nav-stacked"><@FacetAttributesNavigation /></ul>
		</div>
		<@FacetAttributesPanels />
	</div>
</div>
</@s.FacetedSearch>

<div>
	
</div>
<!--ENDFACETS-->
</div><!--END coloumn-->
</@s.AfterSearchOnly>