
<#-- Funtion for sub-ordinal No.s. Credit to : http://christiancox.com/2012/08/freemarker-ordinal-number-suffix/ -->
<#function getOrdinalSuffix cardinal="notSet">
  <#assign ext='' />
  <#assign testCardinal=cardinal%10 />
  <#if (cardinal%100 < 21 && cardinal%100 > 4)>
    <#assign ext='th' />
  <#else>
    <#if (testCardinal<1)>
      <#assign ext='th' />
    <#elseif (testCardinal<2)>
      <#assign ext='st' />
    <#elseif (testCardinal<3)>
      <#assign ext='nd' />
    <#elseif (testCardinal<4)>
      <#assign ext='rd' />
    <#else>
      <#assign ext='th' />
    </#if>
  </#if>
  <#return ext>
</#function>
<#setting number_format="computer">
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#compress>
<!DOCTYPE html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7 sticky"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8 sticky"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9 sticky"> <![endif]-->
<!--[if gt IE 8]><!-->
<html class="no-js sticky">
<!--<![endif]-->
<head>
    <meta charset="utf-8">

    <!--[if IE]><meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"><![endif]-->
    <title>Content Optimiser | Funnelback</title>
    <meta name="description" content="Funnelback Content Optimiser">
    <meta name="viewport" content="width=device-width">

    <link rel="shortcut icon" href="${ContextPath}/content-optimiser/img/favicons/favicon.ico" />

    <link rel="stylesheet" href="${ContextPath}/content-optimiser/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ContextPath}/content-optimiser/css/font-awesome.min.css">

    <link rel="stylesheet" href="${ContextPath}/content-optimiser/css/content-optimiser.css">

</head>

<#assign documentWasFound = response.optimiserModel.selectedDocument?? />
<#assign query = response.resultPacket.query />
<#assign collection = response.resultPacket.collection />
<#assign queryUrl = question.inputParameterMap["optimiser_url"] />
<#assign matchingPages = response.resultPacket.resultsSummary.fullyMatching />

<#if documentWasFound >
    <#assign selectedRank = response.optimiserModel.selectedDocument.rank />
    <#assign selectedUrl = response.optimiserModel.selectedDocument.displayUrl />
    <#assign selectedTitle = response.optimiserModel.selectedDocument.title />
    <#assign totalWords = response.optimiserModel.content.totalWords?string.number />
    <#assign uniqueWords = response.optimiserModel.content.uniqueWords?string.number />

    <!-- Assume 10 results per page -->
    <#assign resultsPerPage = 10>

    <#assign nPagesFromStart = (selectedRank / resultsPerPage)?int>
    <#if nPagesFromStart == 1>
        <#assign pagesFromStart = "<strong>" + nPagesFromStart + "</strong> page">
    <#else>
        <#assign pagesFromStart = "<strong>" + nPagesFromStart + "</strong> pages">
    </#if>
</#if>

<#if RequestParameters.profile??>
    <#assign profile = "&profile=" + RequestParameters.profile>
<#else>
    <#assign profile = "">
</#if>

<body id="fb-co">

<!--[if lt IE 7]>
            <p class="chromeframe">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> or <a href="http://www.google.com/chromeframe/?redirect=true">activate Google Chrome Frame</a> to improve your experience.</p>
 <![endif]--> 

<header>
    <nav role="navigation" class="navbar navbar-inverse navbar-static-top">
    <div class="container">

        <div class="navbar-header">
           <a href="?${profile}" title="Funnelback Content Optimiser"><i class="navbar-brand"><i class="visible-md visible-lg">- &nbsp; Content Optimiser</i></i></a> 
        </div>

        <div class="navbar-right pull-right">
		

    <#--
      <a href="content-optimiser.html?collection=${collection}&optimiser_url=${queryUrl}" class="btn pull-left link-query" title="Change Query"><span class="hidden-xs sr-xs"><i class="fa fa-filter fa-lg"></i></span></a> 
		   <div class="btn-group dropdown-menu-right pull-right collections-top-menu">
			
			<a href="content-optimiser.html" class="btn btn-default dropdown-toggle link-collections" data-toggle="dropdown" title="Content Optimiser Collections List"><span class="hidden-xs sr-xs"><i class="fa  fb-icn-collections"></i></span></a>
			
			
		<div aria-labelledby="collection-menu" role="menu" class="dropdown-menu box">
        <div class="header fa fb-icn-collections

        <h4><i class="fa fb-icn-collections"></i> Collections</h4> 
            
        </div>
        
        
        <div class="body">
       
        	<div><small>Current: <strong>${collection}</strong></small></div>
           <div><small>Query: <strong>${query}</strong></small></div>
        </div>
        
        <ul id="catch-collection-list">
           
        </ul>
        </div>
		</div>
		
		-->
  </div>
    </div>
    </nav>
</header>

<div id="app"> 

  <div class="container">

    <div class="row">
      <div id="main" class="col-md-12">
        <div id="co-cs" class="box">
          <div class="header">
            <h3>Ranking Summary</h3>
          </div>
          <!-- / .header -->

          <#if documentWasFound>
          <div id="co-cs-title" class="pane pt20 pb10">
            <div class="row">
              <div class="col-xs-2 text-center"><b class="text-grey-lt">Title:</b></div>
              <div class="col-xs-10 pl15"> <a class="text-grey-dk" target="new" title="Open the page: ${selectedUrl}" href=${selectedUrl}>${selectedTitle} <i class="fa fa-external-link fs11 pl5 pr5"></i></a> </div>
            </div>
          </div>
          <!-- / .pane -->
          </#if>

          <div class="body pb0 pt0">
            <div class="row">

              <#if documentWasFound>
              <div class="col-sm-2 text-center p0 m0">

                <#if selectedRank == 1>
                    <#assign ribbonColour = "green">
                <#elseif (selectedRank <= 10)>
                    <#assign ribbonColour = "orange">
                <#else>
                    <#assign ribbonColour = "red">
                </#if>

                <div class="ribbon ${ribbonColour}"><i>Rank</i><b>${selectedRank}<sup>${getOrdinalSuffix(selectedRank)}</sup></b></div>


                <#-- TODO: add this as a thumbnail preview of the website, maybe use a ajax to load since it will take time to generate from the server -->

                <#-- Deprecated: 

                    <div class="of rel text-grey-md z1"><i>of</i></div>
                    <div class="pages text-grey-md">${matchingPages} Pages</div>
                -->
              </div>
              </#if>

              <div class="col-sm-10 p0 m0 bl1">
                <div>
                <div class="stack-btns pull-right">

                </div>
                  <form id="co-cs-form" method="get" action="content-optimiser.html" class="form-horizontal disguise" role="form">
                    <div class="form-group m0 pt15">
                      <div class="col-sm-1 m0">
                        <label for="form_query">Query</label>
                      </div>
                      <div class="col-sm-11">
                        <input class="form-control text-disguise" type="text" value="${query}" id="form_query" name="query">
                        <div class="co-form-edit-btn"><i class="fa fa-edit"></i><span class="hidden-xs"> Edit</span></div>
                      </div>
                    </div>
                    <div class="form-group m0">
                      <div class="col-sm-1 m0">
                        <label for="form_url">URL</label>
                      </div>
                      <div class="col-sm-11 mb15">
                        <input class="form-control text-disguise" type="text" value="${queryUrl}" name="optimiser_url" id="form_url">
                      </div>
                    </div>
                    <div class="form-group m0 disguise-hide">
                      <div class="col-sm-1 m0 mt10"> </div>
                      <div class="col-sm-11 mb15">
                        <button class="btn btn-default btn-orange btn-sm pull-left mb15" type="submit">Optimise <i class="fa fa-arrow-circle-right"></i></button>
                      </div>
                    </div>

                    <input type="hidden" name="collection" value="${collection}" />
                    <input type="hidden" name="loaded" value="1" />
                    <#if RequestParameters.profile??>
                        <input type="hidden" name="profile" value="${RequestParameters.profile}" />
                    </#if>

                  </form>

                  <#if documentWasFound>
                  <div id="co-cs-summary" class="pane pt15 pb15 text-grey">
                      This document places <strong class="text-green">${selectedRank}<sup>${getOrdinalSuffix(selectedRank)}</sup></strong>
                      amongst a total of <strong>${matchingPages}</strong> fully-matching documents
                      when a query for <strong>${query}</strong> is run,
                      <#if (nPagesFromStart >= 1)>
                        and is placed approximately ${pagesFromStart} away from the first page of the search results.
                      <#else>
                        and is shown on the first page of the search results.
                      </#if>
                  </div>
                  </#if>

                </div>
              </div>
            </div>
          </div>
          <!-- / .body -->

        
          <!-- / .footer --> 

        </div>
        <!-- / .box -->
        
        <#if documentWasFound>
        <div id="co-stats" class="bump">
          <div class="row">
            <div class="col-sm-2 col-xs-4"><b class="">${totalWords}</b><span>Total Words</span></div>
            <div class="col-sm-2 col-xs-4"><b class="">${uniqueWords}</b><span class="spacing-fix">Unique Words</span></div>
            <div class="col-sm-2 col-xs-4"><b class="">${matchingPages}</b><span>Pages Found</span></div>
          </div>
        </div>
        </#if>
        <!-- / #co-stats -->

        <!--Start #co-comparison-->
        <div id="co-comparison" class="box">
          
              <div class="header">
                <h3>Top-ranked results for '${query}'</h3>
              </div>

              <div class="body p0">

                    <div class="m20">

                   <#if ( (!documentWasFound) || (selectedRank > 10) ) > 
                                            <div class="alert alert-info">
                            <button type="button" class="close" data-dismiss="alert" aria-hidden="true"><i class="fa fa-times"></i></button>
                            <#if (documentWasFound) > 
                                <h4><i class="fa fa-exclamation-triangle"></i>&nbsp Low Ranking!</h4>
                                <div>The selected document was not in the top 10 results for the query: <strong><em>${query}</em></strong>.</div>
                                <div>Your document (<strong>ranked ${selectedRank}</strong>) is shown below the top 10 results for comparison.</div>
                            <#else> 

                                
                                <#if (queryUrl?string?length > 0) >

                                    <h4><i class="fa fa-exclamation-triangle"></i>&nbsp Document Not Found</h4>
                                    <div>The URL provided does not rank in the top 10 results within the collection: <strong>${collection}</strong>.</div>

				    <#if (matchingPages < 1) >
					<p>There were no results returned for this query.</p>
				    <#elseif (matchingPages <= 10) >
                                        <p>All ${matchingPages} matching results for the query: <strong>${query}</strong> are shown below.</p>
                                    <#else >
                                        <p>The top 10 results for this collection that match the query: <strong>${query}</strong> are shown below.</p>
                                    </#if>

                                <#else>

                                    <h4><i class="fa fa-exclamation-triangle"></i>&nbsp No Url!</h4>
                                    <div>No url was entered.</div>

                                </#if>

                            </#if>
                            <p><a href="?collection=${collection}${profile}" class="btn btn-sm btn-primary mt10">
                               &laquo; Back</a>
                            </p>
                        </div>
                    </#if> 
					  
					  <#if (!documentWasFound) >
							
							
					  </#if>
                    <#if (matchingPages > 0) >
                        <!-- Top Ranking Chart and Table -->
                        <div id="chart-top-ten"></div>
                        <div id="chart-top-ten-legend"></div>
                        <table id="ls-top-rank-url" class="table table-condensed table-hover table-fbco mt20 mb30">
                            <tr class="head"><th>Rank</th><th>Page</th><th>URL</th></tr>
                            <!-- The rest of the table data here is populated by JS -->
                        </table>
                    </#if>
                </div>
            </div>             
        </div>
        <!--End #co-comparison-->  

    <!-- Start #co-advice -->
    <#if documentWasFound>
    <div class="box" id="co-advice">
        <div class="header">
            <h3>Optimisation Tips</h3>
        </div>

        <div class="body p0">
            <div class="m20">
			
                <p>
                    <div>Follow the tips below to better optimise the current page and boost its page rank upwards</div>
                    <!--<div><small id="chart-top-ten-desc"><i class="fa fa-info-circle"></i> Click on a section to view detailed instructions</small>-->
                    </div>
                </p>
            </div>
<div class="panel-group" id="accordion"> 

            <#assign hintCounter = 0>            
            <#assign visibleHintCounter = 0>
            <#list response.optimiserModel.hintCollections as hc> <!-- [content, URL, link based, etc.] -->

                <#if (matchingPages < 10)>
                    <#assign pagesToList = matchingPages>
                <#else>
                    <#assign pagesToList = 10>
                </#if>

                <#-- This section deals with determining which sections to show -->
                <#assign displayableSections = 0>
                <#list hc.hints as hint>
                    <#list 1..pagesToList as i>
                        <#-- If there is another page that does better for this factor: -->
                        <#if (hint.scores[i?string] > hint.scores[selectedRank?string]) >
                            <#if hint.name == "content">
                                <#assign displayableSections = displayableSections + 1>
                            </#if>
                            <#list hint.hintTexts as text>
                                <#assign displayableSections = displayableSections + 1>
                            </#list>
                            <#break>
                        </#if>
                    </#list>
                </#list>

                <#assign hintCounter = hintCounter + 1>

                <#if (displayableSections > 0) >
               
			        <#assign visibleHintCounter = visibleHintCounter + 1>

                    <div class="panel mt30 band-${hintCounter}">
                        <div class="panel-heading">
                            <div class="panel-title">
                                <div class="row">
                                    <div class="col-xs-10 mb0">
                                        <div class="title ml10 mr10">
                                            <div class="col-xs-2 col-sm-1">
                                                <span class="order fa-stack fa-lg ">
                                                    <i class="fa fa-circle fa-stack-2x"></i>
                                                    <i class="order-value">${visibleHintCounter}</i>
                                                </span>
                                            </div>
                                            <div class="col-xs-10 col-sm-11">
                                                <#--<a data-parent="#accordion" href="#collapse_${hintCounter}">-->
                                                    <h2 class="title">${hc.name}</h2>
                                                <#--</a>-->
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-xs-2 mb0 text-right">
                                        <span class="pr15 pt5 block">
                                            <#--<i class="fa fa-caret-down"></i>-->
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div id="collapse_${hintCounter}">

                            <div class="col-sm-offset-1">
                                                              
                                    <#list hc.hints as hint>

                                        <#-- Only show this if this factor for our page is not the best.
                                            so, try to find something better -->

                                        <#-- Our page's ranking factor -->
                                        <#assign rf = hint.scores[selectedRank?string]>
                                        <#assign foundBetter = false>

                                        <#list 1..pagesToList as i>
                                            <#if (hint.scores[i?string] > rf) >
                                                <#assign foundBetter = true>                                                
                                                <#break>
                                                <!-- Found better! -->
                                            </#if>
                                        </#list>

                                        <#if foundBetter>
                                        
                                            <#assign divId = "graph_hc" + hintCounter + "_hn" + hint.name >

                                            <div class="chart-wrapper row-fluid">

                                                <div class="col-md-5 mt30">
                                    <ol class="tip-list">      
										            <#if hint.name == "content">
                                                        <li>
                                                            The most common words in the page are <strong>${response.optimiserModel.content.commonWords?html}</strong>.
                                                            These words should be an indicator of the subject of the page. If the words don't accurately reflect the subject of the page, consider re-wording the page, or preventing sections of the page from being indexed by wrapping the section with <strong>&lt;!--noindex--&gt;</strong> and <span><strong>&lt;!--endnoindex--&gt;</strong></span> tags.
                                                        </li>
                                                    </#if>
                                                    <#list hint.hintTexts as text>
                                                        <li>${text}</li>                                  
                                                    </#list>
													</ol>
                                                </div>

                                                <div class="col-md-7">
                                                    <div class="tips-chart" id="${divId}" >
                                                        <p><i>Please put "${hint.name}" graph in this div "${divId}"</i></p>
                                                    </div>
                                                </div>
                                            </div>
                                        </#if>
                                    </#list>
                                
                            </div>
                        </div>
                    </div>
                
                </#if>
            </#list>
        </div>
    </div>
    </#if>
    <!-- END #co-advice -->
</div>
    </div>
    <!--end main-->

    </div>

  </div>
  <!-- /container --> 

</div>
<!-- end content -->

<footer class="hidden-xs">
	<div class="container">
		<div class="col-sm-12">
			<p id="copyright">&copy; 2006 - 2014  <a title="Funnelback Support Hours" href="http://funnelback.com" target="_blank">Funnelback</a> - All rights reserved.</p>
		</div>
	</div>		
</footer>


<script src="${ContextPath}/content-optimiser/js/modernizr-latest.js"></script>
<!--Needs to be replaced if we're in production mode --> 
<!--<script src="../common/vendor/less-1.7.0.min.js"></script>-->
<script src="${ContextPath}/content-optimiser/js/jquery-1.11.0.min.js"></script> 
<script src="${ContextPath}/content-optimiser/js/bootstrap.min.js"></script> 

<!--FB Content Optimiser-->
<script src="${ContextPath}/content-optimiser/js/content-optimiser.js"></script>
    
<!--AM Charts-->
<script src="${ContextPath}/content-optimiser/js/amcharts/amcharts.js"></script>
<script src="${ContextPath}/content-optimiser/js/amcharts/xy.js"></script>
<script src="${ContextPath}/content-optimiser/js/amcharts/serial.js"></script>

<script>

  
  $(document).ready(function(){

    setTimeout(function(){
        $('.url-link').tooltip();
    },1000);

  });

	//parameters into JS object (for use with content-optimiser.js)

	contentOptimiser = [];
	contentOptimiser.query = '${query}';
	contentOptimiser.target_url = '${queryUrl}';


  // AM Charts + related here forward... 
$(function () {


	$('#accordion').on('shown.bs.collapse', function(e){
        id = $(e.target).attr('href');        
        chartdiv_id = $(id).find('.chartdiv').attr('id');                        
        doChart(chartdiv_id, true);
    });

    function suffix(n) {
        var d = (n|0)%100;
        return d > 3 && d < 21 ? 'th' : ['th', 'st', 'nd', 'rd'][d%10] || 'th';
    };

    /* Start top ranking breakdown - AMCcharts JS*/
	
    var topRankingBreakdown = [];

    topRankingBreakdown.labelFunction = function(valueText, serialDataItem, categoryAxis) {

    //Misuse this function to turn '-1' into '...' for the graph y axisAlpha
        if (serialDataItem.dataContext.index == -1) {
            return '...';
        } else {
            return serialDataItem.dataContext.index + suffix(serialDataItem.dataContext.index);
        }
    }

    topRankingBreakdown.exportConfig  = {
        menuTop: 'auto',
        menuLeft: 'auto',
        menuRight: '0px',
        menuBottom: '0px',
        menuItems: [{
        textAlign: 'center',
        icon: '../amcharts/images/export.png',
        iconTitle: 'Save chart as an image',
        onclick: function () {},
        items: [
            {title: 'JPG',format: 'jpg'}, 
            {title: 'PNG',format: 'png'},
            {title: 'SVG',format: 'svg'},
            {title: 'PDF',format: 'pdf'}]
        }],
        menuItemOutput:{
            fileName:"Funnelback Content Optimiser Top Ranking Breakdown for : stheurlinquestion.com "
        },
        menuItemStyle: {
            backgroundColor: 'transparent',
            rollOverBackgroundColor: '#EFEFEF',
            color: '#000000',
            rollOverColor: '#CC0000',
            paddingTop: '6px',
            paddingRight: '6px',
            paddingBottom: '6px',
            paddingLeft: '6px',
            marginTop: '0px',
            marginRight: '0px',
            marginBottom: '0px',
            marginLeft: '0px',
            textAlign: 'left',
            textDecoration: 'none'
        }
    };

    topRankingBreakdown.graphs = [
        <#list response.optimiserModel.hintsByName?keys as hintkey>
            <#assign hint = response.optimiserModel.hintsByName[hintkey] />
            {
                "balloonText": "<div style=\"text-align:left;padding:10px\"><b>[[title]]</b><br>Contributing percentage: <b>[[value]]%</b></b>",
                "fillAlphas": 0.75,
                "lineThickness": 0.6,
                "type": "column",
                //"valueField": "content",
                "valueField": "${hintkey}",
                //"title": "Content",     //TODO: One of these should be a long name
                "title": "${hintkey}"
            },
        </#list>
    ]

    topRankingBreakdown.data = [

        /* For each result in the top 10 */
        <#list response.optimiserModel.topResults as topResult>
        {
            "page_name":      "${topResult.title}",
            "url":           '${topResult.displayUrl}',
            "index":          ${topResult.rank},
            "url_truncated":  '<#if (topResult.displayUrl?length > 30)>${truncateURL(topResult.displayUrl,30)?replace("<br/>", "... ")}<#else>${topResult.displayUrl}</#if>', 

            /* For each Cooler Weight */
            <#list response.optimiserModel.hintsByName?keys as hintkey>
                <#assign hint = response.optimiserModel.hintsByName[hintkey] />
                "${hintkey}": ${hint.scores[topResult.rank?string]?string("0.00")},
            </#list>                    
        },
        </#list>

        /* If your result is present (but after rank 10), put in a separator and your result */
        <#if (documentWasFound && selectedRank > 10) >

            /* This is the '...' separator */
            {"page_name": '...',
                "url": '...',
                "index": -1,
                "url_truncated": '...'},

            /* This is your >10th result */
            {"page_name"        : '${selectedTitle}',
                "url"           : '${selectedUrl}',
                "index"         : ${selectedRank}, 
                "url_truncated" : '${truncateURL(selectedUrl, 30)}',

            /* For each Cooler Weight */
            <#list response.optimiserModel.hintsByName?keys as hintkey>
                <#assign hint = response.optimiserModel.hintsByName[hintkey] />
                "${hintkey}": ${hint.scores[selectedRank?string]?string("0.00")},
            </#list>
            }
        </#if>
    ];

    // make the top ranking breakdown chart
    var chart = AmCharts.makeChart("chart-top-ten", {

        "type": "serial",
        "theme": "none",
        "rotate":true,
        "colors": [
          '#0D8ECF','#0D52D1','#2A0CD0','#8A0CCF','#CD0D74','#754DEB','#FF0F00',
          '#FF6600','#FF9E01','#F5DA70','#FFD840','#ECFF66','#A8ED6A','#5EE165'
        ],
        // Gives a Pastel Appearance
        // "colors": [
        //     '#FFD840','#ECFF66','#A8ED6A','#5EE165',
        //     '#8EE7E0','#8EE5E7','#DCD2EB','#FF7EA9','#FF7EFA','#89D0D9','#FF9E01','#B1B0EB','#EBC0B0','#B0E0EB'
        // ],

        "titles": [{
            "id": "top-ranking-breakdown",
            "size": 12,
            "text": "Score",
            "color":"#505050"}
        ],

        "graphs": topRankingBreakdown.graphs,
        "columnWidth": 0.85,
        "urlField":"url",
        //textClickEnabled:true,#B1B0EB
        "rollOverGraphAlpha":0.5,
         "urlTarget":"_blank",

        "clustered":false,
        "categoryField": "url",
        "categoryAxis": {
            labelFunction:topRankingBreakdown.labelFunction ,
            //"gridPosition": "bottom",
            "position": "left",
            //"labelRotation":30,
            "offset": 0,
            "title": "Rank",
            "titleColor": "#555",
            "color":"#444",
            "axisColor": '#666',
            
            //inside:true,
            //labelsEnabled:false,
            //centerLabelOnFullPeriod:false,
            gridThickness:0.5,
            "guides": [{
                //The category is equal to the url in question.

                category: "${queryUrl}",
                //toCategory: "http://www.demourl.com",
                behindColumns:false,
                lineColor: "#ff0055",
                lineAlpha: 1,
                fillAlpha: 1,
                fillColor: "#ff0055",
                gridAlpha:1,
                dashLength: 3,
                inside: true,
                //Not too sure what this is yet --
                <#if documentWasFound> label: 
                //"${queryUrl}", 
                'Current Page ->',
                </#if>
                color:"#fff"
            }]
        },
        
        "dataProvider": topRankingBreakdown.data,
        
        "valueAxes": [{
            "stackType": "regular",
            //"stackType": "100%",
            "axisAlpha": 1,
            "axisColor": '#505050',
            "color": '#505050',
            "gridAlpha": 1,
            "gridColor": '#ffffff',
            "behindColumns":false
        }],
        "exportConfig": topRankingBreakdown.exportConfig 
    });

    var legend = new AmCharts.AmLegend();
    legend.horizontalGap =0;
    legend.fontSize = 10;
    legend.marginRight = 0;
    legend.rollOverGraphAlpha = 0.1;
    legend.rollOverColor = '#ff6500';
    legend.textClickEnabled = false;
    chart.addLegend(legend, "chart-top-ten-legend");


//    $('#chart-top-ten-legend').after('<small id="chart-top-ten-desc"><!--<i class="fa fa-exclamation-circle"></i>--><i class="fa fa-info-circle"></i> click keys to compare and analyse</small>');

    
    $.each($(topRankingBreakdown.data),function(i,v) {


        var pos = i + 1;
        var hl;
        var rank;

        switch (true) {

            //Decorate the top 10, including your result if necessary
            case pos <= 10:
                <#if documentWasFound>
                    hl = pos === ${selectedRank} ? 'highlight' : '';
                </#if>
                rank = pos+suffix(pos);
                break;
            
            <#if documentWasFound>
            //Decorate the separator (always at position 11 if present)
            case pos == 11:
                hl = '';
                rank = '...';
                break;

            //Decorate your result (if position 12 exists, it is your result)
            case pos == 12:
                hl = 'highlight';
                rank = ${selectedRank} + suffix(${selectedRank});
                break;
            </#if>
        }
    
        //Make the separator unclickable...
        if (pos == 11) {

            $("#ls-top-rank-url")
                .append("<tr data-url=\""+urlToVisit+"\" class=\"rank-"+rank+" "+hl+" \">"
                    +  "<td>"+rank+"</td>"
                    +  "<td><div class=\"title-link\">"+v.page_name+"</div></td>"
                    +  "<td class=\"hidden-xs\"><div class=\"url-link\">"+v.url_truncated+"</div></td>"
                + "</tr>");

        //...But the actual results clickable
        } else {

            var urlToVisit = "content-optimiser.html?query=${query}&optimiser_url=" + v.url + "&collection=${collection}${profile}&loaded=1";
            var toolTip = "Run Content Optimiser for '${query}' on '" + v.url + "'";

            $("#ls-top-rank-url")
                .append("<tr data-url=\""+urlToVisit+"\" class=\"rank-"+rank+" "+hl+" \">"
                    +  "<td>"+rank+"</td>"
                    +  "<td><a class=\"title-link\" href=\""+urlToVisit+"\" target=\"_self\" title=\""+toolTip+"\">"+v.page_name+"</a></td>"
                    +  "<td class=\"hidden-xs\"><a class=\"url-link\" href=\""+v.url+"\" target=\"_fbOut\" title=\"Visit page "+v.url+"\" data-toggle=\"tooltip\" data-placement=\"top\">"+v.url_truncated+"</a></td>"
                + "</tr>");

        }
    });

    $("#ls-top-rank-url").on('click', 'tbody > tr > td', function(e) {

              var anchor  =     $(this).find('a');
              var href    =     anchor.attr('href');
              var target  =     anchor.attr('target');

              if(target && href) {
                window.open(href,target);
              }

              e.preventDefault();
              
    });
});

<#if documentWasFound>
$( function () {

    function suffix(n) {
        var d = (n|0)%100;
        return d > 3 && d < 21 ? 'th' : ['th', 'st', 'nd', 'rd'][d%10] || 'th';
    };

    console.log ("Entering graph function");

    <#assign hintCounter = 0>            
    <#list response.optimiserModel.hintCollections as hc>
        <#assign hintCounter = hintCounter + 1>        

        <#list hc.hints as hint>

            <#-- Only show this if this factor for our page is not the best. so, try to find something better -->

            <#-- Our page's ranking factor -->
            <#assign rf = hint.scores[selectedRank?string]>
            <#assign foundBetter = false>

            <#if (matchingPages < 10)>
                <#assign pagesToList = matchingPages>
            <#else>
                <#assign pagesToList = 10>
            </#if>
            
            <#list 1..pagesToList as i>
                <#if (hint.scores[i?string] > rf) >
                    // Found better! 
                    <#assign foundBetter = true>                                                
                    <#break> 
                </#if>
            </#list>

            <#if foundBetter >
                <#assign divId = "graph_hc" + hintCounter + "_hn" + hint.name >

                //Start preparing the chart             
                chartWrapper = [];

                chartWrapper.chartData1 = [
                    <#list 1..pagesToList as i>
                        {
                            "rank": "${i}" + suffix(${i}),
                            "score": ${hint.scores[i?string]?string("0.00")},
                            "currentPageScore": ${rf?string("0.00")},
                            
                            <#if i == selectedRank>
                                "color":"#FFEB70",
                                "lineColor": "#FF0",
                                "alpha" : 0.6,
                                "dashLengthLine":3,
                                "lineThick": 9
                            </#if>
                            
                        },
                    </#list>

                    <#if (selectedRank > 10) >
                            {"rank": "${selectedRank}" + suffix(${selectedRank}),
                            "score": ${rf?string("0.00")},
                            "currentPageScore": ${rf?string("0.00")},
                            // if the current page fits into the ranks 
                            "color":"#FFDD22",
                            "lineColor": "#FF0",
                            "alpha" : 0.6,
                            "dashLengthLine":3,
                            "lineThick": 9}   
                    </#if>
                ];

                AmCharts.makeChart("${divId}",
                {
                    "type": "serial",
                    "pathToImages": "http://cdn.amcharts.com/lib/3/images/",
                    "categoryField": "rank",
                    "autoMarginOffset": 0,
                    "marginRight": 10,
                    "marginLeft": 0,
                    "marginTop": 5,
                    "fontSize": 12,
                    "color": '#505050',
                    //"fillColors":"#FF0099",
                    "theme": "none",
                    "chartCursor": {},
                    //"chartScrollbar": {},
                    //"trendLines": [],
                    //"legend": [],
                    "graphs": [
                        {
                            "columnWidth": 0.5,
                            "cornerRadiusTop": 2,
                            "lineColor": "#FF7F00",
                            "id": "chart-1",
                            //"title": "Top URLs",
                            "type": "column",
                            "dashLengthField": "dashLengthLine",
                            //"lineThicknessField": "lineThick",
                            "colorField": "color",
                            "lineAlpha": 1,
                            "fillAlphas": 0.75,
                            "valueField": "score",
                            "alphaField": "alpha",
                            "title": "Page Score",
                        },
                        {
                            "bullet": "round",
                            "bulletBorderAlpha": 1,
                            "bulletBorderThickness": 1,
                            "bulletSize": 1,
                            "lineColor": "#FF0000",
                            //"id": "AmGraph-2",
                            "lineThickness": 1.5,
                            "title": "Current URL",
                            "valueField": "currentPageScore",
                        }
                    ],
                    
                    //add in the category of the current page if it sits in the top 10
                    "guides": [{
                        
                        category: ( "${selectedRank}" + suffix(${selectedRank})  ),
                        //toCategory: "http://www.demourl.com",
                        behindColumns:false,
                        lineColor: "#ff0088",
                        lineAlpha: 1,
                        fillAlpha: 1,
                        fillColor: "#ff0088",
                        gridAlpha:1,
                        dashLength: 3,
                        inside: true,
                        //label: "Current Page",
                        color:"#000",
                        "rotate":false,
    
                    }],
                    "valueAxes": [
                        {
                            "id": "ValueAxis-1",
                            "title": "Score"
                        }
                    ],
                    
                    "categoryAxis":
                        {
                            "id": "ValueAxis-1",
                            "title": "Rank",
                            "gridPosition": "start",
                            "labelRotation": 25
                        }
                    ,
                    //"allLabels": [],
                    //"balloon": {},
                    "titles": [{
                        "class": "title-1",
                        "size": 12,
                        "text": "${hint.name} scores",
                        "color":"#303030",
                        "marginBottom":0
                        
                    }],
                    "dataProvider": chartWrapper.chartData1
                }
            );
            </#if>
        </#list>
    </#list>
});
</#if>


</script> 
</body>
</html>
</#compress>