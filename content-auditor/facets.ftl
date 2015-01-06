<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
<@s.AfterSearchOnly>
<!--BEGINFACETS-->
<!-- RESULTS SUMMARY -->
<!-- Collection: <@s.cfg>service_name</@s.cfg> | Attributes last updated: ${response.resultPacket.details.collectionUpdated?datetime} -->
<div id="tabbable-content" class="row">
	<!--<div id="fb-facets" class="col-md-3 col-lg-2">
					<ul class="nav nav-pills nav-stacked">
								<li class="active" role="presentation"><a href="#" title="home">Home</a></li>
								<li role="presentation"><a href="#" title="Profile">Profile</a></li>
								<li role="presentation"><a href="#" title="Messages">Messages</a></li>
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
		    <#if question.selectedCategoryValues?has_content || question.inputParameterMap["duplicate_signature"]??> 
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
							<h4>Breakdown</h4>
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
								<h4>Attributes <small>(${catRowNum})</small></h4>
							</div>

							<#if (catRowNum > 0) >

							<table id="chart-attr-${facet_counter}" data-attribute="${s.facet.name}" class="page-attr table table-striped" data-rows="${catRowNum}" <#if facet_counter == 0>data-toggle="table"</#if> data-height="100">
								<thead>
									<tr>
										<th>Value</th>
										<th>Count</th>
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
										<td><span class="badge detail-count"><@s.CategoryCount /></span> </td>
										
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
					<#--
					"legend": {
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
					//"groupPercent": "1",
					"labelsEnabled": false,
					"pullOutOnlyOne":true,
					"pullOutEffect":"elastic"
				}
				);
				
				var targetChart = content_auditor.chart_${s.facet_index};
				
				targetChart.addListener('rendered', function (event) {
				   
				    var target = document.getElementById('chart-attr-${s.facet_index}');
				    var targetID = '#facet-tab-chart-${s.facet_index}';
				    var targetTableHead = $(targetID + ' table thead tr');

				    if(!targetTableHead.attr('data-chart-legend')){
				    	targetTableHead.append('<th>Percent</th>').attr('data-chart-legend',true);
					}	
				    for( i = 0; i < content_auditor.chart_${s.facet_index}.chartData.length; ) {
				    	
				    	var row = content_auditor.chart_${s.facet_index}.chartData[i];
				        var color = content_auditor.chart_${s.facet_index}.colors[i];
				        var percent = Math.round( row.percents * 100 ) / 100;
				        var value = row.value;
				        var target = $('#attr-${facet_counter}-' + i );
				        var title = row.title;
				       //console.log(row);
				        if(!target.attr('data-chart-legend')){
				        	target
				        	.attr('data-chart-legend',true)
				        	.attr('data-chart-index',i)
				        	.attr('data-title', title)
				        	.attr('data-value', value)
				        	.attr('data-percent', percent)
				        	.attr('data-color', color)
				        	.attr('data-target', row)
				        	.on('mouseover',function(){  
				        		var t = $(this);
				        		hoverSlice($(this).attr('data-chart-index'), 'chart_' + ${s.facet_index}); 
				        		//$('#facet-chart-legend-${s.facet_index}' ).html( '<h4>Summary for <strong>'+ t.attr('data-title') + '</strong></h4> <br>Found <strong>' + t.attr('data-value') + '</strong> times in the ' + $('#detail-current-collection').text() + ' collection, this makes up for <strong>' + t.attr('data-percent') + '%</strong> of the overall total <strong>' + t.parents('.page-attr').attr('data-attribute') + ' attributes</strong>. ');
				        	})
				        	.on('mouseleave',function(){ 
				        		blurSlice($(this).attr('data-chart-index') , 'chart_' + ${s.facet_index}); 
				        	})
				        	.append('<td><strong class="detail-percent">' + percent + '%</strong></td>')
				        	.find('td:first-child').prepend('<span class="badge detail-legend" style="background:'+color+'">&nbsp;</span>&nbsp;')
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
		<#if 	 FacetLabel == 'URI'>				<#assign facetNavIcon ='link'>
		<#elseif FacetLabel == 'Date modified'>		<#assign facetNavIcon ='calendar'>
		<#elseif FacetLabel == 'Author'>			<#assign facetNavIcon ='user'>
		<#elseif FacetLabel == 'Format'>			<#assign facetNavIcon ='file-text-o'>
		<#elseif FacetLabel == 'Language'>			<#assign facetNavIcon ='globe'>
		<#elseif FacetLabel == 'Subject'>			<#assign facetNavIcon ='book'>
		<#elseif FacetLabel == 'Publisher'>			<#assign facetNavIcon ='newspaper-o'>
		<#elseif FacetLabel == 'Title'>				<#assign facetNavIcon ='font'>
		
		<#-- These icons need checking as I cannot see this on my local copy ~ Steve  -->
		
		<#elseif FacetLabel == 'Busiess Stage'>	    <#assign facetNavIcon ='line-chart'>
		<#elseif FacetLabel == 'Busiess Structure'>	<#assign facetNavIcon ='pie-chart'>
		<#elseif FacetLabel == 'Generator'>			<#assign facetNavIcon ='cogs'>
		<#elseif FacetLabel == 'Missing Content'>	<#assign facetNavIcon ='question'>
		<#elseif FacetLabel == 'Missing Content Attributes'><#assign facetNavIcon ='question'>
		<#elseif FacetLabel == 'Creator'>		    <#assign facetNavIcon ='user'>
		<#elseif FacetLabel == 'page Type'>		    <#assign facetNavIcon ='file'>
		<#elseif FacetLabel == 'Red Tape Reduction'><#assign facetNavIcon ='umbrella'>
		<#elseif FacetLabel == 'Four Pillars'>		<#assign facetNavIcon ='certificate'> 
		
		<#else>
		<!-- Default Icon -->
													<#assign facetNavIcon ='circle-o'>
		</#if>

		<li ${facetNavigationClass} role="presentation">
			<a href="#facet-${s.facet_index}.tab-pane" class="" title="View ${FacetLabel} Attributes" data-toggle="tab" role="tab" aria-controls="profile" data-chart_ref="chart_${s.facet_index}"><span class="fa fa-${facetNavIcon}"></span> &nbsp;${FacetLabel}</a>
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