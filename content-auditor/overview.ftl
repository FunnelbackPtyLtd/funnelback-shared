    <div id="facet-container-wrapper" style="margin:0" class="row">
        <div class="col-md-12 no-border no-padding">
            <div class="fb-facet-header"> <h3><span class="facetLabel">Overview</span></h3></div>
            
            <@appliedFacetsBlock urlHash="#collection-${currentCollection}-tab-0"/>

            <div class="overviews">
                <#assign categoryMax = question.collection.configuration.value("ui.modern.content-auditor.overview-category-count")?number />
                <@s.FacetedSearch>
                <@s.Facet>
                <#assign categoryCount = 0 />
                <#assign sep = '' />
                <div class="col-md-6">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="panel-title"><@s.FacetLabel tag="strong"/></h3>
                        </div>

                        <div class="panel-body">

                            <@s.Category max=categoryMax tag="div">
                            <#assign categoryCount = categoryCount + 1 />
                            
                            ${sep} <@s.CategoryName class="" link="content-auditor.html" />&nbsp;<small class="text-muted">(<@s.CategoryCount />)</small>
                            
                            <#assign sep = '' />
                            </@s.Category>
                            
                        </div>

                        <#-- Count up the number of category values there will be, so we can show the number -->
                        <#assign countOfCategoryValues = 0 />
                        <@s.Category max=2147483647 tag="">
                            <#assign countOfCategoryValues = countOfCategoryValues + 1 />
                        </@s.Category>

                        <#if categoryCount == categoryMax>
                        <div class="panel-footer">
                            <a class="btn btn-xs btn-primary" data-toggle="tab" href="#collection-${currentCollection}-tab-1" aria-expanded="true" title="View All ${countOfCategoryValues}" data-chart_ref="chart_${s.facet_index}" onClick="facetTabShow(${s.facet_index})"> View All ${countOfCategoryValues} <span class="fa fa-arrow-right"></span></a>
                        </div>
                        </#if>
                    </div>
                </div>
                
                </@s.Facet>
                
                </@s.FacetedSearch>
            </div>
        </div>
    </div>
