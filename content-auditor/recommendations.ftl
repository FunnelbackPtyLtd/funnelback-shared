    <div id="facet-container-wrapper" style="margin:0" class="row">
        <div class="col-md-12 no-border no-padding">
            <div class="fb-facet-header"> <h3><span class="facetLabel">Recommendations</span></h3></div>
            
            <@appliedFacetsBlock urlHash="#collection-${currentCollection}-tab-0"/>

            <div class="recommendations facets">
                <#assign categoryMax = 10 />
                <@s.FacetedSearch>

                  <script type="text/javascript">
                    function navigateToDataContextUrl(event) {
                      if (event.item.dataContext.url != undefined) {
                        window.location = event.item.dataContext.url;
                      }
                    }
                    function noContentFiller(){

                      return '<div class="no-info"><div class="inner">No information avaliable</div></div>';

                    }
                  </script>
                 
                   
                  <@s.Facet name="Reading Grade">
                      <div class="panel panel-default">
                        <div class="panel-heading">
                          <h3 class="panel-title"><@s.FacetLabel tag="strong" link=main.contentAuditorLink/></h3>
                        </div>
                        <div class="panel-body chart">
                          <div id="reading-grade-chartdiv" style="width: 100%; height: 300px;"></div>
                        </div>
                        <script type="text/javascript">
                            function colourForGrade(grade){
                              if(grade < 7 || grade > 20)
                                return 'red';
                              else 
                                return 'green';
                            }

                            var data = [
                                  <#assign separator = ''>
                                  <@s.Category max=2147483647 tag="">
                                          ${separator}
                                          {
                                              "label": "${s.categoryValue.label?js_string}",
                                              "count": "${s.categoryValue.count?c}",
                                              "url": "${s.CategoryUrl(main.contentAuditorLink)?js_string}#collection-test-content-auditor-tab-2",
                                              "colour": colourForGrade(parseInt("${s.categoryValue.label?js_string}", 10))
                                           }
                                          <#assign separator = ','>
                                  </@s.Category>
                                ];
                           
                            if(data.length  < 1){
                              setTimeout(function(){
                                $('#reading-grade-chartdiv').parent().replaceWith(noContentFiller());
                              },888);
                            }

                            data.sort(function(a,b) { return parseInt(a.label, 10) - parseInt(b.label, 10); });

                            content_auditor.readingGradeChart= AmCharts.makeChart( "reading-grade-chartdiv", {
                              "type": "serial",
                              "marginTop":0,
                              "dataProvider": data,
                              "graphs": [{
                                "id": "g1",
                                "type": "column",
                                "valueField": "count",
                                "lineAlpha": 0.2,
                                "fillColorsField": "colour",
                                "lineColors": "colour",
                                "fillAlphas": 0.75
                              } ],
                                "valueAxes": [{
                                    "axisAlpha": 1,
                                    "position": "left",
                                    "title": "Pages"
                                }],
                                "categoryAxis": {
                                    "gridPosition": "start",
                                    "fillAlpha": 0.05,
                                    "position": "bottom",
                                    "title": "Grade"
                                },
                                "chartScrollbar": {
                                    "graph": "g1",
                                    "oppositeAxis": false,
                                    "offset": 0,
                                    "scrollbarHeight": 30,
                                    "backgroundAlpha": 0,
                                    "selectedBackgroundAlpha": 0.1,
                                    "selectedBackgroundColor": "#FFC687",
                                    "graphFillAlpha": 0,
                                    "graphLineAlpha": 0.5,
                                    "selectedGraphFillAlpha": 0,
                                    "selectedGraphLineAlpha": 1,
                                    "autoGridCount": true,
                                    "color": "#444"
                                },
                                "chartCursor": {
                                    "pan": true,
                                    "valueLineEnabled": true,
                                    "valueLineBalloonEnabled": true,
                                    "cursorAlpha": 0,
                                    "valueLineAlpha": 0.2
                                  },
                              "categoryField": "label",
                              "height": 300,
                               "colors": 	['#FF6600', '#FCD202', '#B0DE09', '#0D8ECF', '#2A0CD0', '#CD0D74', '#CC0000', '#00CC00', '#0000CC', '#DDDDDD', '#999999', '#333333', '#990000']
                            } );

                           content_auditor.readingGradeChart.addListener("clickGraphItem", navigateToDataContextUrl);
                        </script>
                      </div>
                  </@s.Facet>

                  <@s.Facet name="Missing Metadata">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                          <h3 class="panel-title"><@s.FacetLabel tag="strong" link=main.contentAuditorLink/></h3>
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
                  </@s.Facet>

                  <@s.Facet name="Duplicated Titles">
                   <div class="panel panel-default">
                        <div class="panel-heading">
                          <h3 class="panel-title"><@s.FacetLabel tag="strong" link=main.contentAuditorLink/></h3>
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
                   
                  </@s.Facet>

                  <@s.Facet name="Date Modified">
                      <div class="panel panel-default">
                        <div class="panel-heading">
                          <h3 class="panel-title"><@s.FacetLabel tag="strong" link=main.contentAuditorLink/></h3>
                        </div>
                        <div class="panel-body chart">
                          <div id="date-modified-chartdiv" style="width: 100%; height: 300px;"></div>
                        </div>
                        <script type="text/javascript">
                            function colourForYear(year){
                              var now = new Date().getFullYear();
                              if(year < (now - 2) || year > now)
                                return 'red';
                              else 
                                return 'green';
                            }

                            var data = [
                                  <#assign separator = ''>
                                  <@s.Category max=2147483647 tag="">
                                          ${separator}
                                          {
                                              "label": "${s.categoryValue.label?js_string}",
                                              "count": "${s.categoryValue.count?c}",
                                              "url": "${s.CategoryUrl(main.contentAuditorLink)?js_string}#collection-test-content-auditor-tab-2",
                                              "colour": colourForYear(${s.categoryValue.label?js_string})
                                           }
                                          <#assign separator = ','>
                                  </@s.Category>
                                ];

                            data.sort(function(a,b) { return a.label - b.label; });

                            if(data.length  < 1){
                              setTimeout(function(){

                                $('#date-modified-chartdiv').parent().replaceWith(noContentFiller());

                              },888);
                            }

                            content_auditor.dateModifiedChart = AmCharts.makeChart( "date-modified-chartdiv", {
                              "type": "serial",
                              "dataProvider": data,
                              "graphs": [ {
                                "type": "column",
                                "valueField": "count",
                                "lineAlpha": 0.2,
                                "fillColorsField": "colour",
                                "lineColors": "colour",
                                "fillAlphas": 0.75
                              } ],
                              "categoryField": "label",
                              "height": 300,
                               "valueAxes": [{
                                        "axisAlpha": 1,
                                        "position": "left",
                                        "title": "Pages"
                                    }],
                                    "categoryAxis": {
                                        "gridPosition": "start",
                                        "fillAlpha": 0.05,
                                        "position": "left",
                                        "title": "Year"
                                    },
                            } );

                            content_auditor.dateModifiedChart.addListener("clickGraphItem", navigateToDataContextUrl);

                        </script>
                      </div>
                  </@s.Facet>

                  <@s.Facet name="Response Time">
                      <div class="panel panel-default">
                        <div class="panel-heading">
                          <h3 class="panel-title"><@s.FacetLabel tag="strong" link=main.contentAuditorLink/></h3>
                        </div>
                        <div class="panel-body">
                          <div id="response-time-chartdiv" style="width: 100%; height: 300px;"></div>
                        </div>
                        <script type="text/javascript">
                        
                            function gradeResponseTime(time){
                                if(time == 5001)
                                    return '#DF0000';
                                else if(time == 4001)
                                    return '#DF0000';
                                else if(time == 3001)
                                    return '#DF0000';
                                else if(time == 2001)
                                    return '#DF0000';
                                else if(time == 1000)
                                    return '#E94B00';
                                else if(time == 501)
                                    return '#F49600';    
                                else if(time == 201)
                                    return '#FFE100';
                                else if(time == 101)
                                    return '#AAC51D';    
                                else if(time == 21)
                                    return '#55AA3A';
                                else if(time == 0)
                                    return '#008F58';    
                                else 
                                    return '#888';
                            }
                            function cateogroiseResponseTime(time){
                                if(time == 5001)
                                    return 'Unworthy of the web';
                                else if(time == 4001)
                                    return 'Painfully Slow';
                                else if(time == 3001)
                                    return 'Annoyingly Slow';
                                else if(time == 2001)
                                    return 'Very Slow';
                                else if(time == 1000)
                                    return 'Slow';
                                else if(time == 501)
                                    return 'Sluggish';    
                                else if(time == 201)
                                    return 'Normal';
                                else if(time == 101)
                                    return 'Quite Fast';    
                                else if(time == 21)
                                    return 'Awesomely Fast';
                                else if(time == 0)
                                    return 'Extremely Super Fast';    
                                else 
                                    return 'Not Ranked';
                            }
                            
                            var data = [
                                  <#assign separator = ''>
                                  <@s.Category max=2147483647 tag="">
                                          ${separator}
                                          {
                                              "sort": "${s.categoryValue.label?keep_before("-")?js_string}",
                                              "description": cateogroiseResponseTime(${s.categoryValue.label?keep_before("-")?js_string}),
                                              "label": "${s.categoryValue.label?js_string}",
                                              "count": "${s.categoryValue.count?c}",
                                              "colour": gradeResponseTime(${s.categoryValue.label?keep_before("-")?js_string}),
                                              "url": "${s.CategoryUrl(main.contentAuditorLink)?js_string}#collection-test-content-auditor-tab-2"
                                          }
                                          <#assign separator = ','>
                                  </@s.Category>
                                ];


                            if(data.length  < 1){
                              setTimeout(function(){

                                $('#response-time-chartdiv').parent().replaceWith(noContentFiller());

                              },888);
                            }    
                            //alert(JSON.stringify(data, null, 4));
                            data.sort(function(a,b) { return a.sort - b.sort; });
                            
                            content_auditor.responseTimeChart = AmCharts.makeChart( "response-time-chartdiv", {
                              "type": "serial",
                              "dataProvider": data,
                              "graphs": [{
                                    "type": "column",
                                    "valueField": "count",
                                    "fillAlphas": 0.75,
                                    "lineAlpha": 0.2,
                                    "fillColorsField": "colour",
                                    "lineColors": "colour",
                                    "balloonText": "<b>[[count]]</b> pages take between <b>[[category]]</b> milliseconds to load. <br> This is considered: <b> [[description]] </b>" 
                              }],
                                    "valueAxes": [{
                                        "axisAlpha": 1,
                                        "position": "left",
                                        "title": "Pages"
                                    }],
                                    "categoryAxis": {
                                        "gridPosition": "start",
                                        "fillAlpha": 0.05,
                                        "position": "left",
                                        "title": "Response Time (milliseconds)"
                                    },
                              "categoryField": "label",
                              "height": 300
                             
                            });
                            
                            content_auditor.responseTimeChart.addListener("clickGraphItem", navigateToDataContextUrl);
                            
                        </script>
                      </div>
                  </@s.Facet>
                  
                  <@s.Facet name="Undesirable Text">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                          <h3 class="panel-title"><@s.FacetLabel tag="strong" link=main.contentAuditorLink/></h3>
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
                  </@s.Facet>
                  
                
                </@s.FacetedSearch>
            </div>
        </div>
    </div>
