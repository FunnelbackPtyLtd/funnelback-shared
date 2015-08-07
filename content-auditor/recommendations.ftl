    <div id="facet-container-wrapper" style="margin:0" class="row">
        <div class="col-md-12 no-border no-padding">
            <div class="fb-facet-header"> <h3><span class="facetLabel">Recommendations</span></h3></div>
            
            <@appliedFacetsBlock urlHash="#collection-${currentCollection}-tab-0"/>

            <div class="recommendations">
                <#assign categoryMax = 10 />
                <@s.FacetedSearch>

                  <script type="text/javascript">
                    function navigateToDataContextUrl(event) {
                      if (event.item.dataContext.url != undefined) {
                        window.location = event.item.dataContext.url;
                      }
                    }
                  </script>

                  <@s.Facet name="Reading Grade">
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
                                              "url": "${s.CategoryUrl(main.contentAuditorLink)?js_string}#collection-test-content-auditor-tab-2"
                                           }
                                          <#assign separator = ','>
                                  </@s.Category>
                                ];

                            data.sort(function(a,b) { return a.label - b.label; });

                            readingGradeChart = AmCharts.makeChart( "reading-grade-chartdiv", {
                              "type": "serial",
                              "dataProvider": data,
                              "graphs": [ {
                                "type": "column",
                                "valueField": "count"
                              } ],
                              "categoryField": "label",
                              "height": 300
                            } );

                            readingGradeChart.addListener("clickGraphItem", navigateToDataContextUrl);
                        </script>
                      </div>
                    </div>
                  </@s.Facet>

                  <@s.Facet name="Undesirable Text">
                    <div class="col-md-6">
                      <div class="panel panel-default">
                        <div class="panel-heading">
                          <h3 class="panel-title"><@s.FacetLabel tag="strong"/></h3>
                        </div>
                        <div class="panel-body">
                          <#assign categoryCount = 0 />
                          <@s.Category max=categoryMax tag="div">
                            <#assign categoryCount = categoryCount + 1 />
                                
                            <@s.CategoryName class="" link=main.contentAuditorLink  extraParams="#collection-test-content-auditor-tab-2" />&nbsp;<small class="text-muted">(<@s.CategoryCount />)</small>

                          </@s.Category>
                        </div>
                        <#if categoryCount == categoryMax>
                          <#-- Count up the number of category values there will be, so we can show the number -->
                          <#assign countOfCategoryValues = 0 />
                          <@s.Category max=2147483647 tag="">
                              <#assign countOfCategoryValues = countOfCategoryValues + 1 />
                          </@s.Category>

                          <div class="panel-footer">
                            <a class="btn btn-xs btn-primary" data-toggle="tab" href="#collection-${currentCollection}-tab-1" aria-expanded="true" title="View All ${countOfCategoryValues}" data-chart_ref="chart_${s.facet_index}" onClick="facetTabShow(${s.facet_index})"> View All ${countOfCategoryValues} <span class="fa fa-arrow-right"></span></a>
                          </div>
                        </#if>
                      </div>
                    </div>
                  </@s.Facet>

                  <@s.Facet name="Duplicate Titles">
                    <div class="col-md-6">
                      <div class="panel panel-default">
                        <div class="panel-heading">
                          <h3 class="panel-title"><@s.FacetLabel tag="strong"/></h3>
                        </div>
                        <div class="panel-body">
                          <#assign categoryCount = 0 />
                          <@s.Category max=categoryMax tag="div">
                            <#if s.categoryValue.count &gt; 1>
                              <#assign categoryCount = categoryCount + 1 />
                                    
                              <@s.CategoryName class="" link=main.contentAuditorLink extraParams="#collection-test-content-auditor-tab-2" />&nbsp;<small class="text-muted">(<@s.CategoryCount />)</small>

                            </#if>
                          </@s.Category>
                        </div>
                        <#if categoryCount == categoryMax>
                          <#-- Count up the number of category values there will be, so we can show the number -->
                          <#assign countOfCategoryValues = 0 />
                          <@s.Category max=2147483647 tag="">
                              <#assign countOfCategoryValues = countOfCategoryValues + 1 />
                          </@s.Category>

                          <div class="panel-footer">
                            <a class="btn btn-xs btn-primary" data-toggle="tab" href="#collection-${currentCollection}-tab-1" aria-expanded="true" title="View All ${countOfCategoryValues}" data-chart_ref="chart_${s.facet_index}" onClick="facetTabShow(${s.facet_index})"> View All ${countOfCategoryValues} <span class="fa fa-arrow-right"></span></a>
                          </div>
                        </#if>
                      </div>
                    </div>
                  </@s.Facet>

                  <@s.Facet name="Missing Metadata">
                    <div class="col-md-6">
                      <div class="panel panel-default">
                        <div class="panel-heading">
                          <h3 class="panel-title"><@s.FacetLabel tag="strong"/></h3>
                        </div>
                        <div class="panel-body">
                          <#assign categoryCount = 0 />
                          <@s.Category max=categoryMax tag="div">
                            <#if s.categoryValue.count &gt; 1>
                              <#assign categoryCount = categoryCount + 1 />
                                    
                              <@s.CategoryName class="" link=main.contentAuditorLink extraParams="#collection-test-content-auditor-tab-2" />&nbsp;<small class="text-muted">(<@s.CategoryCount />)</small>

                            </#if>
                          </@s.Category>
                        </div>
                        <#if categoryCount == categoryMax>
                          <#-- Count up the number of category values there will be, so we can show the number -->
                          <#assign countOfCategoryValues = 0 />
                          <@s.Category max=2147483647 tag="">
                              <#assign countOfCategoryValues = countOfCategoryValues + 1 />
                          </@s.Category>

                          <div class="panel-footer">
                            <a class="btn btn-xs btn-primary" data-toggle="tab" href="#collection-${currentCollection}-tab-1" aria-expanded="true" title="View All ${countOfCategoryValues}" data-chart_ref="chart_${s.facet_index}" onClick="facetTabShow(${s.facet_index})"> View All ${countOfCategoryValues} <span class="fa fa-arrow-right"></span></a>
                          </div>
                        </#if>
                      </div>
                    </div>
                  </@s.Facet>

                  <@s.Facet name="Date Modified">
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
                                              "url": "${s.CategoryUrl(main.contentAuditorLink)?js_string}#collection-test-content-auditor-tab-2"
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
                      </div>
                    </div>
                  </@s.Facet>

                  <@s.Facet name="Response Time">
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
                                              "url": "${s.CategoryUrl(main.contentAuditorLink)?js_string}#collection-test-content-auditor-tab-2"
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
                      </div>
                    </div>
                  </@s.Facet>
                
                </@s.FacetedSearch>
            </div>
        </div>
    </div>
