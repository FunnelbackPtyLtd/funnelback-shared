    <div id="facet-container-wrapper" style="margin:0" class="row">
        <div class="col-md-12 no-border no-padding">
            <div class="fb-facet-header"> <h3><span class="facetLabel">Recommendations</span></h3></div>
            
            <@appliedFacetsBlock urlHash="#collection-${currentCollection}-tab-recommendations"/>

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
                          <div id="reading-grade-chartdiv" class="chart-container" style="width: 100%; height: 300px;"></div>
                        </div>
                        <script type="text/javascript">
                            function colourForGrade(grade){
                              if(
                                  grade < ${(question.collection.configuration.value("ui.modern.content-auditor.reading-grade.lower-ok-limit")!("6"))?number?c} 
                                  || 
                                  grade > ${(question.collection.configuration.value("ui.modern.content-auditor.reading-grade.upper-ok-limit")!("16"))?number?c}
                                )
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
                                              "url":   "${s.CategoryUrl(main.contentAuditorLink)?js_string}#collection-test-content-auditor-tab-2",
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

                            content_auditor.readingGradeChart = AmCharts.makeChart( "reading-grade-chartdiv", {
                              "type": "serial",
                              "marginTop":0,
                              "dataProvider": data,
                              "pathToImages": "content-auditor/assets/img/amcharts/",
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
                                
                            <#assign key = "ui.modern.content-auditor.facet-metadata." + s.categoryValue.label />
                            <#if s.categoryValue?exists>
                                <span>
                                    <a href="${s.CategoryUrl(main.contentAuditorLink,"#collection-test-content-auditor-tab-2")?html}">
                                      <#if question.collection.configuration.hasValue(key)>
                                        ${question.collection.configuration.value(key)?html}
                                      <#else>
                                        Metadata Class: ${s.categoryValue.label?html}
                                      </#if>
                                    </a>
                                </span>
                            </#if>
                            &nbsp;
                            <small class="text-muted">(<@s.CategoryCount />)</small>

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
                          <div id="date-modified-chartdiv" class="chart-container" style="width: 100%; height: 300px;"></div>
                        </div>
                        <script type="text/javascript">
                            function colourForYear(year){
                              var now = new Date().getFullYear();
                              if(year < (now - ${(question.collection.configuration.value("ui.modern.content-auditor.date-modified.ok-age-years")!("2"))?number?c}) || year > now)
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
                              "pathToImages": "content-auditor/assets/img/amcharts/",
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
                          <div id="response-time-chartdiv" class="chart-container" style="width: 100%; height: 300px;"></div>
                        </div>
                        <script type="text/javascript">
                        
                            function gradeResponseTime(time){
                                if(time >= 5001)
                                    return '#DF0000';
                                else if(time >= 4001)
                                    return '#DF0000';
                                else if(time >= 3001)
                                    return '#DF0000';
                                else if(time >= 2001)
                                    return '#DF0000';
                                else if(time >= 1000)
                                    return '#E94B00';
                                else if(time >= 501)
                                    return '#F49600';    
                                else if(time >= 201)
                                    return '#FFE100';
                                else if(time >= 101)
                                    return '#AAC51D';    
                                else if(time >= 21)
                                    return '#55AA3A';
                                else if(time >= 0)
                                    return '#008F58';    
                                else 
                                    return '#888';
                            }
                            function categoriseResponseTime(time){
                                if(time >= 5001)
                                    return 'Too Slow';
                                else if(time >= 4001)
                                    return 'Painfully Slow';
                                else if(time >= 3001)
                                    return 'Annoyingly Slow';
                                else if(time >= 2001)
                                    return 'Very Slow';
                                else if(time >= 1000)
                                    return 'Slow';
                                else if(time >= 501)
                                    return 'Sluggish';    
                                else if(time >= 201)
                                    return 'Normal';
                                else if(time >= 101)
                                    return 'Quite Fast';    
                                else if(time >= 21)
                                    return 'Very Fast';
                                else if(time >= 0)
                                    return 'Extremely Fast';    
                                else 
                                    return 'Not Ranked';
                            }

                            function cleanDecimal(val){
                               if(val == 0 ){ 
                                return val; 
                               }
                               else if(val >= 40000 ){
                                return '+';
                               } 
                                
                                val = (val / 1000).toFixed(2).replace('0.','.').replace('.00','');

                                return val;      
                            }

                            function constructLabel(labelStart, labelEnd){

                                var constructed = (cleanDecimal(labelStart) + ' to ' + cleanDecimal(labelEnd)).replace('0s','s').replace(' to +','+');                
                                return constructed;
                            }
                            
                            var data = [
                                  <#assign separator = ''>
                                  <@s.Category max=2147483647 tag="">
                                          ${separator}
                                          {
                                              "sort": "${s.categoryValue.label?keep_before("-")?js_string}",
                                              "description": categoriseResponseTime(${s.categoryValue.label?keep_before("-")?js_string}),
                                              "label": constructLabel(${s.categoryValue.label?keep_before("-")?js_string}, ${s.categoryValue.label?keep_after("-")?js_string}),
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
                                    "pathToImages": "content-auditor/assets/img/amcharts/",
                                    "balloonText": "<b>[[count]]</b> pages take between <b>[[category]]</b> seconds to load. <br> This is considered: <b> [[description]] </b>" 
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
                                        "title": "Response Time (seconds)"
                                    },
                              "categoryField": "label",
                              "height": 300
                             
                            });
                            
                            content_auditor.responseTimeChart.addListener("clickGraphItem", navigateToDataContextUrl);
                            $('#response-time-chartdiv').html().appendTo('.recommendations .facets');
                            
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

                <div id="duplicateContentTab" class="facet">
                  <div class="panel panel-default">
                      <div class="panel-heading">
                        <h3 class="panel-title"><strong class="facetLabel">Duplicate Content </strong><#if (extraSearches.duplicates.response.resultPacket.resultsSummary.collapsed > 0) ><span id="duplicateCount" class="badge badge-danger"> ${extraSearches.duplicates.response.resultPacket.resultsSummary.collapsed} </span></#if></h3>
                      </div>
                      <div class="panel-body">
                        <@fb.ExtraResults name="duplicates">
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
                              <@s.Results>
                                <#if s.result.collapsed??>
                                  <tr>
                                    <td class="text-center">
                                      <a class="text-muted duplicates-count" href="?${QueryString}&amp;duplicate_signature=%3F:${s.result.collapsed.signature}#collection-${currentCollection}-tab-2">
                                        <div class="badge badge-danger"> x <strong>${s.result.collapsed.count + 1}</strong>
                                                        </div>
                                    </td>
                                            <td class="text-center">
                                                        ${ fb.renderSize(s.result.fileSize) }
                                            </td>
                                            <td class="text-center">
                                                    <i>${fb.renderSize((s.result.collapsed.count + 1) * s.result.fileSize)}</i>
                                    </td>
                                    <td>
                                      <div class="pull-left">
                                        <a href="?${QueryString}&amp;duplicate_signature=%3F:${s.result.collapsed.signature}#collection-${currentCollection}-tab-2" title="${s.result.title?html}" class="clickable-link"><strong>${s.result.title?html} </strong></a>
                                        <span class="fa fa-open"></span>
                                        <br>
                                        <!-- SITE (Z) -->
                                        <a class="text-muted" href="?${QueryString}&amp;duplicate_signature=%3F:${s.result.collapsed.signature}#collection-${currentCollection}-tab-2"> ${s.result.liveUrl?html}
                                        </a>
                                      </div>
                                    </td>
                                  </tr>
                                </#if>
                              </@s.Results>

                            </tbody>
                          </table>
                          <div class="text-center">
                            <ul class="pagination">
                              <@fb.Prev link=main.contentAuditorLink startParamName="duplicate_start_rank"><li><a href="${fb.prevUrl}" rel="prev"><small><i class="glyphicon glyphicon-chevron-left"></i></small> Prev</a></li></@fb.Prev>
                              <@fb.Page link=main.contentAuditorLink startParamName="duplicate_start_rank"><li <#if fb.pageCurrent> class="active"</#if>><a href="${fb.pageUrl}">${fb.pageNumber}</a></li></@fb.Page>
                              <@fb.Next link=main.contentAuditorLink startParamName="duplicate_start_rank"><li><a href="${fb.nextUrl}" rel="next">Next <small><i class="glyphicon glyphicon-chevron-right"></i></small></a></li></@fb.Next>
                            </ul>
                          </div>
                        </@fb.ExtraResults>
                      </div>
                  </div>
                </div>


            </div>



        </div>
    </div>

