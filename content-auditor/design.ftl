<#ftl encoding="utf-8" />
<#import "/web/templates/modernui/funnelback_classic.ftl" as s/>
<#import "/web/templates/modernui/funnelback.ftl" as fb/>
<#-- Contents of the HTML <head> tag -->
  <#assign pathToAssets = '/s/content-auditor/assets/' />
  <#macro Head>
  <head>
    <title>
    <@s.AfterSearchOnly>${question.inputParameterMap["query"]!?html}${question.inputParameterMap["query_prox"]!?html}<@s.IfDefCGI name="query">,</@s.IfDefCGI></@s.AfterSearchOnly>
    <#--  <@s.cfg>service_name</@s.cfg>-->  - Content Auditor | Funnelback
    </title>
    <link rel="icon" href="/s/content-auditor/assets/img/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="/s/content-auditor/assets/img/favicon.ico" type="image/x-icon"/>
    
    <#-- CSS -->
    <link href="${pathToAssets}css/bootstrap.min.css" rel="stylesheet" media="screen">
    <link href="${pathToAssets}css/font-awesome.min.css" rel="stylesheet" media="screen">
    <link href="${pathToAssets}css/fb-content-auditor.css" rel="stylesheet" media="screen">
    <link href="${pathToAssets}css/jquery.tablescroll.css" rel="stylesheet" media="screen">

    <!-- JS -->
    <script>var content_auditor = [];</script>
    <script type="text/javascript" src="${pathToAssets}/js/amcharts/amcharts.js"></script>
    <script type="text/javascript" src="${pathToAssets}/js/amcharts/pie.js"></script>
    
    
  
  </head>
  </#macro>
  <#-- Header markup -->
  <#macro Header>
  <div class="hidden" id="fb-query-string">${QueryString}</div>
  <header role="banner" id="page-header" class="navbar navbar-fixed-top navbar-inverse">
    <div class="container-fluid">
      <div id="navbar-header">
        <!--<button data-target=".navbar-side" data-toggle="collapse" type="button" class="navbar-toggle collapsed">
        <span class="sr-only">Toggle Sidebar Navigation</span>
        <span class="fa fa-bars fa-lg"></span>
        </button>-->

        <a id="brand" href="#INSERT CONTENT AUDITOR HOME link here" title="Funnelback"><span class="navbar-brand"><em>- &nbsp; 14.2.0</em></span> </a>
        <h1>Content <span>Auditor</span></h1>

        <a id="toggle-sidebar" class="btn btn-xs">
        <span class="sr-only">Toggle Sidebar Navigation</span>
        <span class="fa fa-bars fa-lg"></span>
        </a>

        <a id="toggle-search" class="btn btn-xs">
        <span class="sr-only">Toggle Sidebar Navigation</span>
        <span class="fa fa-search fa-lg"></span>
        </a>

      </div>
      <ul class="nav navbar-nav navbar-right">
        <div class="search-form">
          <#-- QUERY FORM -->
          <form action="/s/content-auditor.html" method="GET" class="form form-inline">
            
            <div class="search-input-group">
              <div class="search-input-wrap">
                <input class="form-control fb-placeholder" name="query" id="query" type="search" placeholder="Keyword(s)" value="${question.inputParameterMap["query"]!?html}" >
                <label for="query"><span>query</span></label>
              </div>
              <div class="search-input-wrap">
                <input class="form-control fb-placeholder" name="scoping_url" type="text" <@s.IfDefCGI name="scoping_url">value="${question.inputParameterMap["scoping_url"]!?html}"</@s.IfDefCGI> placeholder="http://example.com/directory/" />
                <label for="scoping_url"><span>URL</span></label>
              </div>
            </div>
            <#-- ADD CUSTOM SORT MODES FOR ADDITIONAL METADATA FIELDS -->
            <div class="form-group">
              <#assign scopeCheckbox><@s.FacetScope></@s.FacetScope></#assign>
              <#if (scopeCheckbox?length > 0)>
              <div class="">
                <@s.FacetScope>Restrict to selected attributes</@s.FacetScope>
              </div>
              </#if>
            </div>
            
            <div class="form-group">
              <input type="hidden" name="collection" value="${question.inputParameterMap["collection"]!?html}">
              <@s.IfDefCGI name="enc"><input type="hidden" name="enc" value="${question.inputParameterMap["enc"]!?html}"></@s.IfDefCGI>
              <@s.IfDefCGI name="form"><input type="hidden" name="form" value="${question.inputParameterMap["form"]!?html}"></@s.IfDefCGI>
              <@s.IfDefCGI name="profile"><input type="hidden" name="profile" value="${question.inputParameterMap["profile"]!?html}"></@s.IfDefCGI>
            </div>
            
            <input class="btn btn-primary pull-left" type="submit" value="search">
            
          </form>
          </div><#-- .search-form -->
        </ul>
      </div>
    </header>
    <nav role="navigation" class="collapse navbar-collapse navbar-side">
      <ul class="nav navbar-nav">
        <li class="nav-title">search reports</li>
        <@s.AfterSearchOnly>
           <@main.ResultTabsNavigaton />
        </@s.AfterSearchOnly>
      </ul>
    </nav>
    <section id="page-mast">
      <div class="container-fluid">
        <@s.AfterSearchOnly>
        </@s.AfterSearchOnly>
        </div><!-- .container-fluid -->
      </section>
      </#macro>
      <#-- Footer -->
      <#macro Footer>
      <footer id="footer">
        <div class="row">
          <div class="col-sm-4">
            
          </div>
          <div class="col-sm-4 text-center"><a target="_blank" href="http://funnelback.com" title="Funnelback"><div class="footer-brand inline-block"></div></a></div>
          <div class="col-sm-4 text-right"><p id="copyright">&copy; 2006 - 2014 <a target="_blank" href="http://funnelback.com" title="Funnelback">Funnelback</a> - All rights reserved.</p></div>
        </div>
        <div id="load" class="overlay">
          <span><i class="fa fa-cog fa-spin fa-2x"></i>  Loading...</span>
          <div class="loading"></div>
         </div>
      </footer>
      </#macro>
      <#-- FooterScripts markup -->
      <#macro FooterScripts>
      <#-- SCRIPTS -->
      <script src="${pathToAssets}/js/jquery.min.js"></script>
      <#-- query completion JS -->
      <script src="${pathToAssets}/js/bootstrap.min.js"></script>
      <#-- Not sure what this applies to: <script src="${pathToAssets}/js/jquery.tmpl.min.js"></script>-->
      <#-- Not sure what this applies to + cannot find file: <script src="${pathToAssets}/js/jquery.funnelback-completion.js"></script>-->
      <script src="${pathToAssets}/js/pjax-standalone.min.js" type="text/javascript"></script>
      <script src="${pathToAssets}/js/fb-content-auditor.js" type="text/javascript"></script>
      <script type="text/javascript" src="${pathToAssets}/js/jquery.tablescroll.js"></script>

      </#macro>


      <#macro modalOverlay>
      <#-- Modal / #modal-overlay -->
      <div class="modal fade" id="modal-overlay" tabindex="-1" role="dialog" aria-labelledby="modal-overlayLabel" aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
              <h4 class="modal-title" id="modal-overlayLabel"></h4>
            </div>
            <div class="modal-body">
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              <button type="button" class="btn btn-primary">Save changes</button>
            </div>
          </div>
        </div>
      </div>
      </#macro>
