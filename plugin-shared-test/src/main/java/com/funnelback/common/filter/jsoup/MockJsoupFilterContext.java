package com.funnelback.common.filter.jsoup;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A mock FilterContext to be used when testing Jsoup fiters.
 * 
 * Allows a HTML document to be set, additional metadata and customData to be set
 * and also allows for config settings to be configured.
 * 
 * Example:
 * <code>
 * // Setup the HTML document to use in the test.
 * MockJsoupFilterContext filterContext = new MockJsoupFilterContext(&#x22;&#x3C;html&#x3E;\n&#x22; + 
 *     &#x22;&#x3C;body&#x3E;\n&#x22; + 
 *     &#x22;&#x3C;p&#x3E;The HTML document used to test your filter &#x3C;/p&#x3E;\n&#x22; + 
 *     &#x22;&#x3C;/body&#x3E;\n&#x22; + 
 *     &#x22;&#x3C;/html&#x3E;&#x22;);
 * 
 * // Also configure some collection.cfg settings.
 * filterContext.getSetup().getConfigSettings().put(&#x22;myfilter.enabled&#x22;, &#x22;true&#x22;);
 * </code>
 *
 */
@RequiredArgsConstructor
public class MockJsoupFilterContext implements FilterContext {

    @Getter private final MockJsoupSetupContext setup;    
    
    @Getter private final Document document;
    
    @Getter private final Multimap<String, String> additionalMetadata = HashMultimap.create();
    
    @Getter private final Map<String, Object> customData = new HashMap<String, Object>();  
    
    public MockJsoupFilterContext(String html) {
        this(new MockJsoupSetupContext(), Jsoup.parse(html));
    }
}
