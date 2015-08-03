    <div id="facet-container-wrapper" style="margin:0" class="row">
        <div class="col-md-12 no-border no-padding">
            <div class="fb-facet-header"> <h3><span class="facetLabel">Recommendations</span></h3></div>
            
            <@appliedFacetsBlock urlHash="#collection-${currentCollection}-tab-0"/>

            <div class="recommendations">
                <#assign categoryMax = question.collection.configuration.value("ui.modern.content-auditor.overview-category-count")?number />
                <@s.FacetedSearch>
                <@s.Facet>

                <#assign FacetLabel><@s.FacetLabel summary=false /></#assign>
                <#assign FacetLabel = FacetLabel?replace("<[^>]*>", "", "r")?trim >
                    <#assign categoryCount = 0 />
                    <#assign sep = '' />

                          <#-- Count up the number of category values there will be, so we can show the number -->
                          <#assign countOfCategoryValues = 0 />
                          <@s.Category max=2147483647 tag="">
                              <#assign countOfCategoryValues = countOfCategoryValues + 1 />
                          </@s.Category>

                          <script type="text/javascript">
                            function navigateToDataContextUrl(event) {
                              if (event.item.dataContext.url != undefined) {
                                window.location = event.item.dataContext.url;
                              }
                            }
                          </script>

                          <#if FacetLabel == 'Reading Grade'>
                              <div class="col-md-6">
                                  <div class="panel panel-default">
                                      <div class="panel-heading">
                                          <h3 class="panel-title"><@s.FacetLabel tag="strong"/></h3>
                                      </div>
                            <div class="panel-body">
                              <div id="reading-grade-chartdiv" style="width: 100%; height: 300px;"></div>
                            </div>
                              <script type="text/javascript">
                                  var data = [
                                        <#assign separator = ''>
                                        <@s.Category max=2147483647 tag="">
                                                ${separator}
                                                {
                                                    "label": "${s.categoryValue.label?js_string}",
                                                    "count": "${s.categoryValue.count?c}",
                                                    "url": "${s.CategoryUrl(main.contentAuditorLink)?js_string}"
                                                 }
                                                <#assign separator = ','>
                                        </@s.Category>
                                      ];

                                  data.sort(function(a,b) { return a.label - b.label; });

                                  var chartNew = AmCharts.makeChart( "reading-grade-chartdiv", {
                                    "type": "serial",
                                    "dataProvider": data,
                                    "graphs": [ {
                                      "type": "column",
                                      "valueField": "count"
                                    } ],
                                    "categoryField": "label",
                                    "height": 300
                                  } );

                                  chartNew.addListener("clickGraphItem", navigateToDataContextUrl);
                              </script>

                            <div class="panel-footer">
                                <a class="btn btn-xs btn-primary" data-toggle="tab" href="#collection-${currentCollection}-tab-1" aria-expanded="true" title="View All ${countOfCategoryValues}" data-chart_ref="chart_${s.facet_index}" onClick="facetTabShow(${s.facet_index})"> View All ${countOfCategoryValues} <span class="fa fa-arrow-right"></span></a>
                            </div>

                                  </div>
                              </div>
                          <#elseif FacetLabel == 'Date modified'>
                              <div class="col-md-6">
                                  <div class="panel panel-default">
                                      <div class="panel-heading">
                                          <h3 class="panel-title"><@s.FacetLabel tag="strong"/></h3>
                                      </div>
                            <div class="panel-body">
                              <div id="date-modified-chartdiv" style="width: 100%; height: 300px;"></div>
                            </div>
                              <script type="text/javascript">
                                  var data = [
                                        <#assign separator = ''>
                                        <@s.Category max=2147483647 tag="">
                                                ${separator}
                                                {
                                                    "label": "${s.categoryValue.label?js_string}",
                                                    "count": "${s.categoryValue.count?c}",
                                                    "url": "${s.CategoryUrl(main.contentAuditorLink)?js_string}"
                                                 }
                                                <#assign separator = ','>
                                        </@s.Category>
                                      ];

                                  data.sort(function(a,b) { return a.label - b.label; });

                                  var chartNew = AmCharts.makeChart( "date-modified-chartdiv", {
                                    "type": "serial",
                                    "dataProvider": data,
                                    "graphs": [ {
                                      "type": "column",
                                      "valueField": "count"
                                    } ],
                                    "categoryField": "label",
                                    "height": 300
                                  } );

                                  chartNew.addListener("clickGraphItem", navigateToDataContextUrl);

                              </script>

                            <div class="panel-footer">
                                <a class="btn btn-xs btn-primary" data-toggle="tab" href="#collection-${currentCollection}-tab-1" aria-expanded="true" title="View All ${countOfCategoryValues}" data-chart_ref="chart_${s.facet_index}" onClick="facetTabShow(${s.facet_index})"> View All ${countOfCategoryValues} <span class="fa fa-arrow-right"></span></a>
                            </div>


                                  </div>
                              </div>
                          <#elseif FacetLabel == 'Response Time'>
                              <div class="col-md-6">
                                  <div class="panel panel-default">
                                      <div class="panel-heading">
                                          <h3 class="panel-title"><@s.FacetLabel tag="strong"/></h3>
                                      </div>
                            <div class="panel-body">
                              <div id="response-time-chartdiv" style="width: 100%; height: 300px;"></div>
                            </div>
                              <script type="text/javascript">
                                  var data = [
                                        <#assign separator = ''>
                                        <@s.Category max=2147483647 tag="">
                                                ${separator}
                                                {
                                                    "sort": "${s.categoryValue.label?keep_before("-")?js_string}",
                                                    "label": "${s.categoryValue.label?js_string}",
                                                    "count": "${s.categoryValue.count?c}",
                                                    "url": "${s.CategoryUrl(main.contentAuditorLink)?js_string}"
                                                }
                                                <#assign separator = ','>
                                        </@s.Category>
                                      ];

                                  data.sort(function(a,b) { return a.sort - b.sort; });

                                  var chartNew = AmCharts.makeChart( "response-time-chartdiv", {
                                    "type": "serial",
                                    "dataProvider": data,
                                    "graphs": [ {
                                      "type": "column",
                                      "valueField": "count"
                                    } ],
                                    "categoryField": "label",
                                    "height": 300
                                  } );

                                  chartNew.addListener("clickGraphItem", navigateToDataContextUrl);
                              </script>

                            <div class="panel-footer">
                                <a class="btn btn-xs btn-primary" data-toggle="tab" href="#collection-${currentCollection}-tab-1" aria-expanded="true" title="View All ${countOfCategoryValues}" data-chart_ref="chart_${s.facet_index}" onClick="facetTabShow(${s.facet_index})"> View All ${countOfCategoryValues} <span class="fa fa-arrow-right"></span></a>
                            </div>

                                  </div>
                              </div>
                          <#elseif FacetLabel == 'Duplicate Titles'>
                              <div class="col-md-6">
                                  <div class="panel panel-default">
                                      <div class="panel-heading">
                                          <h3 class="panel-title"><@s.FacetLabel tag="strong"/></h3>
                                      </div>
                            <div class="panel-body">

                                <@s.Category max=categoryMax tag="div">
                                  <#if s.categoryValue.count &gt; 1>
                                    <#assign categoryCount = categoryCount + 1 />
                                    
                                    ${sep} <@s.CategoryName class="" link=main.contentAuditorLink />&nbsp;<small class="text-muted">(<@s.CategoryCount />)</small>
                                    
                                    <#assign sep = '' />
                                  </#if>
                                </@s.Category>
                                
                            </div>

                            <#if categoryCount == categoryMax>
                              <div class="panel-footer">
                                  <a class="btn btn-xs btn-primary" data-toggle="tab" href="#collection-${currentCollection}-tab-1" aria-expanded="true" title="View All ${countOfCategoryValues}" data-chart_ref="chart_${s.facet_index}" onClick="facetTabShow(${s.facet_index})"> View All ${countOfCategoryValues} <span class="fa fa-arrow-right"></span></a>
                              </div>
                            </#if>
                                  </div>
                              </div>

                          <#elseif FacetLabel == 'Undesirable Text'>
                              <div class="col-md-6">
                                  <div class="panel panel-default">
                                      <div class="panel-heading">
                                          <h3 class="panel-title"><@s.FacetLabel tag="strong"/></h3>
                                      </div>

                            <div class="panel-body">

                                <@s.Category max=categoryMax tag="div">
                                <#assign categoryCount = categoryCount + 1 />
                                
                                ${sep} <@s.CategoryName class="" link=main.contentAuditorLink />&nbsp;<small class="text-muted">(<@s.CategoryCount />)</small>
                                
                                <#assign sep = '' />
                                </@s.Category>
                                
                            </div>

                            <#if categoryCount == categoryMax>
                              <div class="panel-footer">
                                  <a class="btn btn-xs btn-primary" data-toggle="tab" href="#collection-${currentCollection}-tab-1" aria-expanded="true" title="View All ${countOfCategoryValues}" data-chart_ref="chart_${s.facet_index}" onClick="facetTabShow(${s.facet_index})"> View All ${countOfCategoryValues} <span class="fa fa-arrow-right"></span></a>
                              </div>
                            </#if>
                                  </div>
                              </div>
                          </#if>

                
                </@s.Facet>
                
                </@s.FacetedSearch>
            </div>
        </div>
    </div>
