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
<title>SEO Auditor | Funnelback</title>
<meta name="description" content="Funnelback SEO Auditor">
<meta name="viewport" content="width=device-width">

<link rel="shortcut icon" href="${ContextPath}/content-optimiser/img/favicons/favicon.ico" />

<link rel="stylesheet" href="${ContextPath}/content-optimiser/css/bootstrap.min.css">
<link rel="stylesheet" href="${ContextPath}/content-optimiser/css/font-awesome.min.css">
<link rel="stylesheet" href="${ContextPath}/content-optimiser/css/content-optimiser.css">

<script src="${ContextPath}/content-optimiser/js/jquery-1.11.0.min.js"></script>
<!--[if lt IE 9]>
	<script src="${ContextPath}/content-optimiser/js/html5shiv.min.js"></script>
	<script src="${ContextPath}/content-optimiser/js/ie10-viewport-bug-workaround.js"></script>
	<script src="${ContextPath}/content-optimiser/js/respond.min.js"></script>
<![endif]-->

</head>

<#if RequestParameters.profile??>
    <#assign profile = "&profile=" + RequestParameters.profile?url>
<#else>
    <#assign profile = "">
</#if>

<body>
	<div id="app">
		<div class="container">
			<div class="text-center">
				<a href="http://funnelback.com" target="_fbcom"><img alt="FunnelBack - SEO Auditor" src="${ContextPath}/content-optimiser/img/fb-logo-lg.png" class="fb-logo center" style="margin-top: 200px;"></a>
			</div>
			<div class="row col-sm-8 col-md-6 col-md-offset-3 col-sm-offset-2">
				<div class="box well p0">
					<div class="header">
						<h2>${AllCollections?size} Collections</h2>
					</div>
					<div class="pane">
						<h4>Select a collection to optimise</h4>
					</div>
					<div class="body p0">
						<ul class="fb-list">
							<#list AllCollections as oneCollection>
								<li><a href="?collection=${oneCollection.id}${profile}">
									<i class="fa fa-arrow-circle-right pull-right"></i>${oneCollection.configuration.value("service_name")}
								</a></li>
							</#list>
						</ul>
					</div>
				</div>
			</div>
		</div>
	</div>


	
</body>
</html>
