<#ftl encoding="utf-8" />
<#compress>
<!DOCTYPE html>
<!--[if lt IE 7]> <html id="fb-co" class="sticky no-js lt-ie9 lt-ie8 lt-ie7" lang="en"> <![endif]-->
<!--[if IE 7]> <html id="fb-co" class="sticky no-js lt-ie9 lt-ie8" lang="en"> <![endif]-->
<!--[if IE 8]> <html id="fb-co" class="sticky no-js lt-ie9" lang="en"> <![endif]-->
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
    <div id="app">
        <div class="container text-center">
            <a target="_fbcom" href="http://funnelback.com"><img class="fb-logo center" src="${ContextPath}/content-optimiser/img/fb-logo-lg.png" alt="Funnelback - Content Optimiser"/></a>
            <div class="loading"></div>
            <h1>Generating Content Optimisation Report for:</h1>
            <ul class="no-bullets">
                <li><strong>Query:</strong> ${query}</li>
                <#if optimiser_url?? >
                    <li><strong>URL:</strong> ${optimiser_url}</li>
                </#if>
            </ul>
        </div>
    </div>
    <script src="${ContextPath}/content-optimiser/js/modernizr-latest.js"></script>
    <script src="${ContextPath}/content-optimiser/js/jquery-1.11.0.min.js"></script>
    
    <#if optimiser_url?? >
        <#assign maybeUrl = "&optimiser_url=" + optimiser_url>
    <#else>
        <#assign maybeUrl = "">
    </#if>

    <meta http-equiv="refresh" content="0; url='?query=${query}${maybeUrl}&collection=${collection}&loaded=1">
</body>
</html>

</#compress >