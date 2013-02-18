<#---
    New search tags for the Public UI.

    <p>This library contains tags that don't exist in the Classic UI.</p>
    <p>They either provide improved functionality over the Classic UI or are related to new features.</p>
    <p>Some tags assume that the library has been imported under the <code>fb</code> namespace.</p>
-->

<#--- @begin Navigation -->
<#---
    Generates a link to the previous page of results.

    <p>Example:

        <pre>
&lt;@fb.Prev&gt;
    &lt;a href="${fb.prevUrl}"&gt;Previous ${fb.prevRanks} results&lt;/p&gt;
&lt;/@fb.Prev&gt;
        </pre>
    </p>

    @provides The URL of the previous page, as <code>${fb.prevUrl}</code>, the number of results on the previous page, as <code>${fb.prevRanks}</code>.
-->
<#macro Prev>
    <#if response?exists && response.resultPacket?exists && response.resultPacket.resultsSummary?exists>
        <#if response.resultPacket.resultsSummary.prevStart?exists>
            <#assign prevUrl = question.collection.configuration.value("ui.modern.search_link") + "?"
                + changeParam(QueryString, "start_rank", response.resultPacket.resultsSummary.prevStart) in fb />
            <#assign prevRanks = response.resultPacket.resultsSummary.numRanks in fb />
            <#nested>
        </#if>
    </#if>
</#macro>

<#---
    Generates a link to the next page of results.

    <p>Example:

        <pre>
&lt;@fb.Next&gt;
    &lt;a href="${fb.nextUrl}"&gt;Next ${fb.nextRanks} results&lt;/p&gt;
&lt;/@fb.Next&gt;
        </pre>
    </p>

    @provides The URL of the next page, as <code>${fb.nextUrl}</code>, the number of results on the next page, as <code>${fb.nextRanks}</code>.
-->
<#macro Next>
    <#if response?exists && response.resultPacket?exists && response.resultPacket.resultsSummary?exists>
        <#if response.resultPacket.resultsSummary.nextStart?exists>
            <#assign nextUrl = question.collection.configuration.value("ui.modern.search_link") + "?"
                + changeParam(QueryString, "start_rank", response.resultPacket.resultsSummary.nextStart) in fb />
            <#assign nextRanks = response.resultPacket.resultsSummary.numRanks in fb />
            <#nested>
        </#if>
    </#if>
</#macro>

<#---
    Generates links to result pages.

    <p>Iterate over the nested content for each available page</p>

    <p>
        Three variables will be set in the template:
        <ul>
            <li><code>fb.pageUrl</code>: Url of the page.</li>   
            <li><code>fb.pageCurrent</code>: boolean, whether the current page is the one currently displayed.</li>   
            <li><code>fb.pageNumber</code>: Number of the current page.</li>   
        </ul>
    </p>

    <p>Example:

        <pre>
&lt;@fb.Page&gt;
    &lt;#if fb.pageCurrent&gt;
        ${fb.pageNumber}
    &lt;#else&gt;
        &lt;a href="${fb.pageUrl}"&gt;${fb.pageNumber}&lt;/a&gt;
    &lt;/#if&gt;
&lt;/@fb.Page&gt;
        </pre>

    </p>

-->
<#macro Page>
    <#local rs = response.resultPacket.resultsSummary />
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
        <#assign pageNumber = pg in fb />
        <#assign pageUrl = question.collection.configuration.value("ui.modern.search_link") + "?" + changeParam(QueryString, "start_rank", (pg-1) * rs.numRanks+1) in fb />

        <#if pg == currentPage>
            <#assign pageCurrent = true in fb />
        <#else>
            <#assign pageCurrent = false in fb />
        </#if>
        <#nested>

    </#list>
</#macro>

<#--- @end -->

<#--- @begin Extra searches -->
<#---
    Perform an additional search and process results.

    <p>The data can then be accessed using the standard <code>question</code>,
    <code>response</code> and <code>error</code> objects from within the tag.</p>

    <p>Note that the search is run when the tag is actually evaluated. This
    could impact the overall response time. For this reason it's recommended
    to use <code>@fb.ExtraResults</code>.</p>

    @param question Initial SearchQuestion, used as a base for parameters.
    @param collection Name of the collection to search on.
    @param query Query terms to search for.
    @param params Map of additional parameters (ex: <code>{"num_ranks" : "3"}</code>).
-->
<#macro ExtraSearch question collection query params={}>
    <#local questionBackup = question!{} />
    <#local responseBackup = response!{} />
    <#local errorBackup = error!{} />
    <#local extra = search(question, collection, query, params)>
    <#global question = extra.question!{} />
    <#global response = extra.response!{} />
    <#global error = extra.error!{} />
    <#nested>
    <#global question = questionBackup />
    <#global response = responseBackup />
    <#global error = errorBackup />
</#macro>

<#---
    Process results coming from an extra search.

    <p>The extra search needs to be properly configured in
    <code>collection.cfg</code> for the results to be available.</p>

    <p>An example configuration is:
        <ol>
            <li>
                <strong>Create extra search config file</strong> (<code>$SEARCH_HOME/conf/$COLLECTION_NAME/extra_search.<extra search name>.cfg</code>)<br />
                <code>collection=&lt;collection name to search&gt;</code><br />
                <code>query_processor_options=-num_ranks3</code>
            </li>
            <li><strong>Reference extra search config in collection.cfg</strong><br />
                <code>ui.modern.extra_searches=&lt;extra search name&gt;</code>
            </li>
            <li><strong>Add extra search form code to search template</strong><br />
                <pre>
                &lt;div id="extraSearch"&gt;<br />
                    &lt;@fb.ExtraResults name="&lt;extra search name&gt;"&gt;<br />
                        &lt;#if response.resultPacket.results?size &lt; 0&gt;<br />
                            &lt;h3>Related news&gt;/h3&gt;<br />
                                &lt;#list response.resultPacket.results as result&gt;<br />
                                    &lt;p class="fb-extra-result"&gt;<br />
                                        ${result.title}<br />
                                    &lt;/p&gt;<br />
                                &lt;/#list&gt;<br />
                            &lt;/div&gt;<br />
                        &lt;/#if&gt;<br />
                    &lt;/@fb.ExtraResults&gt;<br />
                &lt;/div&gt;<br />
                </pre>
            </li>
        </ol>
    </p>

    @param name Name of the extra search results to process, as configured in <code>collection.cfg</code>.
-->
<#macro ExtraResults name>
    <#if extra?exists && extra[name]?exists>
        <#local questionBackup = question!{} />
        <#local responseBackup = response!{} />
        <#if error?exists>
            <#local errorBackup = error />
        </#if>

        <#global question = extra[name].question!{} />
        <#global response = extra[name].response!{} />
        <#if extra[name].error?exists>
            <#global error = extra[name].error />
        </#if>

        <#nested>

        <#global question = questionBackup />
        <#global response = responseBackup />
        <#if errorBackup?exists>
            <#global error = errorBackup />
        </#if>
    <#else>
        <!-- No extra results for '${name}' found -->
    </#if>
</#macro>

<#--- @end -->

<#--- @begin Administration -->

<#---
    Display content for admins only.

    <p>Executes nested content only if the page is viewed
    from the Admin UI service (Based on the HTTP port used)</p>

    <p>This is used for example to display the preview / live mode banner.</p>
-->
<#macro AdminUIOnly>
    <#if isAdminUI(Request)>
        <#nested />
    </#if>
</#macro>

<#---
    Displays the preview / live banner.

    <p>Displays the banner to switch between live and preview mode
    for the current form. Use <code>AdminUIOnly</code> to display it
    only from the Admin UI service.</p>
-->
<#macro ViewModeBanner>
    <@AdminUIOnly>
        <#local style="padding: 5px; font-family: Verdana; text-align: right; border: solid 2px #aaa; font-size: small;" />
        <#local returnTo=ContextPath+"/"+question.collection.configuration.value("ui.modern.search_link")+"?"+QueryString />
        <#if question.profile?ends_with("_preview")>
            <div id="funnelback_form_mode" style="background-color: lightblue; ${style}">
                <span id="publish_link"></span>
                &middot; <a href="${SearchPrefix}admin/edit-form.cgi?collection=${question.collection.id}&amp;profile=${question.profile}&amp;f=${question.form}.ftl&amp;return_to=${returnTo?url}" title="Edit this form">edit form</a>
                &middot; <a href="?${changeParam(QueryString, 'profile', question.profile?replace("_preview", ""))}" title="View this search with the current live form">switch to live mode</a>
                | <span title="This form file may be edited before publishing to external search users">preview mode</span> 
            </div>
            <script type="text/javascript">
                function loadPublishLink() {
                    jQuery(function() {
                        jQuery("#publish_link").load("${SearchPrefix}admin/ajax_publish_link.cgi?collection=${question.collection.id}&amp;dir=profile-folder-${question.profile}&amp;f=${question.form}.ftl&amp;mode=publish&amp;return_to=${returnTo?url}");
                    });
                }

                if (typeof jQuery === 'undefined') {
                
                    // We need to load jQuery first.
                    // Slam a script tag into the head. Based on
                    // http://stackoverflow.com/questions/4523263#4523417
                    
                    var head=document.getElementsByTagName('head')[0];
                    var script= document.createElement('script');
                    script.type= 'text/javascript';
                    script.onreadystatechange = function () {
                        if (this.readyState == 'complete' || this.readyState == 'loaded') {
                            loadPublishLink();
                        }
                    }
                    script.onload = loadPublishLink;
                    script.src = "js/jquery/jquery-1.4.2.min.js";
                    head.appendChild(script);
                } else {
                    loadPublishLink();
                }
            </script>
        <#else>
            <div id="funnelback_form_mode" style="background-color: lightgreen; ${style}">
                <a href="?${changeParam(QueryString, 'profile', question.profile+'_preview')}" title="View this search with the current preview form">switch to preview mode</a>
                | <span title="This form file is currently published for external search users">live mode</span> 
            </div>
        </#if>
    </@AdminUIOnly>
</#macro>

<#--- @end -->

<#--- @begin Error handling -->

<#---
    Displays search error message.

    <p>Displays the error to the user and the technical message in an <code>HTML</code> comment + JS console.</p>

    @param defaultMessage Default message to use if there's no detailed error message.
-->
<#macro ErrorMessage defaultMessage="An unkown error has occured. Please try again">
    <#-- PADRE error -->
    <#if response?exists && response.resultPacket?exists
        && response.resultPacket.error?exists>
        <p class="fb-error">${response.resultPacket.error.userMsg!defaultMessage?html}</p>
        <!-- PADRE return code: [${response.returnCode!"Unkown"}], admin message: ${response.resultPacket.error.adminMsg!?html} -->
        <@ErrorMessageJS message="PADRE return code: "+response.returnCode!"Unknown" messageData=response.resultPacket.error.adminMsg! />
    </#if>
    <#-- Other errors -->
    <#if error?exists>
        <!-- ERROR status: ${error.reason!?html} -->
        <#if error.additionalData?exists>
            <p class="fb-error">${error.additionalData.message!defaultMessage?html}</p>
            <!-- ERROR cause: ${error.additionalData.cause!?html} --> 
            <@ErrorMessageJS message=error.additionalData.message! messageData=error.additionalData.cause! />
        <#else>
            <p class="fb-error">${defaultMessage}</p>
        </#if>
    </#if>
</#macro>

<#---
    Displays error messages in the JS console.

    @param message Message to display.
    @param messageData Additional data to display.
-->
<#macro ErrorMessageJS message="" messageData="">
    <script type="text/javascript">
        try {
            console.log("Funnelback: " + "${message?replace("\"", "\\\"")?replace("\n", "\\n")}");
            console.log("Funnelback: " + "${messageData?replace("\"", "\\\"")?replace("\n", "\\n")}");
        } catch (ex) {
        }
    </script>
</#macro>

<#--- @end -->

<#-- @begin Multiple facet selection -->
<#---
    Multiple facet selection: Facet tag.

    <p>Equivalent of the <code><@Facet /></code> tag but allowing to select
    multiple categories using checkboxes.</p>

    <p>If both <code>name</code> and <code>names</code> are not set
    this tag iterates over all the facets.</p>

    @param name Name of a specific facet to display, optional.
    @param names A list of specific facets to display, optional.

    @provides The current facet being iterated as <code>${fb.facet}</code>.
-->
<#macro MultiFacet name="" names=[]>
    <#if response?exists && response.facets?exists>
        <#-- We use checkboxes, so enclose them in a form tag -->
        <form class="fb-facets-multiple">
        <#list response.facets as f>
            <#if ((f.name == name || names?seq_contains(f.name)) ||
                    (name == "" && names?size == 0))
                && (f.hasValues() || question.selectedFacets?seq_contains(f.name))>
                <#assign facet = f in fb>
                <#assign facet_has_next = f_has_next in fb>
                <#assign facet_index = f_index in fb>
                <#-- Do we have values for this facet in the extra searches ? -->
                <#if question.selectedFacets?seq_contains(f.name) && extra?exists
                    && extra[ExtraSearches.FACETED_NAVIGATION]?exists
                    && extra[ExtraSearches.FACETED_NAVIGATION].response?exists
                    && extra[ExtraSearches.FACETED_NAVIGATION].response.facets?exists>
                    <#list extra[ExtraSearches.FACETED_NAVIGATION].response.facets as extraFacet>
                        <#if extraFacet.name == f.name>
                            <#assign facet = extraFacet in fb>
                            <#assign facet_has_next = extraFacet_has_next in fb>
                            <#assign facet_index = extraFacet_index in fb>
                            <#break>
                        </#if>
                    </#list>
                </#if>
                <#nested>
            </#if>
        </#list>
        </form>
    </#if>
</#macro>

<#---
    Multiple facet selection: Categories tag.

    <p>Displays categories for a given facet with possibility
    of multiple selection using checkboxes. Will iterate over
    every categories of the given facet.</p>

    @param facet Facet to display categories for.
-->
<#macro MultiCategories facet>
    <#list facet.categories as category>
        <@MultiCategory category=category facetSelected=question.selectedFacets?seq_contains(facet.name) />
    </#list>
</#macro>

<#---
    Multiple facet selection: Category tag.

    <p>Displays a category, its value and all its sub-categories values
    recursively.</p>

    @param category Category to display.
    @param facetSelected  Whether the parent facet of the category has been selected by the user or not.
-->
<#macro MultiCategory category facetSelected=false>
    <div class="category">
        <span class="categoryLabel">${category.label!""}</span>

        <#-- Direct values -->
        <@MultiValues values=category.values facetSelected=facetSelected />

        <#-- Sub categories -->
        <#list category.categories as subCategory>
           <@MultiCategory category=subCategory facetSelected=facetSelected />
        </#list>
    </div>
</#macro>

<#---
    Multiple facet selection: Values tag.

    <p>Display the values of a category with a checkbox allowing multiple selections.</p>

    @param values : List of values to display.
    @param facetSelected : Whether the parent facet of the category which this value belongs has been selected by the user or not.
    @param max Maximum number of values to display.
-->
<#macro MultiValues values facetSelected max=16>
    <#if values?exists && values?size &gt; 0>
        <ul>
        <#local count = 0>
        <#list values as val>
            <#local count=count+1>
            <#if count &gt; max><#break></#if>

            <#local paramName = val.queryStringParam?split("=")[0] />
            <#local paramValue = urlDecode(val.queryStringParam?split("=")[1]) />
            <#local checked = question.selectedCategoryValues[paramName]?exists && question.selectedCategoryValues[paramName]?seq_contains(paramValue) />
            <li>
                <input type="checkbox" class="fb-facets-value"
                    <#if checked>checked="checked"</#if>
                    name="${paramName}"
                    value="${paramValue}">
                <span class="categoryName<#if checked> selected</#if>">${val.label}</span><#if ! facetSelected> (<span class="categoryCount">${val.count}</span>)</#if><br />
            </li>
        </#list>
        </ul>
    </#if>
</#macro>

<#-- @end -->

<#---
    Checks if a query blending occurred and provide a link to cancel it.

    @param prefix : Prefix to blended query terms, defaults to &quot;Your query has been expanded to: &quot;.
    @param linkText : Text for the link to cancel query blending, defaults to &quot;Click here to use verbatim query&quot;.
-->
<#macro CheckBlending prefix="Your query has been expanded to: " linkText="Click here to use verbatim query">
    <#if response?? && response.resultPacket??
        && response.resultPacket.QSups?? && response.resultPacket.QSups?size &gt; 0>
        ${prefix} <span><#list response.resultPacket.QSups as qsup> ${qsup.query}<#if qsup_has_next>, </#if></#list></span>.
        &nbsp;<a href="?${QueryString}&amp;qsup=off">${linkText}</a>
    </#if>
</#macro>

<#---
    Includes remote content from an URL.
    
    <p>Content is cached to avoid firing an HTTP request for each search results page.</p>

    @param url : URL to request. This is the only mandatory parameter.
    @param expiry : Cache time to live, in seconds (default = 3600). This is a number so you must pass the parameters without quotes: <tt>expiry=3600</tt>.
    @param start : Regular expression pattern (Java) marking the beginning of the content to include. Double quotes must be escaped: <tt>start="start \"pattern\""</tt>.
    @param end : Regular expression pattern (Java) marking the end of the content to include. Double quotes must be escaped too.
    @param username : Username if the remote server requires authentication.
    @param password : Password if the remote server requires authentication.
    @param useragent : User-Agent string to use.
    @param timeout : Time to wait, in seconds, for the remote content to be returned.
    @param convertrelative: Boolean, whether relative links in the included content should be converted to absolute ones.
-->
<#macro IncludeUrl url params...>
    <@IncludeUrlInternal url=url
        expiry=params.expiry
        start=params.start
        end=params.end
        username=params.username
        password=params.password
        useragent=params.useragent
        timeout=params.timeout
        convertrelative=params.convertrelative />
</#macro>

<#---
    Displays TextMiner suggestions (Entity, Definition, Source URL).
-->
<#macro TextMiner>
    <#if response.entityDefinition?exists>
        <a href="${response.entityDefinition.url?html}"><@s.boldicize>${response.entityDefinition.entity?html}</@s.boldicize></a><#if !response.entityDefinition.definition?starts_with("is")>: </#if> <span id="entity-definition">${response.entityDefinition.definition?html}</span>     
    </#if>  
</#macro>

<#---
    Formats a string according to a Locale.

    <p>This tag is usually used with internationalisation.</p>
    <p>Either <tt>key</tt> or <tt>str</tt> must be provided. Using <tt>key</tt> will
    lookup the corresponding translation key in the data model. Using <tt>str</tt> will
    format the <tt>str</tt> string directly.</p>
    <p>When <tt>key</tt> is used, <tt>str</tt> can be used with it as a fallback value if
    the key is not found in the data model. For example <code>&lt;@fb.Format key="results" str="Results for %s" args=[question.query] /&gt;</code>
    will lookup the key <em>results</em> in the translations. If the key is not present,
    then the literal string <em>Results for %s</em> will be used instead.</p>

    <p>See the <em>Modern UI localisation guidelines</em> for more information and examples.</p>

    @param locale The <tt>java.util.Locale</tt> to use, defaults to the current Locale in the <tt>question</tt>.
    @param key Takes the string to format from the translations in the data model (<tt>response.translations</tt>).
    @param str Use a literal string instead of a translation key. For example <em>"%d results match the query %s"</em>. See <tt>java.util.Formatter</tt> for the format specifier documentation.
    @param args Array of arguments to be formatted, for example <tt>[42, "funnelback"]</tt>.
-->
<#macro Format args str="" key="" locale=question.locale>
    <#if key != "">
        <#local s = response.translations[key]!str />
    <#else>
        <#local s = str />
    </#if>

    <#if args??>
        ${format(locale, s, args)}
    <#else>
        ${format(locale, s)}
    </#if>
</#macro>

<#---
    Generates an "optimise" link to the content optimiser (From the admin side only)

    @param label Text to use for the link.
-->
<#macro Optimise label="Optimise">
    <@AdminUIOnly>
        <a class="fb-optimise" href="content-optimiser/runOptimiser.html?optimiser_url=${s.result.indexUrl}&amp;query=${response.resultPacket.query}&amp;collection=${s.result.collection}&amp;=${question.profile}">${label}</a>
    </@AdminUIOnly>
</#macro>

<#--- @begin Session -->
<#---
    Generates a "save" link to save the current result in the results cart
    or a "remove" link if the result is already in the cart
-->
<#macro ResultCart>
    <#if question?? && question.searchUser??
        && response?? && response.resultsCart??>
        <a class="fb-result-cart" data-incart="<#if response.resultsCart[s.result.indexUrl]??>true<#else>false</#if>" data-href-add="cart-add.json?collection=${question.collection.id}&amp;title=${s.result.title?url}&amp;liveUrl=${s.result.liveUrl?url}&amp;summary=${s.result.summary?url}&amp;displayUrl=${s.result.displayUrl}&amp;indexUrl=${s.result.indexUrl?url}" data-href-remove="cart-remove.json?collection=${question.collection.id}&amp;url=${s.result.indexUrl?url}" data-url="${s.result.indexUrl}" href="#"></a>
    </#if>
</#macro>

<#---
    Displays the user search history and clear history links.

    @param lastQueryLabel Label to use for "last queries" text.
    @param clearLabel Label to use for the "clear history" link.
    @param resultsLabel Label to use for the number of "results".
    @param max Max number of entries to show.
-->
<#macro SearchHistory lastQueriesLabel="Last queries" clearLabel="clear" resultsLabel="results" max=5>
    <#if response.searchHistory?size &gt; 0>
        <div id="fb-search-history">
            <h4>${lastQueriesLabel} (<a class="clear" href="search-history-clear.json?collection=${question.collection.id}">${clearLabel}</a>) :</h4>

            <ul>
                <#list response.searchHistory as h>
                    <li<#if h_index &gt;= max> class="fb-more" </#if>><a href="${h.searchUrl}">${h.originalQuery}</a> (${h.totalMatching} ${resultsLabel}) ${h.searchDate?datetime?string}</li> 
                </#list>
            </ul>
        </div>
    </#if>

</#macro>

<#---
    Displays the user click history and clear click history links.

    @param lastQueryLabel Label to use for "last results clicked" text.
    @param clearLabel Label to use for the "clear history" link.
    @param max Max number of entries to show.
-->
<#macro ClickHistory lastClicksLabel="Last results clicked" clearLabel="clear" max=5>
    <#if response.clickHistory?size &gt; 0>
        <div id="fb-click-history">
            <h4>${lastClicksLabel} (<a class="clear" href="click-history-clear.json?collection=${question.collection.id}">${clearLabel}</a>) :</h4>

            <ul>
                <#list response.clickHistory as h>
                    <li<#if h_index &gt;= max> class="fb-more" </#if>><a href="${h.indexUrl}">${h.indexUrl}</a></li>
                </#list>
            </ul>
        </div>
    </#if>
</#macro>

<#---
    Displays the list of saved search results.

    @param savedResultsLabel to use for "saved results".
    @param clearLabel Label to use for the "clear cart" link.
-->
<#macro ResultsCart savedResultsLabel="Saved results" clearLabel="clear">
    <div id="fb-results-cart" style="display: none;">
        <h4>${savedResultsLabel} (<a class="clear" href="cart-clear.json?collection=${question.collection.id}">${clearLabel}</a>) :</h4>

        <ul></ul>
    </div>
</#macro>
<#--- @end Session -->
