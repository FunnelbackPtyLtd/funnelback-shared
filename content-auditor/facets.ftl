<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
<@s.AfterSearchOnly>
<!--BEGINFACETS-->
<!-- RESULTS SUMMARY -->
<p class="lead-in">
<#if response.resultPacket.resultsSummary.totalMatching == 0>
<strong>No </strong>attributes are availiable as there are <strong class="fb-result-count fb-total-matching">0</strong> search results for <strong>${response.resultPacket.queryAsProcessed?html}</strong>
</#if>
<#if response.resultPacket.resultsSummary.totalMatching != 0>
Attributes attached to the <strong class="fb-result-count fb-total-matching">${response.resultPacket.resultsSummary.totalMatching?string.number}</strong>
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
			
			<div id="fb-facet-details" class="col-md-10" role="tabpanel">
				<div class="fb-facet-header">
				<h3><@s.FacetLabel summary=false /></h3>
				
				<p class="fb-facet-breadcrumb text-muted">${(FacetSummary?replace(":",""))?replace("all",('All ' + s.facet.name)?replace("<[^>]*>", "", "r"))?replace("&rarr;"," &nbsp;<i class=\"fa fa-angle-right text-muted\"></i>&nbsp; ")}</p>
				<div class="set-all-anchor hidden">${FacetSummaryLabel}</div>
			</div>


			<#-- applied facets block -->
		    <#if question.selectedCategoryValues?has_content> 
		    	<div class="drill-filters"><span class="fa fa-filter"></span>
		        <@AppliedFacets class="btn btn-xs btn-warning" group=true urlHash="#facet-${facet_counter}.tab-pane"/>
		        <@ClearFacetsLink  class="btn btn-xs btn-danger" urlHash="#facet-${facet_counter}.tab-pane"/>
		    	</div>
		    </#if>
			
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
						<div class="facet-search-chart facets-chart-wrapper col-md-12">
						<div class="facet-header inline-block">
							<h4><#if (FacetSummaryLabel)?trim != "">Breakdown of: <strong>${FacetSummaryLabel} <!--<a class="btn btn-xs btn-primary inline-block"><span class="fa fa-times fa-times"></span> Clear</a>--> </strong><#else>${s.facet.name} Breakdown</#if> </h4>
						</div>	
							<div id="facet-chart-${facet_counter}" class="facet-chart-container"></div>
							<div id="facet-chart-legend-${s.facet_index}" class="facet-chart-legend"></div>
						</div>
							

						<!--Attributes Coloumn-->
						<#assign catRowNum = 0 />
						<@s.Category max=1000>
							<#assign catRowNum = catRowNum + 1>
						</@s.Category>
									
						<div class="facet-search-details boxed no-border-lr facet-container col-md-12">
						
						<div class="facet-header">
								<h4><#if (FacetSummaryLabel)?trim !="">Attributes of: <strong>${FacetSummaryLabel}</strong><#else>${s.facet.name} Attributes <small>(${catRowNum})</small></#if></h4>
							</div>

							<#if (catRowNum > 0) >

							<table id="chart-attr-${facet_counter}" class="page-attr table table-striped" data-rows="${catRowNum}" <#if facet_counter == 0>data-toggle="table"</#if> data-height="100">
								<thead>
									<tr>
										<th>${s.facet.name}</th>
										<th >Occurrence</th>
									</tr>
								</thead>
								<tbody>
									<#assign catTableCounter = 0 />
									<@s.Category max=1000>
									
									
									<#assign assignCategoryName><@s.CategoryName /></#assign>
									<#--TODO: Remove wrapping Div element. <div class="category"> -->
									<tr id="attr-${facet_counter}-${catTableCounter}">
										<#--<td>&nbsp;${catTableCounter}</td>-->
										<td>${assignCategoryName?replace("&amp;type=facets","")?replace('">', '#facet-' + facet_counter + '.tab-pane">')}</td>
										<td><span class="badge"><@s.CategoryCount /></span> </td>
										
									</tr>
									<#assign catTableCounter = catTableCounter + 1>
									</@s.Category>
								</tbody>
							</table>

							<div id="chart-attr-count-${facet_counter}" class="hide row-count" data-row-count="${catTableCounter}"></div>
						
						<!--Attributes Coloumn-->

						<#else>
						<hr>
						<div class="drilled-to-last well well-fb" style="margin:0 28px 0 15px">
						<p style="margin:0px 0 15px">It looks like you've drilled down as far as possible for this attribute.</p>
						<!--<hr><a class="btn btn-default btn-sm"><span class="fa fa-long-arrow-left"></span> Show All</a>-->
						
						</div>
						</#if>
						</div>

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

				var baseColors = [];

				<#if (catRowNum < 2) >
				<#assign hexSteps = 2>
				<#else>
				<#assign hexSteps = catRowNum>
				</#if>
				(new KolorWheel("#19CD9B")).abs( 255,255,75,${hexSteps}).each(function() {
					baseColors[this.step] = this.getHex();
				});


				content_auditor.chart_${s.facet_index} = AmCharts.makeChart("facet-chart-${facet_counter}", {
					"type": "pie",
					"startAngle": "225",
					"startDuration": 0,
					"innerRadius":"30%",
					"colors":baseColors,
					<#--"legend": {
						"markerType": "triangleDown",
						"position": "bottom",
						"align": "center",
						"divId": "facet-chart-legend-${s.facet_index}"
					},
					-->
					"dataProvider": [
			
					<#assign categoriesToList = s.facet.categories>
					<#if s.facet.categories[0]?? && (s.facet.categories[0].categories?size > 0)>
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
				
				var targetChart = content_auditor.chart_${s.facet_index};
				
				targetChart.addListener('rendered', function (event) {
				   
				    var target = document.getElementById('chart-attr-${s.facet_index}');
				    var targetTableHead = $('#facet-tab-chart-${s.facet_index} table thead tr');
				    if(!targetTableHead.attr('data-chart-legend')){
				    	targetTableHead.append('<th>Overall</th>').attr('data-chart-legend',true);
					}	
				    for( i = 0; i < content_auditor.chart_${s.facet_index}.chartData.length; ) {
				    	
				    	var row = content_auditor.chart_${s.facet_index}.chartData[i];
				        var color = content_auditor.chart_${s.facet_index}.colors[i];
				        var percent = Math.round( row.percents * 100 ) / 100;
				        var value = row.value;
				        var target = $('#attr-${facet_counter}-' + i );
				       
				        if(!target.attr('data-chart-legend')){
				        	target.attr('data-chart-legend',true)
				        	target.attr('data-chart-index',i)
				        	.on('mouseover',function(){  hoverSlice($(this).attr('data-chart-index'), 'chart_' + ${s.facet_index}); })
				        	.on('mouseleave',function(){ blurSlice($(this).attr('data-chart-index') , 'chart_' + ${s.facet_index}); })
				        	.append('<td><strong>' + percent + '%</strong></td>')
				        	.find('td:first-child').prepend('<span class="badge" style="background:'+color+'">&nbsp;</span>&nbsp;')
				        	//.parent('tr').find('.badge').css({background: color })
				        	//.append('<td>one</td>')
				        	;
				        }
				        i++;  
				    }

					 	
				});
						

				
			</script>
			<#assign facet_counter = facet_counter + 1 />
			</div><!--END: tab panel-->
		</div>
		</@s.Facet>
		</#macro>
		
		<#macro FacetAttributesNavigation>
		<#assign facet_counter = 0 />
		<#assign facetNavigation = ''>
		<@s.Facet class="hide remove">
		<#if s.facet_index == 0 >
		<#assign facetNavigationClass = 'class="active"'>
		<#else>
		<#assign facetNavigationClass = "">
		</#if>
		
		
		<#assign facetNavigationItem>
		<#assign FacetLabel><@s.FacetLabel summary=false /></#assign>
		<#assign FacetLabel = FacetLabel?replace("<[^>]*>", "", "r")?trim >
		<#if 	 FacetLabel == 'URI'>			<#assign facetNavIcon ='link'>
		<#elseif FacetLabel == 'Date modified'>	<#assign facetNavIcon ='calendar'>
		<#elseif FacetLabel == 'Author'>		<#assign facetNavIcon ='user'>
		<#elseif FacetLabel == 'Format'>		<#assign facetNavIcon ='file-text-o'>
		<#elseif FacetLabel == 'Language'>		<#assign facetNavIcon ='globe'>
		<#elseif FacetLabel == 'Subject'>		<#assign facetNavIcon ='book'>
		<#elseif FacetLabel == 'Publisher'>		<#assign facetNavIcon ='newspaper-o'>
		<#elseif FacetLabel == 'Title'>			<#assign facetNavIcon ='font'>
		<#else>
		<!-- Default Icon -->
												<#assign facetNavIcon ='circle-thin'>
		</#if>

		<li ${facetNavigationClass} role="presentation">
			<a href="#facet-${s.facet_index}.tab-pane" class="" title="" data-toggle="tab" role="tab" aria-controls="profile" data-chart_ref="chart_${s.facet_index}"><span class="fa fa-${facetNavIcon}"></span> &nbsp;${FacetLabel}</a>
		</li>
		</#assign>
		<#assign facetNavigation = facetNavigation + facetNavigationItem >
		
		</@s.Facet>
		${facetNavigation}
		</#macro>

        <div>
            <h3>Overview</h3>
            <i>Maybe this should be a tab?</i>
            <@s.FacetedSearch>
                <@s.Facet>
                  <#assign categoryCount = 0 />
                  <#assign sep = '' />
                  <@s.FacetLabel tag="b"/>:
                  <@s.Category tag="span">
                    <#assign categoryCount = categoryCount + 1 />
                    <#if categoryCount &lt; 4>
                       ${sep} <@s.CategoryName class="" />&nbsp;(<@s.CategoryCount />)
                    </#if>
                    <#assign sep = ',' />                    
                    <#if categoryCount == 4>
                        , more...
                    </#if>
                  </@s.Category>
                </@s.Facet>
                <br />
            </@s.FacetedSearch>
        </div>

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