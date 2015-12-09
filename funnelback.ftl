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
<#macro Prev link=question.collection.configuration.value("ui.modern.search_link") startParamName="start_rank">
    <#if response?exists && response.resultPacket?exists && response.resultPacket.resultsSummary?exists>
        <#if response.resultPacket.resultsSummary.prevStart?exists>
            <#assign prevUrl = link + "?"
                + changeParam(QueryString, startParamName, response.resultPacket.resultsSummary.prevStart) in fb />
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
<#macro Next link=question.collection.configuration.value("ui.modern.search_link") startParamName="start_rank">
    <#if response?exists && response.resultPacket?exists && response.resultPacket.resultsSummary?exists>
        <#if response.resultPacket.resultsSummary.nextStart?exists>
            <#assign nextUrl = link + "?"
                + changeParam(QueryString, startParamName, response.resultPacket.resultsSummary.nextStart) in fb />
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

    @param numPages Number of pages links to display (default = 10)
-->
<#macro Page numPages=10 link=question.collection.configuration.value("ui.modern.search_link") startParamName="start_rank">
    <#local rs = response.resultPacket.resultsSummary />
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
        <#assign pageNumber = pg in fb />
        <#assign pageUrl = link + "?" + changeParam(QueryString, startParamName, (pg-1) * rs.numRanks+1) in fb />

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
    @param params Map of additional parameters (ex: <code>{&quot;num_ranks&quot; : &quot;3&quot;}</code>).
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

    <p>Extra searches are run in parallel of the main query and take advantage
    of multi-core machines. It's recommended to use it rather than <code>@fb.ExtraSearch</code></p>

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
                &lt;div id=&quot;extraSearch&quot;&gt;<br />
                    &lt;@fb.ExtraResults name=&quot;&lt;extra search name&gt;&quot;&gt;<br />
                        &lt;#if response.resultPacket.results?size &lt; 0&gt;<br />
                            &lt;h3>Related news&gt;/h3&gt;<br />
                                &lt;#list response.resultPacket.results as result&gt;<br />
                                    &lt;p class=&quot;fb-extra-result&quot;&gt;<br />
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
    <#if extraSearches?exists && extraSearches[name]?exists>
        <#local questionBackup = question!{} />
        <#local responseBackup = response!{} />
        <#if error?exists>
            <#local errorBackup = error />
        </#if>

        <#global question = extraSearches[name].question!{} />
        <#global response = extraSearches[name].response!{} />
        <#if extraSearches[name].error?exists>
            <#global error = extraSearches[name].error />
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
                &middot; <a href="?${changeParam(QueryString, 'profile', question.profile?replace("_preview", ""))?html}" title="View this search with the current live form">switch to live mode</a>
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
                    script.src = "${GlobalResourcesPrefix}js/jquery/jquery-1.10.2.min.js";
                    head.appendChild(script);
                } else {
                    loadPublishLink();
                }
            </script>
        <#else>
            <div id="funnelback_form_mode" style="background-color: lightgreen; ${style}">
                <a href="?${changeParam(QueryString, 'profile', question.profile+'_preview')?html}" title="View this search with the current preview form">switch to preview mode</a>
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
        <p class="search-error">${response.resultPacket.error.userMsg!defaultMessage?html}</p>
        <!-- PADRE return code: [${response.returnCode!"Unkown"}], admin message: ${response.resultPacket.error.adminMsg!?html} -->
        <@ErrorMessageJS message="PADRE return code: "+response.returnCode!"Unknown" messageData=response.resultPacket.error.adminMsg! />
    </#if>
    <#-- Other errors -->
    <#if error?exists>
        <!-- ERROR status: ${error.reason!?html} -->
        <#if error.additionalData?exists>
            <p class="search-error">${error.additionalData.message!defaultMessage?html}</p>
            <!-- ERROR cause: ${error.additionalData.cause!?html} --> 
            <@ErrorMessageJS message=error.additionalData.message! messageData=error.additionalData.cause! />
        <#else>
            <p class="search-error">${defaultMessage}</p>
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

            <#local paramName = urlDecode(val.queryStringParam?split("=")[0]) />
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
    @param tag : Tag to use to wrap the expanded queries.
-->
<#macro CheckBlending prefix="Your query has been expanded to: " linkText="Click here to use verbatim query" tag="span">
    <#if response?? && response.resultPacket??
        && response.resultPacket.QSups?? && response.resultPacket.QSups?size &gt; 0>
        ${prefix} <${tag}><#list response.resultPacket.QSups as qsup> ${qsup.query}<#if qsup_has_next>, </#if></#list></${tag}>.
        &nbsp;<a href="?${QueryString}&amp;qsup=off">${linkText}</a>
    </#if>
</#macro>

<#---
    Includes remote content from an URL.
    
    <p>Content is cached to avoid firing an HTTP request for each search results page.</p>

    @param url : URL to request. This is the only mandatory parameter.
    @param expiry : Cache time to live, in seconds (default = 3600). This is a number so you must pass the parameters without quotes: <tt>expiry=3600</tt>.
    @param start : Regular expression pattern (Java) marking the beginning of the content to include. Double quotes must be escaped: <tt>start=&quot;start \&quot;pattern\&quot;&quot;</tt>.
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
        convertRelative=params.convertRelative
        convertrelative=params.convertrelative />
</#macro>

<#---
    Displays TextMiner suggestions (Entity, Definition, Source URL).
-->
<#macro TextMiner>
    <#if response.entityDefinition?exists>
        <a href="${response.entityDefinition.url?html}"><@s.boldicize>${response.entityDefinition.entity?html}</@s.boldicize></a><#if !response.entityDefinition.definition?starts_with("is")>: </#if> <span>${response.entityDefinition.definition?html}</span>     
    </#if>  
</#macro>

<#---
    Formats a string according to a Locale.

    <p>This tag is usually used with internationalisation.</p>
    <p>Either <tt>key</tt> or <tt>str</tt> must be provided. Using <tt>key</tt> will
    lookup the corresponding translation key in the data model. Using <tt>str</tt> will
    format the <tt>str</tt> string directly.</p>
    <p>When <tt>key</tt> is used, <tt>str</tt> can be used with it as a fallback value if
    the key is not found in the data model. For example <code>&lt;@fb.Format key=&quot;results&quot; str=&quot;Results for %s&quot; args=[question.query] /&gt;</code>
    will lookup the key <em>results</em> in the translations. If the key is not present,
    then the literal string <em>Results for %s</em> will be used instead.</p>

    <p>See the <em>Modern UI localisation guidelines</em> for more information and examples.</p>

    @param locale The <tt>java.util.Locale</tt> to use, defaults to the current Locale in the <tt>question</tt>.
    @param key Takes the string to format from the translations in the data model (<tt>response.translations</tt>).
    @param str Use a literal string instead of a translation key. For example <em>&quot;%d results match the query %s&quot;</em>. See <tt>java.util.Formatter</tt> for the format specifier documentation.
    @param args Array of arguments to be formatted, for example <tt>[42, &quot;funnelback&quot;]</tt>.
-->
<#macro Format args=[] str="" key="" locale=question.locale>
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
    Generates an &quot;optimise&quot; link to the content optimiser (From the admin side only)

    @param label Text to use for the link.
-->
<#macro Optimise label="Optimise">
    <@AdminUIOnly>
        <a class="search-optimise" href="content-optimiser.html?optimiser_url=${s.result.indexUrl}&amp;query=${response.resultPacket.query}&amp;collection=${s.result.collection}&amp;profile=${question.profile}">${label}</a>
    </@AdminUIOnly>
</#macro>

<#---

    Generates a link to show the results that were collapsed with this specific result.

    @param defaultLabel Label to use when there's no label for the current collapsing column
    @param labels Text to use for the link. <code>{0}</code> will be replaced by the number of collapsed results. This is a hash where the key is the collapsing column, as a String.

-->
<#macro Collapsed defaultLabel="{0} very similar results" defaultApproximateLabel="About {0} very similar results" labels={}>
    <#if s.result.collapsed??>
        <#assign text = defaultLabel />
        <#if response.resultPacket.resultsSummary.estimatedCounts>
                <#assign text = defaultApproximateLabel>
        </#if>
        <#if labels[s.result.collapsed.column]??>
            <#assign text = labels[s.result.collapsed.column] />
        </#if>
        <a class="search-collapsed" href="?${removeParam(QueryString, ["start_rank"])?html}&amp;s=%3F:${s.result.collapsed.signature}&amp;fmo=on&amp;collapsing=off">${text?replace("{0}", s.result.collapsed.count)}</a>
    </#if>
</#macro>

<#---
    Displays a table with the time taken by each step in the query lifecycle.

    @param width Width in pixels to use for the bar graphs
    @msLabel Label to use for &quot;milliseconds&quot;
    @totalLabel Label to use for the &quot;Total&quot; summary row
    @jsOnly Do not display the metrics, only output the processing time in the JS console.
-->
<#macro PerformanceMetrics width=500 msLabel="ms" totalLabel="Total" jsOnly=false class="search-metrics" tdClass="" title="<h3>Performance</h3>">
    <#if response?? && response.performanceMetrics??>
        ${response.performanceMetrics.stop()}
        <script type="text/javascript">
            try {
                console.log("Query processing time: ${response.performanceMetrics.totalTimeMillis} ${msLabel}");
            } catch (ex) {
            }
        </script>
        <#if ! jsOnly>
            <#assign scale= width / response.performanceMetrics.totalTimeMillis />
            <#assign offset=0 />

            ${title}

            <table class="${class}">
            <thead>
                <tr>
                    <th>Component</th>
                    <th>Time</th>
                    <th>Chart</th>
                </tr>
            </thead>
            <tbody>
            <#list response.performanceMetrics.taskInfo as ti>
                <#assign timeTaken = ti.timeMillis * scale />
                <#assign kv = (ti.taskName!":")?split(":") />
                <#assign valueClass=kv[0]! />
                <#assign name=kv[1]! />
                <tr>
                    <td>${name}</td>
                    <td>${ti.timeMillis!} ${msLabel}</td>
                    <td><div class="metric ${tdClass} ${valueClass}" style="width: ${timeTaken?round}px; margin-left: ${offset}px;">&nbsp;</div></td>
                </tr>
                <#assign offset = offset+(timeTaken) />
            </#list>
                <tr>
                    <th>${totalLabel}</th>
                    <th colspan="2">${response.performanceMetrics.totalTimeMillis} ${msLabel}</th>
                </tr>
            </tbody>
            </table>
        </#if>
    </#if>
</#macro>

<#---
    Generates an authentication token suitable for click tracking redirection,
    based on the globally configured <tt>server_secret</tt>.

    @nested URL to generate the token for.
-->
<#macro AuthToken><#compress>
    <#assign content><#nested></#assign>
    ${authToken(content)}
</#compress></#macro>

<#---
    Converts a size to its equivalent in gigabytes, megabytes, or kilobytes as appropriate.
    kilobytes are rounded to no decimal places, giga and mega take one decimal place.
    Conversion is done by 1024, not 1000.
    returns a string
-->
<#function renderSize sz>
    <#local giga = 1024 * 1024 * 1024>
    <#local mega = 1024 * 1024>
    <#local kilo = 1024>
    <#if (sz > giga) >
        <#return (sz / giga)?string[".#"] + "&nbspGB">
    <#elseif (sz > mega)>
        <#return (sz / mega)?string[".#"] + "&nbspMB">
    <#elseif (sz > kilo)>
        <#return (sz / kilo)?int + "&nbspKB">
    <#else>
        <#return sz + "&nbspB">
    </#if>
</#function>

<#-- @begin Session features -->

<#---
    Check if the user click history is empty or not. Writes 'true' if it is, 'false' otherwise.
-->
<#macro HasClickHistory><#if session?? && session.clickHistory?size &gt; 0>true<#else>false</#if></#macro>

<#---
    Check if the user search history is empty or not. Writes 'true' if it is, 'false' otherwise.
-->
<#macro HasSearchHistory><#if session?? && session.searchHistory?size &gt; 0>true<#else>false</#if></#macro>

<#-- @end -->

<#-- @begin Session features (Deprecated in v13) -->

<#---
    DEPRECATED in v13: Display the result cart
-->
<#macro ResultsCart savedResultsLabel="Saved results" clearLabel="clear"></#macro>

<#---
    DEPRECATED in v13: Display the click history
-->
<#macro ClickHistory lastClicksLabel="Last results clicked" clearLabel="clear" max=5></#macro>

<#---
    DEPRECATED in v13: Display the search history
-->
<#macro SearchHistory lastQueriesLabel="Last queries" clearLabel="clear" resultsLabel="results" max=5></#macro>

<#---
    DEPRECATED in v13: Display a link to add a result to the cart
-->
<#macro ResultCart></#macro> 

<#-- @end -->
