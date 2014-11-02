///* 0. Global Variables */
//var jsonUrl = 'http://localhost:8084/s/search.json';
//var nullQueryString = '?collection=test-shakespeare&query=-padrenull';
//var activeQueryString = '?' + $('#fb-query-string').text();
//var jsonReport;
//var jsonQuery;
//var totalDocuments;
//
///* 1. Document Ready */
//$(document).ready(function () {
//  if($('body').hasClass('after-search')) {
//    $.ajax({
//      dataType: 'json',
//      url: jsonUrl + activeQueryString,
//      success: function(data) {
//        jsonQuery = data;
//      },
//      complete: function(data) {
//        initFunnelback();
//        trace(jsonQuery);
//      }
//    });
//  } else {
//    initFunnelback();
//  }
//});
//
//function initFunnelback() {
//
//  // Query completion setup.
//  $("#query").fbcompletion({
//    'enabled'    : 'enabled',
//    'collection' : 'business-gov-internet',
//    'program'    : '/search/../s/suggest.json',
//    'format'     : 'extended',
//    'alpha'      : '.5',
//    'show'       : '10',
//    'sort'       : '0',
//    'length'     : '3',
//    'delay'      : '0',
//    'profile'    : '_default'
//  });
//
//  // Load content containers
//  $('.fb-ajax-get').each(function () {
//    var $ajaxContentContainer = $(this);
//    var contentUrl = $ajaxContentContainer.data('fb-get-url');
//    $.ajax({
//      type: 'GET',
//      url: contentUrl,
//      dataType: 'html',
//      success: function(data) {
//        $ajaxContentContainer.html(data);
//      },
//      complete: function(data) {
//        // if the container is the report details container, prepare the report graphs
//        if($ajaxContentContainer.hasClass('fb-report-details')) {
//          totalDocuments = $('.data-total-doc-count').data('value');
//          if($('body').hasClass('initial-form')) {
//            initReportGraphs();
//          }
//        }
//
//        if($ajaxContentContainer.hasClass('fb-initial-facets')) {
//          $('.facet-graph-button').click(function(e) {
//            var $graphButton = $(this);
//            e.preventDefault();
//            var fn = $graphButton.parent().parent().find('.facetLabel').text();
//            var facetName = fn.trim();
//            for (var f in jsonReport.response.facets) {
//              var facet = jsonReport.response.facets[f];
//              if(facet.name == facetName) {
//                var facetChartData = graphData(facet, 8);
//                initFacetGraph(facetChartData, facetName);
//                $graphButton.parent().parent().after($('#fb-facet-graph'));
//              }
//            }
//          });
//          /*
//          $('.facet-graph-button').fancybox({
//            maxWidth  : '100%',
//            maxHeight : 600,
//            fitToView : false,
//            width   : '80%',
//            height    : '90%',
//            autoSize  : false,
//            closeClick  : false,
//            openEffect  : 'none',
//            closeEffect : 'none'
//          });
//          */
//        }
//
//        if($ajaxContentContainer.hasClass('fb-after-search-facets')) {
//          $('.facet-graph-button').click(function(e) {
//            var $graphButton = $(this);
//            e.preventDefault();
//            var fn = $graphButton.parent().parent().find('.facetLabel').text();
//            var facetName = fn.trim();
//            for (var f in jsonQuery.response.facets) {
//              var facet = jsonQuery.response.facets[f];
//              if(facet.name == facetName) {
//                var facetChartData = graphData(facet, 8);
//                initFacetGraph(facetChartData, facetName);
//                $graphButton.parent().parent().after($('#fb-facet-graph'));
//              }
//            }
//          });
//        }        
//      }
//    })
//  });
//}
//
//function initReportGraphs() {
//
//  // Load searchJson
//  $.ajax({
//    dataType: 'json',
//    url: jsonUrl + nullQueryString,
//    success: function(data) {
//      // jsonReport = $.parseJSON(data);
//      jsonReport = data;
//      for (var f in jsonReport.response.facets) {
//        var facet = jsonReport.response.facets[f];
//        if(facet.name == 'Missing Content Attributes') {
//          // var metadataHealthChartData = graphData(facet, 99);
//          var metadataHealthChartData = graphInverseData(facet, 99, totalDocuments);
//
//          metadataHealthChartData.unshift({"attribute":"Total documents", "count":totalDocuments, "colour":"#A2B5C5"});
//
//          // Prepare metadata health chart
//          var chart;
//          chart = new AmCharts.AmSerialChart();
//          chart.autoMargins = true;
//          chart.startDuration = 1;
//          chart.dataProvider = metadataHealthChartData;
//          chart.categoryField = "attribute";
//          chart.rotate = true;
//          chart.addTitle("Metadata Health",16,"#000000",1,true);
//          
//          // Category Axis Y
//          var categoryAxis = chart.categoryAxis;
//          categoryAxis.gridPosition = "start";
//          categoryAxis.axisColor = "#DADADA";
//          categoryAxis.title = "Metadata Attributes";
//          categoryAxis.fillAlpha = 1;
//          categoryAxis.gridAlpha = 0;
//          categoryAxis.fillColor = "#FAFAFA";
//
//          // Value Axis X
//          var valueAxis = new AmCharts.ValueAxis();
//          valueAxis.axisColor = "#DADADA";
//          valueAxis.title = "Document Count";
//          valueAxis.gridAlpha = 0.1;
//          chart.addValueAxis(valueAxis);
//
//          // Preapre metadata health graph
//          var graph = new AmCharts.AmGraph();
//          graph.title = "Income";
//          graph.valueField = "count";
//          graph.type = "column";
//          graph.balloonText = "Documents with [[category]] metadata applied:[[value]]";
//          graph.lineAlpha = 0;
//          graph.fillColors = "#afd9ae";
//          graph.fillColorsField = "colour";
//          graph.fillAlphas = 1;
//
//          // Add the graph to the chart
//          chart.addGraph(graph);
//
//          chart.creditsPosition = "top-right";   
//          chart.write('metadata-health-chart');    
//
//        } 
//
//        if(facet.name == 'Date modified') {
//          var chart;
//          var chartColours = ["#de4c4f", "#d8854f", "#eea638", "#a7a737", "#86a965", "#8aabb0", "#69c8ff", "#cfd27e", "#9d9888", "#916b8a", "#724887", "#7256bc"];
//          var contentAgeChartData = graphData(facet, 7);
//          chart = AmCharts.makeChart("content-age-chart", {
//            type: 'pie',
//            dataProvider: contentAgeChartData,
//            titleField: "attribute",
//            valueField: "count",
//            sequencedAnimation: true,
//            startEffect: ">",
//            innerRadius: "20%",
//            startDuration: 0.5,
//            labelRadius: 10,
//            balloonText: "[[title]]<br><span style='font-size:14px'><b>[[value]]</b> ([[percents]]%)</span>",
//            colors: chartColours
//          });
//          chart.addTitle("Content Age",14,"#000000",1,true);
//          chart.write("content-age-chart");
//        } 
//      }
//    }
//  }) 
//}
//
//function initFacetGraph(facetChartData, facetName) {
//  /*
//  var chart;
//  var chartColours = ["#FF0F00", "#FF6600", "#FF9E01", "#FCD202", "#F8FF01", "#B0DE09", "#04D215", "#0D8ECF", "#0D52D1", "#2A0CD0", "#8A0CCF", "#CD0D74", "#754DEB", "#DDDDDD", "#999999", "#333333", "#000000", "#57032A", "#CA9726", "#990000", "#4B0C25"];
//
// 
//  chart = new AmCharts.AmPieChart();
//
//  // title of the chart
//  chart.addTitle(facetName, 16);
//
//  chart.dataProvider = facetChartData;
//  chart.titleField = "attribute";
//  chart.valueField = "count";
//  chart.sequencedAnimation = false;
//  chart.startEffect = ">";
//  chart.innerRadius = "20%";
//  chart.startDuration = 0.5;
//  chart.labelRadius = 10;
//  chart.balloonText = "[[title]]<br><span style='font-size:14px'><b>[[value]]</b> ([[percents]]%)</span>";  
//  chart.colors = chartColours;
//
//  $('#fb-facet-graph').attr("style", "width: 90%; height: 600px;");
//
//  // WRITE                                 
//  chart.write("fb-facet-graph");  
//  */
//  var chart;
//  var chartColours = ["#de4c4f", "#d8854f", "#eea638", "#a7a737", "#86a965", "#8aabb0", "#69c8ff", "#cfd27e", "#9d9888", "#916b8a", "#724887", "#7256bc"];
//  $('#fb-facet-graph').attr("style", "width: 90%; height: 600px;");
//
//  chart = AmCharts.makeChart("fb-facet-graph", {
//    // addTitle: [facetName, 16],
//    type: 'pie',
//    dataProvider: facetChartData,
//    titleField: "attribute",
//    valueField: "count",
//    sequencedAnimation: true,
//    startEffect: ">",
//    innerRadius: "20%",
//    startDuration: 0.5,
//    labelRadius: 10,
//    balloonText: "[[title]]<br><span style='font-size:14px'><b>[[value]]</b> ([[percents]]%)</span>",
//    colors: chartColours
//  });
//}
//
//// this function receives the facet name and category, and returns a JSON arrow which is to be used by amCharts
//function graphData(facet, max) {
//  var facetGraphArray = [];
//  var counter = 0;
//  var maxCount = 0;
//  for (var c in facet.categories) {
//    var facetCategory = facet.categories[c];
//    for (var v in facetCategory.values) {
//      counter++;
//      if(counter <= max) {
//        var facetValue = facetCategory.values[v];
//        // trace(facetValue.label + ' ' + facetValue.count);
//        facetGraphArray.push({"attribute":facetValue.label.toString(), "count":facetValue.count});        
//      } else if (counter > max) {
//        maxCount += facetValue.count;
//      }
//    }
//  } 
//  if (maxCount > 0) {
//    facetGraphArray.push({"attribute":"Other", "count":maxCount});
//  }
//  // trace(facetGraphArray);
//  return facetGraphArray;
//}
//
//function graphInverseData(facet, max, subtract) {
//  var facetGraphArray = [];
//  var counter = 0;
//  var maxCount = 0;
//  for (var c in facet.categories) {
//    var facetCategory = facet.categories[c];
//    for (var v in facetCategory.values) {
//      counter++;
//      if(counter <= max) {
//        var facetValue = facetCategory.values[v];
//        // trace(facetValue.label + ' ' + facetValue.count);
//        var inverseCount = subtract - facetValue.count;
//        trace(inverseCount);
//        facetGraphArray.push({"attribute":facetValue.label.toString(), "count":inverseCount});        
//      } else if (counter > max) {
//        maxCount += facetValue.count;
//      }
//    }
//  } 
//  if (maxCount > 0) {
//    facetGraphArray.push({"attribute":"Other", "count":maxCount});
//  }
//  // trace(facetGraphArray);
//  return facetGraphArray;
//}
//
///* X. Debugging */
//function trace(s) {
//  if ('console' in self && 'log' in console) console.log(s)
//  // the line below is commented out - enable it if you want to see debugging alerts in IE
//  //else alert(s)
//}
//
///* X. Debugging */
//function trace(s) {
//  if ('console' in self && 'log' in console) console.log(s)
//  // the line below is commented out - enable it if you want to see debugging alerts in IE
//  //else alert(s)
//}  
