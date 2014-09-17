<#ftl encoding="utf-8" />
<#setting number_format="computer">
<#compress>
<!DOCTYPE html>
<!--[if lt IE 7]>      <html id="fb-co" class="sticky no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html id="fb-co" class="sticky no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html id="fb-co" class="sticky no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!-->
<html id="fb-co" class="sticky no-js">
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

<script src="${ContextPath}/content-optimiser/js/jquery-1.11.0.min.js"></script>
<!--[if lt IE 9]>
	<script src="${ContextPath}/content-optimiser/js/html5shiv.min.js"></script>
	<script src="${ContextPath}/content-optimiser/js/ie10-viewport-bug-workaround.js"></script>
	<script src="${ContextPath}/content-optimiser/js/respond.min.js"></script>
<![endif]-->

</head>

<body id="fb-co-hm">
	<!--[if lt IE 7]><p class="chromeframe">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> or <a href="http://www.google.com/chromeframe/?redirect=true">activate Google Chrome Frame</a> to improve your experience.</p><![endif]-->
	<div id="app">
		<div class="container">

			<a target="_blank" href="?"><img
				class="fb-logo" src="${ContextPath}/content-optimiser/img/fb-logo-lg.png"
				alt="Funnelback - Content Optimiser" /></a>

			<div class="row bump">

				<form id="co-form-main" method="GET" action="" enctype="application/x-www-form-urlencoded">
					<div class="col-md-5">
						<div class="form-group">
							<label for="query" class="sr-only">Query</label>
							<input type="text" placeholder="Query" id="query"
								class="form-control query" name="query" tabindex="1" value="" required>
						</div>
					</div>

					<div class="col-md-5">
						<div class="form-group">
							<label for="optimiser_url" class="sr-only">Target URL</label>
							<input type="text" placeholder="Target URL" id="optimiser_url"
								value="" name="optimiser_url" class="optimiser_url form-control"
								tabindex="2">
						</div>
					</div>

					<div class="col-sm-2">
					<label for="optimiser_url" class="sr-only">&nbsp;</label>
						<button id="co-opt-btn" type="submit" class="btn btn-orange btn-lg" tabindex="3">
							<i class="fa fa-arrow-circle-right"></i>&nbsp; Optimise
						</button>
					</div>
                    <input type="hidden" name="collection" value="${collection?url}" />
					<#if RequestParameters.profile??>
                    <input type="hidden" name="profile" value="${RequestParameters.profile?url}" />    
                    </#if>
					
                </form>
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
</body>
</html>
</#compress>