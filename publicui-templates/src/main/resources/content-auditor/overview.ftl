    <div id="facet-container-wrapper" style="margin:0" class="row">
        <div class="col-md-12 no-border no-padding">
            <div class="fb-facet-header"> <h3><span class="facetLabel">Overview</span></h3></div>
            
            <@appliedFacetsBlock urlHash="#collection-${currentCollection}-tab-0"/>

            <div class="facets">
                <#assign categoryMax = question.collection.configuration.value("ui.modern.content-auditor.overview-category-count")?number />
                <@s.FacetedSearch>
                <@s.Facet>

                <#assign FacetLabel><@s.FacetLabel summary=false /></#assign>
                <#assign FacetLabel = FacetLabel?replace("<[^>]*>", "", "r")?trim >
                    <#if FacetLabel != 'Reading Grade' && FacetLabel != 'Date modified' && FacetLabel != 'Response Time' && FacetLabel != 'Duplicate Titles' && FacetLabel != 'Undesirable Text'
                     && FacetLabel != 'Missing Metadata' && FacetLabel != 'URI'>
                    <#-- Exclude the facets we already show elsewhere -->

                      <#assign categoryCount = 0 />
                      <#assign sep = '' />
                      <div>
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h3 class="panel-title"><@s.FacetLabel tag="strong" link=main.contentAuditorLink/></h3>
                            </div>

                            <#-- Count up the number of category values there will be, so we can show the number -->
                            <#assign countOfCategoryValues = 0 />
                            <@s.Category max=2147483647 tag="">
                                <#assign countOfCategoryValues = countOfCategoryValues + 1 />
                            </@s.Category>

                           <#assign textNoContent = 'No information avaliable'> 
                            <#if countOfCategoryValues < 1>

                                <div class="no-info">
                                  <div class="inner">
                                    ${textNoContent}
                                  </div>
                                </div>      

                            </#if>

                            <script type="text/javascript">
                              function navigateToDataContextUrl(event) {
                                if (event.item.dataContext.url != undefined) {
                                  window.location = event.item.dataContext.url;
                                }
                              }
                            </script>

                            <div class="panel-body">

                                <@s.Category max=categoryMax tag="div">
                                <#assign categoryCount = categoryCount + 1 />
                                
                                ${sep} <@s.CategoryName class="" link=main.contentAuditorLink extraParams="#collection-${currentCollection}-tab-0" />&nbsp;<small class="text-muted">(<@s.CategoryCount />)</small>
                                
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
