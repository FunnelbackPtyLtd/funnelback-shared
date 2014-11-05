<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>

<#-- Assign global variables -->

<#assign serviceName0>${question.inputParameterMap["collection"]!?html}</#assign>
<#assign serviceName1>TODO</#assign>
<#assign serviceName2>TODO</#assign>
<#assign serviceName3>TODO</#assign>

<#assign collectionId0>${question.inputParameterMap["collection"]!?html}</#assign>
<#assign collectionId1>TODO</#assign>
<#assign collectionId2>TODO</#assign>
<#assign collectionId3>TODO</#assign>

<#-- Contents of the HTML <head> tag -->
<#macro Head>
<head>
    <title>
        <@s.AfterSearchOnly>${question.inputParameterMap["query"]!?html}${question.inputParameterMap["query_prox"]!?html}<@s.IfDefCGI name="query">,</@s.IfDefCGI></@s.AfterSearchOnly>
          <#--  <@s.cfg>service_name</@s.cfg>--> Sample - Content Master
    </title>

    <link rel="icon" href="/s/content-auditor/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="/s/content-auditor/favicon.ico" type="image/x-icon"/>  
    
    <!-- BOOTSTRAP -->
    <link href="/s/content-auditor/bootstrap.min.css" rel="stylesheet" media="screen">    
    <link href="/s/content-auditor/jquery.fancybox.css" rel="stylesheet" media="screen">
    <link href="/s/content-auditor/style.css" rel="stylesheet" media="screen">    
    <style type="text/css">
        .nowrap {
            white-space: nowrap;
        }
    </style>

    <!-- amcharts JS -->
    <!-- TODO - Must use our local copy -->
    <script type="text/javascript" src="http://www.amcharts.com/lib/3/amcharts.js"></script>
    <script type="text/javascript" src="http://www.amcharts.com/lib/3/pie.js"></script>
    <script type="text/javascript" src="http://www.amcharts.com/lib/3/themes/none.js"></script>
</head>
</#macro>

<#-- Header markup -->
<#macro Header>

<div class="visuallyhidden" id="fb-query-string">${QueryString}</div>

<div class="top-strip">
  <div class="top-strip-logo"></div>
  <h1>Content Auditor</span></h1>
</div>

<div id="top-content">
  <@s.InitialFormOnly>
  <div class="top-logo">
    <a href="/s/content-auditor.html?collection=${question.inputParameterMap["collection"]!?html}">
      <img src="/s/content-auditor/sm-logo.png" alt="Content Auditor"/>
    </a>
  </div><!-- .top-logo -->
  </@s.InitialFormOnly>

  <@s.AfterSearchOnly>
  <div class="top-logo">
    <a href="/s/content-auditor.html?collection=${question.inputParameterMap["collection"]!?html}">
      <img src="/s/content-auditor/sm-logo-no-text.png" alt="Content Auditor"/>
    </a>
  </div><!-- .top-logo -->
  </@s.AfterSearchOnly>

  <div class="search-form">
      <h2 class="visuallyhidden">Search</h2>
      <!-- QUERY FORM -->
      <form action="/s/content-auditor.html" method="GET" class="form-block">    
          <div class="form-field input-text field-query">
              <label for="query">Keywords</label>        
              <input name="query" id="query" type="search" placeholder="Search terms&hellip;" value="${question.inputParameterMap["query"]!?html}" class="input-xlarge">
          </div>
          <div class="form-field input-text field-url">
              <label for="scoping_url">Scoping URL</label>
              <input name="scoping_url" type="text" <@s.IfDefCGI name="scoping_url">value="${question.inputParameterMap["scoping_url"]!?html}"</@s.IfDefCGI> placeholder="http://example.com/directory/" />
          </div>
          
          <!-- ADD CUSTOM SORT MODES FOR ADDITIONAL METADATA FIELDS -->
          <div class="form-field select field-sort">     
              <label for="sort">Sort results by</label>
              <@s.Select name="sort" id="sort" options=["=Relevance", "date=Date (Newest First)", "adate=Date (Oldest First)", "url=URL", "title=Title (A-Z)", "dtitle=Title (Z-A)"] />
          </div>

          <#assign scopeCheckbox><@s.FacetScope></@s.FacetScope></#assign>
          <#if (scopeCheckbox?length > 0)>
          <div class="form-field input-checkbox field-facet-scope">
              <@s.FacetScope>Restrict to selected attributes</@s.FacetScope>
          </div>
          </#if>

          <input type="hidden" name="collection" value="${question.inputParameterMap["collection"]!?html}">
          <@s.IfDefCGI name="enc"><input type="hidden" name="enc" value="${question.inputParameterMap["enc"]!?html}"></@s.IfDefCGI>
          <@s.IfDefCGI name="form"><input type="hidden" name="form" value="${question.inputParameterMap["form"]!?html}"></@s.IfDefCGI>            
          <@s.IfDefCGI name="profile"><input type="hidden" name="profile" value="${question.inputParameterMap["profile"]!?html}"></@s.IfDefCGI>

          <div class="form-field input-submit field-submit">
              <input type="submit" value="search">
          </div>    
      </form>
  </div><!-- .search-form -->
</div><!-- #top-content -->

</#macro>

<#-- FooterScripts markup -->
<#macro FooterScripts>

<!-- SCRIPTS -->
<script src="${SearchPrefix}js/jquery/jquery-1.10.2.min.js"></script>
<script src="${SearchPrefix}js/jquery/jquery-ui-1.10.3.custom.min.js"></script>

<!-- fancybox JS -->
<script src="/s/content-auditor/jquery.fancybox.pack.js"></script>

<!-- query completion JS -->
<script src="${SearchPrefix}thirdparty/bootstrap-3.0.0/js/bootstrap.min.js"></script>
<script src="${SearchPrefix}js/jquery/jquery.tmpl.min.js"></script>
<script src="${SearchPrefix}js/jquery.funnelback-completion.js"></script>

<!-- global JS -->
<script src="/s/content-auditor/global.js" type="text/javascript"></script>

</#macro>