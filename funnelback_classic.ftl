<#---
    NickScript equivalent tags in FreeMarker.

    <p>This library contains a re-implementation of most of the Classic UI tags.</p>
    <p>This library <strong>must</strong> be imported under the <code>s</code> namespace.</p>

    <p>This documentation will give an overview of each tag. For a complete documentation
    please refer to the main <a href="../">Funnelback documentation</a>.</p>

    <p>Some tags takes their parameters from the nested content to mimic the Classic UI
    tags, for example: <code>&lt;@IfDefCGI&gt;query&lt;/@IfDefCGI&gt;</code>.
    In that case the input parameter is documented under the <strong>Nested</strong> heading.</p>
-->

<#---
    Generates previous / next navigation links, and page shortcuts.

    @param label Label to be placed at the beginning of the page list.
    @param separator: Text to placed between the page links.
    @param next_prev_prefix : Text placed before the Next and Previous links.
    @param next_prev_suffix : Text placed after the Next and Previous links.
    @nested Any HTML attributes to include in the a tag.
-->
<#macro PrevNext label="" separator=" " next_prev_prefix=" [ " next_prev_suffix=" ] ">
    <#if response?exists && response.resultPacket?exists && response.resultPacket.resultsSummary?exists>
        <#local rs = response.resultPacket.resultsSummary />

        ${label!""}

        <#-- PREVIOUS link -->
        <#if rs.prevStart?exists>
            <#local url = question.collection.configuration.value("ui.modern.search_link") + "?" />
            <#local url = url + changeParam(QueryString, "start_rank", rs.prevStart) />

            ${next_prev_prefix}<a href="${url?html}" rel="prev" class="fb-previous-result-page fb-page-nav" <#nested>>Prev ${rs.numRanks}</a>${next_prev_suffix}
        </#if>

        <#local pages = 0 />
        <#if rs.fullyMatching &gt; 0>
            <#local pages = (rs.fullyMatching + rs.partiallyMatching + rs.numRanks - 1) / rs.numRanks />
        <#else>
            <#local pages = (rs.totalMatching + rs.numRanks - 1) / rs.numRanks />
        </#if>

        <#local currentPage = 1 />
        <#if rs.currStart &gt; 0 && rs.numRanks &gt; 0>
            <#local currentPage = (rs.currStart + rs.numRanks -1) / rs.numRanks />
        </#if>

        <#local firstPage = 1 />
        <#if currentPage &gt; 4>
            <#local firstPage = currentPage - 4 />
        </#if>

        <#list firstPage..firstPage+9 as pg>
            <#if pg &gt; pages><#break /></#if>

            <#if pg == currentPage>
                <span class="fb-current-result-page">${pg}</span>
            <#else>
                <#local url = question.collection.configuration.value("ui.modern.search_link") + "?" />
                <#local url = url + changeParam(QueryString, "start_rank", (pg-1) * rs.numRanks+1) />

                <a href="${url?html}" <#nested>>${pg}</a>
            </#if>

            <#if pg_has_next> ${separator} </#if>
        </#list>

        <#-- NEXT link -->
        <#if rs.nextStart?exists>
            <#local url = question.collection.configuration.value("ui.modern.search_link") + "?" />
            <#local url = url + changeParam(QueryString, "start_rank", rs.nextStart) />

            ${next_prev_prefix}<a href="${url?html}" rel="next" class="fb-next-result-page fb-page-nav" <#nested>>Next ${rs.numRanks}</a>${next_prev_suffix}
        </#if>

    </#if>
</#macro>

<#---
    Conditional display, content is evaluated only when there are results.
-->
<#macro AfterSearchOnly>
    <#if response?exists
        && response.resultPacket?exists
        && response.resultPacket.resultsSummary?exists
        && response.resultPacket.resultsSummary.totalMatching?exists>
        <#nested>
    </#if>
</#macro>

<#---
    Conditional display, content is evaluated only when there is no search.
-->
<#macro InitialFormOnly><#compress>
    <#if response?exists
        && response.resultPacket?exists
        && response.resultPacket.resultsSummary?exists
        && response.resultPacket.resultsSummary.totalMatching?exists>
    <#else>
        <#nested>
    </#if>
</#compress></#macro>

<#---
    Generates an Open Search link.
-->
<#macro OpenSearch>
    <#local title><#nested></#local>
    <#if ! title?exists || title == "">
        <#local title = "Search " + question.collection.configuration.value("service_name") />
    </#if> 
    <link rel="search" type="application/opensearchdescription+xml" href="open-search.xml?${QueryString?html}" title="${title}">
</#macro>

<#---
    Read a configuration parameter.

    <p>Reads a <code>collection.cfg</code> parameter for the
    current collection being searched and displays it.

    @nested Name of the parameter.
-->
<#macro cfg><#compress>
    <#local key><#nested></#local>
    <#if key?exists && key != ""
        && question.collection.configuration.value(key)?exists>
        ${question.collection.configuration.value(key)}
    </#if>
</#compress></#macro>

<#---
    Conditional display against CGI parameters (Query string).

    <p>The nested content will be evaluated only if the desired
    parameter exists.</p>

    @param name Name of the parameter to test.
-->
<#macro IfDefCGI name><#compress>
    <#if question?exists
        && question.inputParameterMap?exists
        && question.inputParameterMap?keys?seq_contains(name)>
        <#nested>
    </#if>
</#compress></#macro>

<#---
    Conditional display against CGI parameters (Query string).

    <p>The nested content will be evaluated only if the desired
    parameter is <strong>not</strong> set.

    @param name Name of the parameter to test.
-->
<#macro IfNotDefCGI name><#compress>
    <#if question?exists
        && question.inputParameterMap?exists
        && question.inputParameterMap?keys?seq_contains(name)>
    <#else>
        <#nested>
    </#if>
</#compress></#macro>

<#---
    Retrieves a cgi parameter value.

    @nested Name of the parameter.
-->
<#macro cgi><#compress>
    <#local key><#nested></#local>
    <#if question?exists
        && question.inputParameterMap?exists
        && question.inputParameterMap[key]?exists>
        <#-- Return first element only, to mimic Perl UI behavior --> 
        ${question.inputParameterMap[key]?html!}
    </#if>
</#compress></#macro>

<#---
    Displays spelling suggestions.
-->
<#macro CheckSpelling>
    <#if question?exists
        && question.collection?exists
        && question.collection.configuration.value("spelling_enabled")?exists
        && is_enabled(question.collection.configuration.value("spelling_enabled"))
        && response?exists
        && response.resultPacket?exists
        && response.resultPacket.spell?exists>
        Did you mean: <a href="${question.collection.configuration.value("ui.modern.search_link")}?${changeParam(QueryString, "query", response.resultPacket.spell.text?url)?html}">
            <span class="funnelback-highlight">${response.resultPacket.spell.text}</span>
        </a>
    </#if>
</#macro>

<#---
    Conditional display against best bets and best bets looping.

    <p>The content will be evaluated only if there is a best bet match
    for the query, and once per best bet found.</p>

    @provides The best bet as <code>${s.bb}</code>.
-->
<#macro BestBets>
    <#if response?exists
        && response.resultPacket?exists
        && response.resultPacket.bestBets?exists
        && response.resultPacket.bestBets?size &gt; 0>
        <#list response.resultPacket.bestBets as bestBet>
            <#assign bb = bestBet in s />
            <#nested>
        </#list>
    </#if>
</#macro>

<#---
    Conditional display against results and results looping.

    <p>The content will be evaluated only if there are results,
    and once per result found.</p>

    <p><strong>Note:</strong> This will loop over a list containing
    a list of TierBar and Result objects, so you need to check the
    object type in the loop before trying to access its fields like
    title, etc.</p>

    @provides the search results as <code>${s.result}</code>.
-->
<#macro Results>
    <#if response?exists
        && response.resultPacket?exists
        && response.resultPacket.resultsWithTierBars?exists>
        <#list response.resultPacket.resultsWithTierBars as r>
            <#assign result = r in s />
            <#nested>
        </#list>
    </#if>
</#macro>

<#---
    Cut the left part of a string if it matches the given pattern.

    @param cut Pattern to look for.
-->
<#macro cut cut><#compress>
    <#if cut?exists>
        <#local value><#nested></#local>
        ${value?replace(cut, "", "r")}
    </#if>
</#compress></#macro>

<#---
    Truncate a string on word boundaries.

    @param length Length to keep.
-->
<#macro Truncate length><#compress>
    <#local value><#nested></#local>
    ${truncate(value, length)}
</#compress></#macro>

<#---
    Truncate a string on word boundaries.

    <p>If the string contains HTML it'll try to preserve its validity.</p>

    @param length Length to keep.
-->
<#macro TruncateHTML length><#compress>
    <#local value><#nested></#local>
    ${truncateHTML(value, length)}
</#compress></#macro>

<#---
    Truncate an URL in a sensible way.

    <p>This tag will attempt to break the URL up over maximum of
    two lines, only breaking on slashes.</p>

    @param length Length to keep.
-->
<#macro TruncateURL length><#compress>
    <#local value><#nested></#local>
    ${truncateURL(value, length)}
</#compress></#macro>

<#---
    Generates an "explore" link for a search result.
-->
<#macro Explore>
    <a class="fb-explore" href="?${changeParam(QueryString, "query", "explore:" + s.result.liveUrl)?html}">Explore</a>
</#macro>

<#---
    Conditional display against quick links.

    <p>The content will be evaluated only if the current
    result has quick links.</p>
-->
<#macro Quicklinks>
    <#if s.result.quickLinks?exists>
        <#nested>
    </#if>
</#macro>

<#---
    Iterates over quick links.

    <p>The content will be evaluated once per quick link.</p>

    @provides The quick link as <code>${s.ql}</code>.
-->
<#macro QuickRepeat>
    <#if s.result.quickLinks?exists && s.result.quickLinks.quickLinks?exists>
        <#list s.result.quickLinks.quickLinks as quickLink>
            <#assign ql = quickLink in s />
            <#nested>
        </#list>
    </#if>
</#macro>

<#---
    Wraps words into strong tags.

    @param bold The words to boldicize, space separated. If not set it will automatically boldicize the query terms.
-->
<#macro boldicize bold=""><#compress>
    <#local content><#nested></#local>
    <#if bold != "">
        ${tagify("strong", bold, content)}
    <#else>
        <#-- Pass the regular expression returned by PADRE -->
        ${tagify("strong", response.resultPacket.queryHighlightRegex!, content, true)} 
    </#if>
</#compress></#macro>

<#---
    Wraps words into emphasis tags.

    @param italics The words to italicize, space separated. If not set it will automatically italicize the query terms.
-->
<#macro italicize italics><#compress>
    <#local content><#nested></#local>
    <#if italics?exists>
        ${tagify("em", italics, content)}
    <#else>
        <#-- Pass the regular expression returned by PADRE -->
        ${tagify("em", response.resultPacket.queryHighlightRegex!, content, true)} 
    </#if>
</#compress></#macro>

<#---
    Displays the <em>cleaned</em> query.

    <p>The <em>cleaned</em> query contains only query expressions
    entered by the user, without the one dynamically generated for
    other purposes like faceted navigation.</p>
-->
<#macro QueryClean><#compress>
    <#if response?exists
        && response.resultPacket?exists>
        ${response.resultPacket.queryCleaned?html}
    </#if>
</#compress></#macro>

<#---
    Encodes a String in URL format.

    @nested Content to encode.
-->
<#macro URLEncode><#compress>
    <#assign content><#nested></#assign>
    ${content?url}
</#compress></#macro>

<#---
    Decodes a String from HTML.

    @nested Content to decode.
-->
<#macro HtmlDecode><#compress>
    <#assign content><#nested></#assign>
    ${htmlDecode(content)}
</#compress></#macro>

<#--- @begin Faceted navigation -->

<#---
    Conditional display against faceted navigation.

    <p>The content will be evaluated only if faceted navigation
    is configured.</p>
-->
<#macro FacetedSearch>
    <#if question?exists
        && facetedNavigationConfig(question.collection, question.profile)?exists >
        <#nested>
    </#if>
</#macro>

<#---
    Displays a facet, a list of facets, or all facets.

    <p>If both <code>name</code> and <code>names</code> are not set
    this tag iterates over all the facets.</p>

    @param name Name of a specific facet to display, optional.
    @param names A list of specific facets to display, optional.
    @param class CSS class to use on the DIV containing each facet, defaults to <code>facet</code>.

    @provides The facet as <code>${s.facet}</code>.
-->
<#macro Facet name="" names=[] class="facet">
    <#if response?exists && response.facets?exists>
        <#if name == "" && names?size == 0>
            <#-- Iterate over all facets -->
            <#list response.facets as f>
                <#if f.hasValues() || question.selectedFacets?seq_contains(f.name)>
                    <#assign facet = f in s>
                    <div class="${class}">
                        <#nested>
                    </div>
                </#if>
            </#list>
        <#else>
            <#list response.facets as f>
                <#if (f.name == name || names?seq_contains(f.name) ) && (f.hasValues() || question.selectedFacets?seq_contains(f.name))>
                    <#assign facet = f in s>
                    <div class="${class}">
                        <#nested>
                    </div>
                </#if>
            </#list>
        </#if>
    </#if>
</#macro>

<#---
    Displays a facet label and a breadcrumb.

    @param class Optional class to affect to the div containing the facet and breadcrumb.
    @param separator Separator to use in the breadcrumb.
    @param summary Set to true if you want this tag to display the summary + breadcrumb, otherwise use <code>&lt;@s.FacetSummary /&gt;</code>.
-->
<#macro FacetLabel class="facetLabel" separator="&rarr;" summary=true>
    <#local fn = facetedNavigationConfig(question.collection, question.profile) >
    <#if fn?exists>
        <#-- Find facet definition in the configuration corresponding
             to the facet we're currently displaying -->
        <#list fn.facetDefinitions as fdef>
            <#if fdef.name == s.facet.name>
                <#assign facetDef = fdef in s />
                <div class="${class}"> ${s.facet.name}
                    <#if summary><@FacetSummary separator=separator alltext="all" /></#if>
                </div>
            </#if>
        </#list> 
    </#if>
</#macro>

<#---
    Displays The facet summary and breadcrumb.

    <p>This tag is called by <code>&lt;@s.FacetLabel /&gt;</code> but this can be disabled
    so that the summary and breadcrumb can be displayed separately using this tag for more flexibility.</p>

    @param separator Separator to use in the breadcrumb.
    @param alltext Text to use to completely remove the facet constraints. Defaults to &quot;all&quot;.
-->
<#macro FacetSummary separator="&rarr;" alltext="all">
    <#-- We must test various combinations here as different browsers will encode
         some characters differently (i.e. '/' will sometimes be preserved, sometimes
         encoded as '%2F' -->
    <#if QueryString?contains("f." + facetDef.name?url)
        || urlDecode(QueryString)?contains("f." + facetDef.name)
        || urlDecode(QueryString)?contains("f." + facetDef.name?url)>
        : <a href="${question.collection.configuration.value("ui.modern.search_link")}?${removeParam(facetScopeRemove(QueryString, facetDef.allQueryStringParamNames), ["start_rank"] + facetDef.allQueryStringParamNames)?html}">${alltext}</a>
    </#if>
    <@FacetBreadCrumb categoryDefinitions=facetDef.categoryDefinitions selectedCategoryValues=question.selectedCategoryValues separator=separator />
</#macro>

<#---
    Displays facet title or value of the current category.

    <p>Displays either the facet title if no categories has been
    selected, or the value of the currently selected category.</p>

    <p>For hierarchical facets, displays the latest selected category.</p>

    @param title Whether to display the facet title only, or the category.
    @param class CSS class to apply to the container DIV, <code>shortFacetLabel</code> by default.
-->
<#macro ShortFacetLabel title=false class="shortFacetLabel">
    <#if (title?is_boolean && title) || (title?is_string && title == "true")>
        <div class="${class}">${s.facet.name!""}</div>
    <#else>
        <#local deepest = s.facet.findDeepestCategory(question.selectedCategoryValues?keys)!"">
        <#if deepest != "">
            <div class="${class}">${question.selectedCategoryValues[deepest.queryStringParamName]?first}</div>
        <#else>
            <div class="${class}">${s.facet.name!""}</div>
        </#if>
    </#if>
</#macro>

<#---
    Recursively generates the breadcrumbs for a facet.

    @param categoryDefinitions List of sub categories (hierarchical).
    @param selectedCategoryValues List of selected values.
    @param separator Separator to use in the breadcrumb.
-->
<#macro FacetBreadCrumb categoryDefinitions selectedCategoryValues separator>
    <#list categoryDefinitions as def>

        <#if def.class.simpleName == "URLFill" && selectedCategoryValues?keys?seq_contains(def.queryStringParamName)>
            <#-- Special case for URLFill facets: Split on slashes -->
            <#assign path = selectedCategoryValues[def.queryStringParamName][0]>
            <#assign pathBuilding = "">
            <#list path?split("/", "r") as part>
                <#assign pathBuilding = pathBuilding + "/" + part>
                <#-- Don't display bread crumb for parts that are part
                     of the root URL -->
                <#if ! def.data?contains(part+"/")>
                    <#if part_has_next>
                        ${separator} <a href="${question.collection.configuration.value("ui.modern.search_link")}?${removeParam(facetScopeRemove(QueryString, def.allQueryStringParamNames), ["start_rank"] + def.allQueryStringParamNames)?html}&amp;${def.queryStringParamName}=${pathBuilding?url}">${part}</a>
                    <#else>
                        ${separator} ${part}
                    </#if>
                </#if>
            </#list>
        <#else>
            <#if selectedCategoryValues?keys?seq_contains(def.queryStringParamName)>
                <#-- Find the label for this category. For nearly all categories the label is equal
                     to the value returned by the query processor, but not for date counts for example.
                     With date counts the label is the actual year "2003" or a "past 3 weeks" but the
                     value is the constraint to apply like "d=2003" or "d>12Jun2012" -->
                <#-- Use value by default if we can't find a label -->
                <#local valueLabel = selectedCategoryValues[def.queryStringParamName][0] />

                <#-- Iterate over generated facets -->
                <#list response.facets as facet>
                    <#if def.facetName == facet.name>
                        <#-- Facet located, find current working category -->
                        <#assign fCat = facet.findDeepestCategory([def.queryStringParamName])!"" />
                        <#if fCat != "">
                            <#list fCat.values as catValue>
                                <#-- Find the category value for which the query string param
                                     matches the currently selected value -->
                                <#local kv = catValue.queryStringParam?split("=") />
                                <#if valueLabel == urlDecode(kv[1])>
                                    <#local valueLabel = catValue.label />
                                </#if>
                            </#list>
                        </#if>
                    </#if>
                </#list> 

                <#-- Find if we are processing the last selected value (leaf node) -->
                <#local last = true>
                <#list def.allQueryStringParamNames as param>
                    <#if param != def.queryStringParamName && selectedCategoryValues?keys?seq_contains(param)>
                        <#local last = false>
                        <#break>
                    </#if>
                </#list>

                <#if last == true>
                    ${separator} ${valueLabel}
                <#else>
                    ${separator} <a href="${question.collection.configuration.value("ui.modern.search_link")}?${removeParam(facetScopeRemove(QueryString, def.allQueryStringParamNames), ["start_rank"] + def.allQueryStringParamNames)?html}&amp;${def.queryStringParamName}=${selectedCategoryValues[def.queryStringParamName][0]?url}">
                        ${valueLabel}
                    </a>
                    <@FacetBreadCrumb categoryDefinitions=def.subCategories selectedCategoryValues=selectedCategoryValues separator=separator/>
                </#if>
                <#-- We've displayed one step in the breadcrumb, no need to inspect
                     other category definitions -->
                <#break />
            </#if>
        </#if>
    </#list>
</#macro>

<#---
    Displays a link to show more or less categories for a facet.
-->
<#macro MoreOrLessCategories>
    <span class="moreOrLessCategories"><a href="#" onclick="javascript:toggleCategories(this)" style="display: none;">more...</a></span>
</#macro>

<#---
    Displays a link for a facet category value.

    @param class Optional CSS class to use, defaults to <code>categoryName</code>.
-->
<#macro CategoryName class="categoryName">
    <#if s.categoryValue?exists>
        <#assign paramName = s.categoryValue.queryStringParam?split("=")[0]>
        <span class="${class}">
            <a href="${question.collection.configuration.value("ui.modern.search_link")}?${removeParam(facetScopeRemove(QueryString, paramName), ["start_rank", paramName])?html}&amp;${s.categoryValue.queryStringParam?html}">${s.categoryValue.label}</a>
        </span>
    </#if>
</#macro>

<#---
    Displays the result count for a facet category value.

    @param class Optional CSS class.
-->
<#macro CategoryCount class="categoryCount"><#compress>
    <#if s.categoryValue?exists><span class="${class}">${s.categoryValue.count}</span></#if>
</#compress></#macro>

<#---
    Display the <em>facet scope</em> checkbox.

    <p>Provides a checkbox to constraint search to the
    currently selected facet(s) only.</p>

    @nested Text to display beside the checkbox.
-->
<#macro FacetScope>
    <@AfterSearchOnly>
    <#if question?exists && question.selectedCategoryValues?size &gt; 0>
        <#local facetScope = "" />
        <#list question.selectedCategoryValues?keys as key>
            <#list question.selectedCategoryValues[key] as value>
                <#local facetScope = facetScope + key?url+"="+value?url />
                <#if value_has_next><#local facetScope = facetScope + "&" /></#if>
            </#list>
            <#if key_has_next><#local facetScope = facetScope + "&" /></#if>
        </#list> 
                <input type="checkbox" name="facetScope" id="facetScope" value="${facetScope}" checked="yes">

        <label for="facetScope"><#nested></label>
    </#if>
    </@AfterSearchOnly>
</#macro>

<#--- @end -->

<#--- @begin Contextual navigation -->

<#---
    Root tag for contextual navigation.

    <p>The content is always evaluated, regardless of the
    presence of contextual navigation suggestions.</p>
-->
<#macro ContextualNavigation>
    <#nested>
</#macro>

<#---
    Conditional display against the presence of clusters.

    <p>The content will be evaluated only if contextual navigation
    clusters were found.</p>
-->
<#macro NoClustersFound>
    <#if response?exists
        && response.resultPacket?exists
        && ! response.resultPacket.contextualNavigation?exists>
        <#nested>
    </#if>
</#macro>

<#---
    Displays previously followed clusters.
-->
<#macro ClusterNavLayout>
    <#if question?exists && question.cnPreviousClusters?size &gt; 0>
        <#nested>
    </#if>
</#macro>

<#---
    Iterates overs previously followed clusters.

    @provides The previous cluster as <code>${s.previousCluster}</code>.
-->
<#macro ContextualNavigationNav>
    <#if question?exists && question.cnPreviousClusters?size &gt; 0>
        <#list question.cnPreviousClusters as cluster>
            <#assign previousCluster = cluster in s>
            <#nested>
        </#list>
    </#if>
</#macro>

<#---
    Conditional display against contextual navigation clusters.

    <p>The content will be evaluated only if there are contextual
    navigation clusters, except if there is only one of the <em>site</em>
    type.</p>

    @provides The contextual navigation object as <code>${s.contextualNavigation}</code>.
-->
<#macro ClusterLayout>
    <#if response?exists
        && response.resultPacket?exists
        && response.resultPacket.contextualNavigation?exists>
        <#assign contextualNavigation = response.resultPacket.contextualNavigation in s />
        <#if contextualNavigation.categories?size == 1 && contextualNavigation.categories[0].name == "site"
            && contextualNavigation.categories[0].clusters?size &lt; 2>
            <#-- Do nothing if we only have a site category with only 1 site -->
        <#else>
            <#nested>
        </#if>
    </#if>
</#macro>

<#---
    Displays a contextual navigation category <strong>or</strong> a 
    faceted navigation category.

    <p>The presence of the <code>name</code> parameter determines the role.</p>
    <p>The <code>nbCategories</code> and <code>recursionCategories</code> parameters
    are internals and can be safely ignored when using this tag.</p>

    <p>For faceted navigation the <tt>max</tt> parameter sets the maximum number of
    categories to return. If you need to display only a few number of them with a <em>more...</em>
    link for expansion, you'll need to use Javascript. See the default form file for an example.</p>

    @param name Name of the category for contextual navigation. Can be <code>type</code>, <code>type</code> or <code>topic</code>. Empty for a faceted navigation category.
    @param max Maximum number of categories to display, for faceted navigation.
    @param nbCategories (Internal parameter, do not use) Current number of categories displayed (used in recursion for faceted navigation).
    @param recursionCategories (Internal parameter, do not use) List of categories to process when recursing for faceted navigation).

    @provides The category as <code>${s.category}</code>.
-->
<#macro Category max=16 nbCategories=0 recursionCategories=[] name...>
    <#--
        We use a trick to set the 'name' parameter optional: The argument
        is set as an optional multivalued argument. If we don't use that FM
        will complain that the 'name' argument must be set.
        Using that trick makes the 'name' parameter a Hash, so to access our
        initial 'name' argument we must use ${name['name']}
    -->
    <#if name?exists && name?size == 1>
        <#list response.resultPacket.contextualNavigation.categories as c>
            <#if c.name?exists && c.name == name["name"]>
                <#assign category = c in s />
                <#if c.name != "site" || c.clusters?size &gt; 1>
                    <#nested>
                </#if>
            </#if>
        </#list>
    <#else>

        <#-- Find if we are working at the root level (facet) or in a sub category -->
        <#if recursionCategories?exists && recursionCategories?size &gt; 0>
            <#local categories = recursionCategories />
        <#else>
            <#local categories = s.facet.categories />
        </#if>
        <#if categories?exists && categories?size &gt; 0>

            <#local class = "category">
            <#if name?size &gt; 0 && name["class"]?exists>
                <#local class = name["class"]>
            </#if>

            <#list categories as c>
                <#assign category = c in s>
                <#list c.values as cv>
                    <#-- Find if this category has been selected. If it's the case, don't display
                         it in the list, except if it's an URL fill facet as we must display sub-folders
                         of the currently selected folder -->
                    <#if ! question.selectedCategoryValues?keys?seq_contains(cv.queryStringParam?split("=")[0])
                        || c.queryStringParamName?contains("|url")>
                        <#assign categoryValue = cv in s>
                        <#local nbCategories = nbCategories+1 />
                        <#if nbCategories &gt; max><#return></#if>

                        <div class="${class}">
                            <#nested>
                        </div>
                    </#if>
                </#list>
                <#-- Recurse in sub categories -->
                <#if category.categories?exists && category.categories?size &gt; 0>
                    <@Category recursionCategories=category.categories max=max nbCategories=nbCategories><#nested></@Category>
                </#if>
            </#list>

        </#if>
    </#if>
</#macro>

<#---
    Iterates over contextual navigation clusters.

    @provides The cluster as <code>${s.cluster}</code>.
-->
<#macro Clusters>
    <#if s.category?exists>
        <#list s.category.clusters as c>
            <#assign cluster = c in s />
            <#nested>
        </#list>
    </#if>
</#macro>

<#---
    Link to show more clusters.

    @param category Name of the category for contextual navigation (<code>type</code> , <code>site</code>, <code>topic</code>).
-->
<#macro ShowMoreClusters category>
    <#if s.category?exists && s.category.name == category && s.category.moreLink?exists>
        <#nested>
    </#if>
</#macro>

<#---
    Link to show less clusters.

    @param category Name of the category for contextual navigation (<code>type</code> , <code>site</code>, <code>topic</code>).
-->
<#macro ShowFewerClusters category>
    <#if s.category?exists && s.category.name == category && s.category.fewerLink?exists>
        <#nested>
    </#if>
</#macro>

<#--- @end -->

<#---
    Generates a HTML <code>&lt;select /&gt;</code> tags with options.

    @param name Name of the select.
    @param options List of option, either single strings that will be used as the name and value, or <code>value=label</code> strings.
    @param range Optional range expression to generate options.
    @param additional : Any additional HTML attributes to set to the select tag.
-->
<#macro Select name options=[] range="" additional...>
    <select name="${name}" <#compress>
        <#if additional?exists && additional?is_hash>
            <#list additional?keys as key>${key}="${additional[key]}" </#list>
        </#if>
        </#compress>>
        <#if options?size &gt; 0>
            <#list options as opt>
                <#if opt?contains("=")>
                    <#assign valueAndLabel = opt?split("=")>
                    <option value="${parseRelativeDate(valueAndLabel[0])}" <#if question.inputParameterMap[name]?exists
                        && question.inputParameterMap[name] == valueAndLabel[0]>selected="selected"</#if>>${valueAndLabel[1]}</option>
                <#else>
                    <option value="${parseRelativeDate(opt)}" <#if question.inputParameterMap[name]?exists
                        && question.inputParameterMap[name] == opt>selected="selected"</#if>>${parseRelativeDate(opt)}</option>
                </#if>
            </#list>
        </#if>
        <#if range != "">
            <#assign parsedRange = parseRange(range) />
            <#list parsedRange.start..parsedRange.end as i>
                <option value="${i?c}" <#if question.inputParameterMap[name]?exists
                    && question.inputParameterMap[name] == i?c>selected="selected"</#if>>${i?c}</option>
            </#list>
        </#if>
    </select>
</#macro>

<#---
    Displays the current date and time.
-->
<#macro CurrentDate><p class="date">${currentDate()?datetime?string}</p></#macro>

<#---
    Displays the last updated date of the collection being searched.

    @param prefix Optional prefix to output before the date.
-->
<#macro Date prefix=""><#compress>
    <#if question?exists && question.collection?exists>
        <#local updDate = updatedDate(question.collection.id)!"">
        <#if updDate?is_date>
            ${prefix}${updatedDate(question.collection.id)?datetime?string}
        <#else>
            ${prefix}Meta colllection
        </#if>
    </#if>
</#compress></#macro>

<#---
    Generates a RSS link or button for the current query.

    @pested Type of link to generate, <code>link</code> or <code>button</code>.
-->
<#macro rss>
    <#assign type><#nested></#assign>
    <#if type?exists>
        <#if type == "link">
            <link rel="alternate" type="application/rss+xml" title="Search results" href="${SearchPrefix}rss.cgi?${QueryString!""}" />
        </#if>
        <#if type == "button">
            <p id="rss-button"><a href="${SearchPrefix}rss.cgi?${QueryString!""}" class="rss">
                <span class="rss-left"> XML </span><span class="rss-right">RSS 2.0</span>
            </a></p>
        </#if>
    </#if>
</#macro>

<#---
    Provide links to collection search forms.

    <p>This will iterate over every existing search forms
    for the current collection and display a link to access every
    one of them.</p>
-->
<#macro FormChoice>
    <#if question?exists && question.collection?exists>
        <#assign forms = formList(question.collection.id, question.profile) />
        <#assign url = question.collection.configuration.value("ui.modern.search_link")
            + "?collection=" + question.collection.id
            + "&amp;profile=" + question.profile />
        <#list forms as form>
            <#if form != question.form && !form?matches("^.*-\\d{12}$")>
                <a href="${url}&amp;form=${form}">${form}</a>
            </#if>
        </#list>
    </#if>
</#macro>
