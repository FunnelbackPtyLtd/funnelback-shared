<!DOCTYPE html>
<!--[if lt IE 7]>      <html id="fb-co" class="sticky no-js lt-ie9 lt-ie8 lt-ie7" lang="en"> <![endif]-->
<!--[if IE 7]>         <html id="fb-co" class="sticky no-js lt-ie9 lt-ie8" lang="en"> <![endif]-->
<!--[if IE 8]>         <html id="fb-co" class="sticky no-js lt-ie9" lang="en"> <![endif]-->
<!--[if gt IE 8]><!-->
<html id="fb-co" class="sticky no-js" lang="en">
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

<body>


<header>
    <nav role="navigation" class="navbar navbar-inverse navbar-static-top">
    <div class="container">

        <div class="navbar-header">
           <a href="?" title="Funnelback Content Optimiser"><i class="navbar-brand"><i class="visible-md visible-lg">- &nbsp; Content Optimiser</i></i></a> 
        </div>

       </div>
    </nav>
</header>

<div id="app">



  <div class="container">
  
    <div class="box mt10">
      <div class="header">
        <h3>Anchors Summary</h3>
      </div>
      <!-- / .header -->
      
      <div class="pane">
        <div class="well mt5 mb5">
          <p><strong>URL:</strong> <a href="http://www.abc.net.au/atthemovies/txt/s3044692.htm">http://www.abc.net.au/atthemovies/txt/s3044692.htm </a></p>
          <p><strong>Collection:</strong> <span>demo-v12-classifier</span></p>
          <p><strong>Showing:</strong> documents <strong>1</strong> - <strong>1</strong> of <strong>1</strong> total documents</p>
        </div>
      </div>
      <!-- / .pane -->
      
      <div class="body p0">

        <p id="chart-sankey"></p>
        <table class="table table-hover  mb0">
          <thead>
            <tr>
              <th><span class="hidden-xs">Link</span> Type</th>
              <th>Anchor text</th>
              <th>Within-collection links</th>
              <th>Ext<span class="hidden-xs">ernal</span> links</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td> within site </td>
              <td><a data-content="HTML WILL BE ISERTED HERE AFTER JSON REQUEST" title="" data-toggle="popover" href="?collection=demo-v12-classifier&amp;docnum=00000784&amp;anchortext=%5Bk2%5DMachete" data-original-title="Machette">Machete</a></td>
              <td>1 </td>
              <td>0 </td>
            </tr>
            <tr>
              <td> within site </td>
              <td><a data-toggle="popover" href="?collection=demo-v12-classifier&amp;docnum=00000784&amp;anchortext=%5Bk2%5DMore" data-content="HTML WILL BE ISERTED HERE AFTER JSON REQUEST" title="" data-original-title="More">More</a></td>
              <td>1 </td>
              <td>0 </td>
            </tr>
            <tr>
              <td> within site </td>
              <td><a href="?collection=demo-v12-classifier&amp;docnum=00000784&amp;anchortext=%5Bk2%5Datthemovies%20img%202010%20ep40%20machete%20small%20jpg%20Machete">at themovies img 2010 ep40 machete small jpg Machete</a></td>
              <td>1 </td>
              <td>0 </td>
            </tr>
          </tbody>
          <tfoot class="bg-yellow-lightest">
            <tr>
              <td class="text-right" colspan="2"><strong>Total:</strong></td>

              <#-- maybe we should have if value > 3 (or some significant No.) add a class of:  class="label label-success"  on the strong? (Tested it and it looks great!) ~ Steve -->
              <td><strong>3</strong></td>
              <td><strong>0</strong></td>
            </tr>
          </tfoot>
        </table>
      </div>
      <!-- / .body -->
      
      <div class="footer" id="footer"> <a class="btn btn-sm btn-default no-shadow" onclick="javascript:history.go(-1);" href="#" id="btn-prev-page"><i class="fa fa-arrow-circle-left"></i> Previous Page</a> </div>
      
      <!-- / .footer --> 
      
    </div>
  </div>
</div>

<footer class="hidden-xs">

		<!-- start: Container -->
		<div class="container">
			<div class="col-sm-12">
				<p id="copyright">
					&copy; 2006 - 2014 <a title="Funnelback Support Hours"
						href="http://funnelback.com" target="_blank">Funnelback</a> - All
					rights reserved.
				</p>
			</div>
		</div>
		<!-- end: Container  -->

	</footer>

<#-- START SANKEY/D3 scripts -->	
<script type="text/javascript" src="${ContextPath}/content-optimiser/js/d3.js"></script>
<script type="text/javascript" src="${ContextPath}/content-optimiser/js/sankey.js"></script>
<script>

var margin = {top: 15, right: 15, bottom: 15, left: 15},
    width = 1080 - margin.left - margin.right,
    height = 500 - margin.top - margin.bottom;

var formatNumber = d3.format(",.0f"),
    format = function(d) { return formatNumber(d) + " TWh"; },
    color = d3.scale.category20();

var svg = d3.select("#chart-sankey").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var sankey = d3.sankey()
    .nodeWidth(15)
    .nodePadding(10)
    .size([width, height]);

var path = sankey.link();

d3.json("${ContextPath}/content-optimiser/json/demo.json", function(energy) {

  sankey
      .nodes(energy.nodes)
      .links(energy.links)
      .layout(32);

  var link = svg.append("g").selectAll(".link")
      .data(energy.links)
    .enter().append("path")
      .attr("class", "link")
      .attr("d", path)
      .style("stroke-width", function(d) { return Math.max(1, d.dy); })
      .sort(function(a, b) { return b.dy - a.dy; });

  link.append("title")
      .text(function(d) { return d.source.name + " → " + d.target.name + "\n" + format(d.value); });

  var node = svg.append("g").selectAll(".node")
      .data(energy.nodes)
    .enter().append("g")
      .attr("class", "node")
      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
    .call(d3.behavior.drag()
      .origin(function(d) { return d; })
      .on("dragstart", function() { this.parentNode.appendChild(this); })
      .on("drag", dragmove));

  node.append("rect")
      .attr("height", function(d) { return d.dy; })
      .attr("width", sankey.nodeWidth())
      .style("fill", function(d) { return d.color = color(d.name.replace(/ .*/, "")); })
      .style("stroke", function(d) { return d3.rgb(d.color).darker(2); })
    .append("title")
      .text(function(d) { return d.name + "\n" + format(d.value); });

  node.append("text")
      .attr("x", -6)
      .attr("y", function(d) { return d.dy / 2; })
      .attr("dy", ".35em")
      .attr("text-anchor", "end")
      .attr("transform", null)
      .text(function(d) { return d.name; })
    .filter(function(d) { return d.x < width / 2; })
      .attr("x", 6 + sankey.nodeWidth())
      .attr("text-anchor", "start");

  function dragmove(d) {
    d3.select(this).attr("transform", "translate(" + d.x + "," + (d.y = Math.max(0, Math.min(height - d.dy, d3.event.y))) + ")");
    sankey.relayout();
    link.attr("d", path);
  }
});

</script>
<#-- END SANKEY/D3 scripts -->
</body>
</html>
