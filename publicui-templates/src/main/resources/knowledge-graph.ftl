<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en-us">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="robots" content="nofollow">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <script src="${ContextPath}/${GlobalResourcesPrefix}thirdparty/es6-promise-4.2.5/es6-promise.auto.min.js"></script>
  <title>Funnelback Knowledge Graph</title>
  <link rel="stylesheet" href="${ContextPath}/${GlobalResourcesPrefix}thirdparty/font-awesome-5.7.2/css/font-awesome.min.css" />
  <link rel="stylesheet" href="${ContextPath}/${GlobalResourcesPrefix}thirdparty/nprogress-0.2.0/nprogress.min.css" />
  <link rel="stylesheet" href="${ContextPath}/${GlobalResourcesPrefix}css/funnelback.knowledge-graph-2.7.0.min.css" />
  <!--[if lt IE 9]>
    <script src="${ContextPath}/${GlobalResourcesPrefix}thirdparty/html5shiv.js"></script>
    <script src="${ContextPath}/${GlobalResourcesPrefix}thirdparty/respond.min.js"></script>
  <![endif]-->
  <style>
    .loader {color: #aaa; position: fixed; top: 45%; left: calc(53% - 144px);}
    .loader .text {font-family: "Open Sans"; font-size: 20px; display: inline-block; vertical-align: middle; margin-bottom: 24px; margin-left: 8px;}
  </style>
</head>
<body>
<div class="loader"><span class="fa fa-fw fa-3x fa-spinner fa-pulse"></span><span class="text">Loading...</span></div>
<script type="text/javascript" src="${ContextPath}/${GlobalResourcesPrefix}thirdparty/jquery-3.3.1/jquery.min.js"></script>
<script type="text/javascript" src="${ContextPath}/${GlobalResourcesPrefix}thirdparty/nprogress-0.2.0/nprogress.min.js"></script>
<script type="text/javascript" src="${ContextPath}/${GlobalResourcesPrefix}thirdparty/momentjs-2.22.2/moment.min.js"></script>
<script type="text/javascript" src="${ContextPath}/${GlobalResourcesPrefix}thirdparty/handlebars-4.0.12/handlebars.min.js"></script>
<script type="text/javascript" src="${ContextPath}/${GlobalResourcesPrefix}js/funnelback.knowledge-graph-2.7.0.min.js"></script>
<script type="text/javascript">
  jQuery(document).ready(function() {
    jQuery(document).knowledgeGraph({
      apiBase: window.location.origin,
      collection: '${collectionId}',
      profile: '${profileId}',
      <#noautoesc>targetUrl: '${targetUrl?js_string}',</#noautoesc>
      trigger: 'full',
      searchUrl: window.location.origin + '/s/search.json',
    });
  });
</script>
</body>