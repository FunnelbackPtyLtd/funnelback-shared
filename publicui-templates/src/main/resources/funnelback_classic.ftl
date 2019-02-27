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
    @param separator Text to placed between the page links.
    @param next_prev_prefix Text placed before the Next and Previous links.
    @param next_prev_suffix Text placed after the Next and Previous links.
    @param prevLabel Label to use for "previous"
    @param nextLabel Label to use for "next"
    @param numPages Number of pages links to display (default = 10)
    @nested Any HTML attributes to include in the a tag.
-->
<#macro PrevNext label="" separator=" " next_prev_prefix=" [ " next_prev_suffix=" ] " prevLabel="Prev" nextLabel="Next" numPages=10>
    <#if response?exists && response.resultPacket?exists && response.resultPacket.resultsSummary?exists>
        <#local rs = response.resultPacket.resultsSummary />

        ${label!""}

        <#-- PREVIOUS link -->
        <#if rs.prevStart?exists>
            <#local url = question.collection.configuration.value("ui.modern.search_link") + "?" />
            <#local url = url + changeParam(QueryString, "start_rank", rs.prevStart) />

            ${next_prev_prefix}<a href="${url?html}" rel="prev" class="fb-previous-result-page fb-page-nav" <#nested>>${prevLabel} ${rs.numRanks}</a>${next_prev_suffix}
        </#if>

        <#local pages = 0 />
        <#if rs.fullyMatching??>
            <#if rs.fullyMatching &gt; 0>
                <#local pages = (rs.fullyMatching + rs.partiallyMatching + rs.numRanks - 1) / rs.numRanks />
            <#else>
                <#local pages = (rs.totalMatching + rs.numRanks - 1) / rs.numRanks />
            </#if>
        <#else>
            <#-- Event search -->
            <#local pages = (rs.totalMatching + rs.numRanks - 1) / rs.numRanks />
        </#if>

        <#local currentPage = 1 />
        <#if rs.currStart &gt; 0 && rs.numRanks &gt; 0>
            <#local currentPage = (rs.currStart + rs.numRanks -1) / rs.numRanks />
        </#if>

        <#local firstPage = 1 />
        <#if currentPage &gt; ((numPages-1)/2)?floor>
            <#local firstPage = currentPage - ((numPages-1)/2)?floor />
        </#if>

        <#list firstPage..firstPage+(numPages-1) as pg>
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

            ${next_prev_prefix}<a href="${url?html}" rel="next" class="fb-next-result-page fb-page-nav" <#nested>>${nextLabel} ${rs.numRanks}</a>${next_prev_suffix}
        </#if>

    </#if>
</#macro>

<#---
    Conditional display, content is evaluated only when there are results.
-->
<#macro AfterSearchOnly>
    <#if (response.resultPacket.resultsSummary.totalMatching)??>
        <#nested>
    </#if>
</#macro>

<#---
    Conditional display, content is evaluated only when there is no search.
-->
<#macro InitialFormOnly><#compress>
    <#if (response.resultPacket.resultsSummary.totalMatching)??>
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

    <p>Reads a <code>profile.cfg</code> with fallback to read a <code>collection.cfg</code> 
    parameter for the current collection being searched and displays it.

    @nested Name of the parameter.
-->
<#macro cfg><#compress>
    <#local key><#nested></#local>
    <#if key?? && key != "" && question.currentProfileConfig.get(key)??>
        ${question.currentProfileConfig.get(key)}
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

    @param prefix Prefix to display before the suggestion, usually "Did you mean ?".
-->
<#macro CheckSpelling prefix="Did you mean:" suffix="?">
    <#if question?exists
        && question.collection?exists
        && question.collection.configuration.value("spelling_enabled")?exists
        && is_enabled(question.collection.configuration.value("spelling_enabled"))
        && response?exists
        && response.resultPacket?exists
        && response.resultPacket.spell?exists>
        ${prefix} <a href="${question.collection.configuration.value("ui.modern.search_link")}?${changeParam(QueryString, "query", response.resultPacket.spell.text?url)?html}">
            <span class="funnelback-highlight">${response.resultPacket.spell.text}</span></a>${suffix}
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
            <#assign bb_index = bestBet_index in s />
            <#assign bb_has_next = bestBet_has_next in s />
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
            <#assign result_has_next = r_has_next in s />
            <#assign result_index = r_index in s />
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
        ${value?replace("^"+cut, "", "r")}
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

    @param label Label to use for the link.
-->
<#macro Explore label="Explore">
    <a class="search-explore" href="?collection=${question.collection.id}&profile=${question.profile}&query=explore:${s.result.liveUrl?url?html}">${label}</a>
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
            <#assign ql_index = quickLink_index in s />
            <#assign ql_has_next = quickLink_has_next in s />
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
<#macro italicize italics=""><#compress>
    <#local content><#nested></#local>
    <#if italics != "">
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
        ${response.resultPacket.queryCleaned!?html}
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
	
	@param negate Whether to negate the tag, i.e. evaluate the content if faceted navigation is not configured.
-->
<#macro FacetedSearch negate=false>
	<#if !negate>
		<#if question?exists
			&& facetedNavigationConfig(question.collection, question.profile)?exists >
			<#nested>
		</#if>
	<#else>
		<#if !question?exists
			|| !facetedNavigationConfig(question.collection, question.profile)?exists >
			<#nested>
		</#if>
    </#if>
</#macro>

<#---
    Displays a facet, a list of facets, or all facets.

    <p>If both <code>name</code> and <code>names</code> are not set
    this tag iterates over all the facets.</p>

    @param name Name of a specific facet to display, optional.
    @param names A list of specific facets to display, optional. Won't affect facet display order (defined in <code>faceted_navigation.cfg</code>).
    @param orderedNames A list of specific facets to display, in the order they are to be displayed optional.
    @param class CSS class to use on the DIV containing each facet, defaults to <code>facet</code>.

    @provides The facet as <code>${s.facet}</code>.
    @deprecated The new facets data model has been simplified and can be used directly with native FreeMarker tags
-->
<#macro Facet name="" names=[] orderedNames=[] class="facet">
    <#if response?exists && response.facets?exists>
        <#if name == "" && names?size == 0 && orderedNames?size == 0>
            <#-- Iterate over all facets -->
            <#list response.facets as f>
                <#if f.hasValues() || question.selectedFacets?seq_contains(f.name)>
                    <#assign facet = f in s>
                    <#assign facet_index = f_index in s>
                    <#assign facet_has_next = f_has_next in s>
                    <div id="facet-${facet_index}" class="${class}">
                        <#nested>
                    </div>
                </#if>
            </#list>
        <#else>
            <#if orderedNames?size == 0>
                <#list response.facets as f>
                    <#if (f.name == name || names?seq_contains(f.name) ) && (f.hasValues() || question.selectedFacets?seq_contains(f.name))>
                        <#assign facet = f in s>
                        <#assign facet_index = f_index in s>
                        <#assign facet_has_next = f_has_next in s>
                        <div class="${class}">
                            <#nested>
                        </div>
                    </#if>
                </#list>
            <#else>
                <#list orderedNames as orderedName>
                    <#list response.facets as f>
                        <#if (f.name == orderedName) && (f.hasValues() || question.selectedFacets?seq_contains(f.name))>
                            <#assign facet = f in s>
                            <#assign facet_index = f_index in s>
                            <#assign facet_has_next = f_has_next in s>
                            <div class="${class}">
                                <#nested>
                            </div>
                        </#if>
                    </#list>
                </#list>
            </#if>
        </#if>
    </#if>
</#macro>

<#---
    Displays a facet label and a breadcrumb.

    @param class Optional class to affect to the div containing the facet and breadcrumb.
    @param separator Separator to use in the breadcrumb.
    @param summary Set to true if you want this tag to display the summary + breadcrumb, otherwise use <code>&lt;@s.FacetSummary /&gt;</code>.
    @param tag HTML tag to wrap the name and summary
    @deprecated The new facets data model has been simplified and can be used directly with native FreeMarker tags
-->
<#macro FacetLabel class="facetLabel" separator="&rarr;" summary=true tag="div" link=question.collection.configuration.value("ui.modern.search_link")>
    <#local fn = facetedNavigationConfig(question.collection, question.profile) >
    <#if fn?exists>
        <#-- Find facet definition in the configuration corresponding
             to the facet we're currently displaying -->
        <#list fn.facetDefinitions as fdef>
            <#if fdef.name == s.facet.name>
                <#assign facetDef = fdef in s />
                <#assign facetDef_index = fdef_index in s />
                <#assign facetDef_has_next = fdef_has_next in s />
                <${tag} class="${class}"> ${s.facet.name}
                    <#if summary><@FacetSummary separator=separator alltext="all" link=link /></#if>
                </${tag}>
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
    @deprecated The new facets data model has been simplified and can be used directly with native FreeMarker tags
-->
<#macro FacetSummary separator="&rarr;" alltext="all" link=question.collection.configuration.value("ui.modern.search_link")>
    <#-- We must test various combinations here as different browsers will encode
         some characters differently (i.e. '/' will sometimes be preserved, sometimes
         encoded as '%2F' -->
    <#if QueryString?contains("f." + facetDef.name?url)
        || urlDecode(QueryString)?contains("f." + facetDef.name)
        || urlDecode(QueryString)?contains("f." + facetDef.name?url)>
        : <a href="${.namespace.FacetAllUrl(facetDef, link)?html}">${alltext}</a>
    </#if>
    <@FacetBreadCrumb categoryDefinitions=facetDef.categoryDefinitions selectedCategoryValues=question.selectedCategoryValues separator=separator />
</#macro>

<#function FacetAllUrl facetDef link=question.collection.configuration.value("ui.modern.search_link")>
    <#return link + "?" + removeParam(facetScopeRemove(QueryString, facetDef.allQueryStringParamNames), ["start_rank","duplicate_start_rank"] + facetDef.allQueryStringParamNames) />
</#function>

<#---
    Displays facet title or value of the current category.

    <p>Displays either the facet title if no categories has been
    selected, or the value of the currently selected category.</p>

    <p>For hierarchical facets, displays the latest selected category.</p>

    @param title Whether to display the facet title only, or the category.
    @param class CSS class to apply to the container DIV, <code>shortFacetLabel</code> by default.
    @deprecated The new facets data model has been simplified and can be used directly with native FreeMarker tags
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
    @deprecated The new facets data model has been simplified and can be used directly with native FreeMarker tags
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
                <#if ! def.data?lower_case?matches(".*[/\\\\]"+part?lower_case+"[/\\\\].*")>
                    <#-- part needs to be url-decoded to be displayed nicely
                         e.g. "with spaces" rather than "with%20spaces" -->
                    <#if part_has_next>
                        ${separator} <a href="${question.collection.configuration.value("ui.modern.search_link")}?${removeParam(facetScopeRemove(QueryString, def.allQueryStringParamNames), ["start_rank","duplicate_start_rank"] + def.allQueryStringParamNames)?html}&amp;${def.queryStringParamName?url}=${pathBuilding?url}">${urlDecode(part)?html}</a>
                    <#else>
                        ${separator} ${urlDecode(part)?html}
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
                    ${separator} ${valueLabel?html}
                <#else>
                    ${separator} <a href="${question.collection.configuration.value("ui.modern.search_link")}?${removeParam(facetScopeRemove(QueryString, def.allQueryStringParamNames), ["start_rank","duplicate_start_rank"] + def.allQueryStringParamNames)?html}&amp;${def.queryStringParamName}=${selectedCategoryValues[def.queryStringParamName][0]?url}">
                        ${valueLabel?html}
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
    @deprecated The new facets data model has been simplified and can be used directly with native FreeMarker tags
-->
<#macro MoreOrLessCategories>
    <span class="moreOrLessCategories"><a href="#" onclick="javascript:toggleCategories(this)" style="display: none;">more...</a></span>
</#macro>

<#---
    Displays a link for a facet category value.

    @param class Optional CSS class to use, defaults to <code>categoryName</code>.
    @param link Search link to use. Defaults to <code>ui.modern.search_link</code>.
    @param extraParams Optional extra URL parameters to append to the link. Will be appended as is. Consider using <code>?url</code> when passing a value.
    @deprecated The new facets data model has been simplified and can be used directly with native FreeMarker tags
-->
<#macro CategoryName class="categoryName" link=question.collection.configuration.value("ui.modern.search_link") extraParams="">
    <#if s.categoryValue?exists>
        <span class="${class}">
            <a href="${.namespace.CategoryUrl(link,extraParams)?html}">${s.categoryValue.label?html}</a>
        </span>
    </#if>
</#macro>

<#---
    Returns the URL back to the current page with the current category selected. Note that you probabaly want to HTML escape the result.

    @param link Search link to use. Defaults to <code>ui.modern.search_link</code>.
    @param extraParams Optional extra URL parameters to append to the link. Will be appended as is. Consider using <code>?url</code> when passing a value.
-->
<#function CategoryUrl link=question.collection.configuration.value("ui.modern.search_link") extraParams=""><#compress>
    <#assign paramName = .namespace.categoryValue.queryStringParam?split("=")[0]>
    <#return link + "?" + removeParam(facetScopeRemove(QueryString, paramName), ["start_rank", "duplicate_start_rank", paramName]) + "&" + .namespace.categoryValue.queryStringParam + extraParams />
</#compress></#function>

<#---
    Displays the result count for a facet category value.

    @param class Optional CSS class.
    @deprecated The new facets data model has been simplified and can be used directly with native FreeMarker tags
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
<#macro FacetScope input=true><#compress>
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
        <#if input>
            <input type="checkbox" name="facetScope" id="facetScope" value="${facetScope}" checked="checked">
            <label for="facetScope"><#nested></label>
        <#else>
            ${facetScope}
        </#if>
    </#if>
    </@AfterSearchOnly>
</#compress></#macro>

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
        && (! response.resultPacket.contextualNavigation?exists
            || response.resultPacket.contextualNavigation.categories?size == 0)>
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
            <#assign previousCluster_index = cluster_index in s>
            <#assign previousCluster_has_next = cluster_has_next in s>
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
        && response.resultPacket.contextualNavigation?exists
        && response.resultPacket.contextualNavigation.categories?size &gt; 0>
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
    
    <p> Note the use of this is ⚠ Deprecated for faceted navigation</p>

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
	@param tag HTML tag to wrap faceted navigation categories (defaults to DIV).

    @provides The category as <code>${s.category}</code>.
-->
<#macro Category max=16 nbCategories=0 recursionCategories=[] tag="div" name...>
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
                <#assign category_hax_next = c_has_next in s />
                <#assign category_index = c_index in s />
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
                <#assign category_hax_next = c_has_next in s />
                <#assign category_index = c_index in s />
                <#list c.values as cv>
                    <#-- Find if this category has been selected. If it's the case, don't display it in the list this means 
                         we do not show unselected sibling values of a selected value (this deals with the case a metadata class 
                         has multiple values. -->
                    <#-- URL Fill is special the single category can be selected yet be required to show unselected
                         sub paths, that is why in URL mode only we show all non selected values -->
                    <#if ! question.selectedCategoryValues?keys?seq_contains(urlDecode(cv.queryStringParam?split("=")[0]))
                         || (c.queryStringParamName?contains("|url") && !cv.selected)>
                        <#assign categoryValue = cv in s>
                        <#assign categoryValue_has_next = cv_has_next in s>
                        <#assign categoryValue_index = cv_index in s>

                        <#local nbCategories = nbCategories+1 />
                        <#if nbCategories &gt; max><#return></#if>
                        
                        <#if tag != "">
                            <${tag} class="${class}">
                                <#nested>
                            </${tag}>
                        <#else>
                            <#nested>  
                        </#if>
                    </#if>
                </#list>
                <#-- Recurse in sub categories -->
                <#if category.categories?exists && category.categories?size &gt; 0>
                    <@Category recursionCategories=category.categories max=max tag=tag nbCategories=nbCategories><#nested></@Category>
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
            <#assign cluster_has_next = c_has_next in s />
            <#assign cluster_index = c_index in s />
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
    @param defaultValue Default value to set when there's no CGI value.
    @param additional Any additional HTML attributes to set to the select tag (e.g. <code>&lt;@s.Select ... class=&quot;...&quot; onchange=&quot;...&quot; /&gt;</code>)
-->
<#macro Select name options=[] range="" defaultValue="" additional...>
    <#assign selectedValue = defaultValue />
    <#if question.inputParameterMap[name]?? && question.inputParameterMap[name] != "">
        <#assign selectedValue = question.inputParameterMap[name] />
    </#if>
    <select name="${name}" <#compress>
        <#if additional?exists && additional?is_hash>
            <#list additional?keys as key>${key}="${additional[key]}" </#list>
        </#if>
        </#compress>>
        <#if options?size &gt; 0>
            <#list options as opt>
                <#if opt?contains("=")>
                    <#assign valueAndLabel = opt?split("=")>
                    <option value="${parseRelativeDate(valueAndLabel[0])}" <#if selectedValue == valueAndLabel[0]>selected="selected"</#if>>${valueAndLabel[1]}</option>
                <#else>
                    <option value="${parseRelativeDate(opt)}" <#if selectedValue == opt>selected="selected"</#if>>${parseRelativeDate(opt)}</option>
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
<#macro CurrentDate><p class="date">${currentDate()?datetime?string["MMM d, yyyy h:mm:ss aa"]}</p></#macro>

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
