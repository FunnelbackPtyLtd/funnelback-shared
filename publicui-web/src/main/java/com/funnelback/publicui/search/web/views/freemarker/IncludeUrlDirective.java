package com.funnelback.publicui.search.web.views.freemarker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;
import org.jsoup.select.Selector;

import com.funnelback.common.utils.exception.ExceptionUtils;
import com.funnelback.publicui.i18n.I18n;

import freemarker.core.Environment;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
/**
 * Includes content from an an external URL.
 * Replacement for NickScript's IncludeUrl plugin
 * Supports HTTP redirects, however 'meta-refresh' tags/redirects within the included url are not supported.
 * 
 * <p>Content is cached to avoid firing an HTTP request for each search results page.</p>
 * 
 * <p>Note that the CSS selectors supported are only those that are supported by Jsoup.</p>
 * 
 * <p>when CSS selectors are used the document will be modified by Jsoup.
 * For example when given:</p>
 * <pre>&#x3C;div&#x3E;&#x3C;p&#x3E;foo&#x3C;/p&#x3E;&#x3C;/div&#x3E;</pre>
 * <p>Jsoup will transform that to:</p>
 * <pre>
 * &#x3C;html&#x3E;
 * &#x3C;head&#x3E;
 * &#x3C;/head&#x3E;
 * &#x3C;body&#x3E;
 * &#x3C;div&#x3E;&#x3C;p&#x3E;foo&#x3C;/p&#x3E;&#x3C;/div&#x3E;
 * &#x3C;/body&#x3E;
 * &#x3C;/html&#x3E;
 * </pre>
 * 
 * 
 * Parameters:
 * - url: Absolute URL to include
 * - expiry: Cache TTL, in seconds
 * - start: Regex pattern to consider to start including content
 * - end: Regex pattern to consider to end including content
 * - username: Username if the remote resource require authentication
 * - password: Password if the remote resource require authentication
 * - useragent: Override default user agent
 * - timeout: Time to wait in seconds for the remote resource
 * - convertrelative: wether we should attempt to convert relative links to absolute ones.
 * - cssSelector: CSS selector to use to select the HTML which should be included. The
 * selected element will be the first one to match the selector. The HTML returned will include 
 * the element and its attributes. When this option is enabled the document may be slightly modified
 * to be a valid HTML document before the cssSelector is applied this includes wrapping in
 * '&#x3C;html&#x3E;' tags and '&#x3C;body&#x3E;' tags. See above for more information about Jsoup modification.
 * - removeByCssSelectors: A list of CSS selectors which match elements to be removed
 * from the included HTML. This is run after 'cssSelector', and so if 'cssSelector' is used
 * the CSS selectors must be relative to the previously selected element. If 'cssSelector' is not
 * used the HTML may be slightly modified to be a valid HTML document before elements are
 * removed. See above for more information about Jsoup modification.
 * 
 */
@Log4j2
public class IncludeUrlDirective implements TemplateDirectiveModel {

    public static final String NAME = "IncludeUrlInternal";
    
    public static final int DEFAULT_EXPIRY = 3600; // seconds

    public static final int DEFAULT_TIMEOUT = 50 * 1000; // milliseconds 

    public static final Pattern CONVERT_RELATIVE_PATTERN = Pattern.compile("<([^!>\\.]*?)(href|src|action|background)\\s*=\\s*([\"|']?)(.*?)\\3((\\s+.*?)*)>", Pattern.DOTALL);
    
    protected enum Parameters {
        url, expiry, start, end, username, password, useragent, timeout, convertrelative, convertRelative, 
        cssSelector,removeByCssSelectors
    }
    
    private CacheManager appCacheManager;
    private I18n i18n;
    
    public IncludeUrlDirective(CacheManager appCacheManager, I18n i18n) {
        this.appCacheManager = appCacheManager;
        this.i18n = i18n;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
        throws TemplateException, IOException {
        
        if(log.isDebugEnabled()) {
            // Java is having trouble with the types if we don't set it to a variable :(
            Set<Map.Entry> s = params.entrySet();
            s.stream().forEach(e -> log.debug("IncludeUrl was given: " + e.getKey() + "=" + e.getValue()));
        }
        
        TemplateModel param = (TemplateModel) params.get(Parameters.url.toString());
        if (param == null) {
            env.getOut().write("<!-- " + i18n.tr("parameter.missing", "url") + " -->");
            return;
        }
        
        String url = ((TemplateScalarModel) param).getAsString();
        Cache cache = appCacheManager.getCache(NAME);
            
        Element elt = cache.get(url);
        if (elt != null) {
            log.debug("URL '" + url + "' found in cache");
                
            int expiry = DEFAULT_EXPIRY;
            param = (TemplateModel) params.get(Parameters.expiry.toString());
            if (param != null) {
                expiry = ((TemplateNumberModel) param).getAsNumber().intValue();
            }
            
            long currentTime = System.currentTimeMillis();
            
            log.debug("expire for {} is {} Will get content again if {} + {} < {}", url, expiry,
                elt.getLastUpdateTime(), (expiry*1000), currentTime);
            
            if ((elt.getLastUpdateTime() + (expiry*1000)) < currentTime) {
                log.debug("Cache for URL '" + url + "' expired. Requesting content again");
                try {
                    String content = getContent(env, url, params);
                    
                    if (content != null) {
                        cache.remove(elt);
                        elt = new Element(url, content);
                        cache.put(elt);
                        log.debug("Updated cached version of content for URL '" + url + "'");
                        
                        env.getOut().write(transformContent(url, content, params));
                    } else {
                        // Unable to refresh content, return previous cached version
                        env.getOut().write(transformContent(url, (String) elt.getObjectValue(), params));
                    }
                } catch (Exception e) {
                    env.getOut().write("<!-- " + i18n.tr("freemarker.method.IncludeUrlDirective.refresh.error") + " -->");
                    log.error("Error while requesting request content from url '" + url + "'. Previous cached version will be returned.", e);
                    env.getOut().write(transformContent(url, (String) elt.getObjectValue(), params));
                }
            } else {
                // Cache not expired
                log.debug("Returned cached version for URL '" + url + "'");
                String content = (String) elt.getObjectValue();
                env.getOut().write(transformContent(url, content, params));
            }
            
        } else {
            log.debug("URL '" + url + "' not found in cache");
            
            try {
                String content = getContent(env, url, params);
                if (content != null) {
                    elt = new Element(url, content);
                    elt.setEternal(true);
                    cache.put(elt);
                    
                    env.getOut().write(transformContent(url, content, params));
                } else {
                    env.getOut().write("<!-- No remote content returned -->");
                }
            } catch (Exception e) {
                env.getOut().write("<!-- " + i18n.tr("freemarker.method.IncludeUrlDirective.remote.error") + " -->");
                log.error("Error while requesting request content from URL '" + url + "'", e);
            }
        }
    }
    
    /**
     * Request remote content.
     * @param env Template environment
     * @param url Remote URL
     * @param params Template params
     * @return The remote content, or null if there was a problem in the question parameters
     * @throws TemplateModelException
     * @throws IOException
     * @throws AuthenticationException
     */
    private String getContent(Environment env, String url, Map<String, TemplateModel> params) throws TemplateModelException, IOException, AuthenticationException {
        
        log.debug("Initiating new HTTP request to '" + url + "'");
        HttpClient httpClient = new DefaultHttpClient();
        HttpParams httpParams = httpClient.getParams();
        
        HttpGet httpGet = new HttpGet(url);
        
        int timeout = DEFAULT_TIMEOUT;
        
        TemplateModel param = params.get(Parameters.timeout.toString());
        if (param != null) {
            timeout = ((TemplateNumberModel) param).getAsNumber().intValue();
        }
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        log.debug("Set timeout to '" + timeout + "'");
        
        param = params.get(Parameters.useragent.toString());
        if (param != null) {
            String ua = ((TemplateScalarModel) param).getAsString();
            HttpProtocolParams.setUserAgent(httpParams, ua);
            
            log.debug("Set user agent to '" + ua + "'");
        }
        
        param = params.get(Parameters.username.toString());
        if (param != null) {
            String userName = ((TemplateScalarModel) param).getAsString();
            
            param = params.get(Parameters.password.toString());
            if (param != null) {
                String password = ((TemplateScalarModel) param).getAsString();
                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(userName, password);
                httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet));
                
                log.debug("Set credentials with username '" + userName + "'");
                
            } else {
                env.getOut().write("<!-- " + i18n.tr("freemarker.method.IncludeUrlDirective.remote.password") + " -->");
                return null;
            }
        }
        
        HttpResponse response = httpClient.execute(httpGet);
        if(response.getEntity() != null) {
            InputStream is = response.getEntity().getContent();
            try {
                return IOUtils.toString(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
                
        return null;
    }
    
    protected Optional<ProcessedElement> fetchContentWithJsoup(String url, String content, Map<String, TemplateModel> params) throws TemplateModelException {
        TemplateModel param = params.get(Parameters.cssSelector.toString());
        if (param == null) {
            return Optional.empty();
        }
        
        String cssSelector = ((TemplateScalarModel) param).getAsString();
        
        Evaluator parsedCssSelector;
        try {
            parsedCssSelector = QueryParser.parse(cssSelector);
        } catch (Exception e) {
            log.warn("Could not parse css selector '{}' error message was: '{}'",
                cssSelector, new ExceptionUtils().getAllMessages(e));
            return Optional.empty();
        }
        
        Document doc = Jsoup.parse(content);
        
        if(log.isDebugEnabled()) {
            log.debug("cssSelector '{}' will be used to find an element on modified document '{}'", cssSelector, doc.html());
        }
        
        Optional<org.jsoup.nodes.Element> res = Optional.ofNullable(Selector.select(parsedCssSelector, doc))
            .map(List::stream)
            .orElse(Stream.empty())
            .findFirst();
        
        if(!res.isPresent()) {
            log.warn("No elements found matching css selector {}", cssSelector);
        }
        
        return Optional.of(new ProcessedElement(res));
    }
    
    @AllArgsConstructor
    @Getter
    public static class ProcessedElement {
        public Optional<org.jsoup.nodes.Element> element;
    }
    
    protected Optional<ProcessedElement> removeContentWithJsoup(String url, 
                                String originalContent,
                                Optional<ProcessedElement> processedElement, 
                                Map<String, TemplateModel> params) 
            throws TemplateModelException {
        // If we already processed the document (say selecting an element) and 
        // we ended up with nothing then don't bother attempting to remove elements from nothing
        if(processedElement.isPresent() && (!processedElement.get().getElement().isPresent())) {
            return processedElement;
        }
        Optional<ProcessedElement> result = removeContentWithJsoup(url, 
            () -> {
                    if(processedElement.isPresent()) return processedElement.get().getElement().get();
                    return Jsoup.parse(originalContent);
                }, 
            params);
        
        // Handle the case that the user did not ask to remove any elements 
        if((!result.isPresent()) && processedElement.isPresent()) return processedElement;
        return result;
    }
    
    protected Optional<ProcessedElement> removeContentWithJsoup(String url, 
                                Supplier<org.jsoup.nodes.Element> baseElementSupplier, 
                                Map<String, TemplateModel> params) 
            throws TemplateModelException {
        
        TemplateModel param = params.get(Parameters.removeByCssSelectors.toString());
        if (param == null) {
            return Optional.empty();
        }
        
        Optional<List<String>> cssSelectors = getListFromSingleStringOrSequence(param);
        
        if(!cssSelectors.isPresent()) {
            log.warn("Could not understand argument for '{}'", Parameters.removeByCssSelectors.toString());
            return Optional.empty();
        }
        
        if(cssSelectors.get().isEmpty()) return Optional.empty();
        
        org.jsoup.nodes.Element baseElement = baseElementSupplier.get();
        
        if(log.isDebugEnabled()) {
            log.debug("Css selectors in '{}' will remove elements from modified document '{}'", 
                Parameters.removeByCssSelectors.toString(), baseElement.outerHtml());
        }
        
        for(String cssSelector : cssSelectors.get()) {
            Evaluator parsedCssSelector;
            try {
                parsedCssSelector = QueryParser.parse(cssSelector);
            } catch (Exception e) {
                log.warn("Could not parse css selector '{}' from {} error message was: '{}'.",
                    cssSelector, Parameters.removeByCssSelectors.toString(), new ExceptionUtils().getAllMessages(e));
                continue;
            }
            for(org.jsoup.nodes.Element e : Optional.of(Selector.select(parsedCssSelector, baseElement)).orElse(new Elements())) {
                e.remove();
            }
            
        }
        return Optional.of(new ProcessedElement(Optional.of(baseElement)));
    }
    
    /**
     * Transforms content if required: Extracts the relevant part and convert relative links
     * @param url
     * @param content
     * @param params
     * @return
     * @throws TemplateModelException
     */
     protected String transformContent(String url, String content, Map<String, TemplateModel> params) throws TemplateModelException {
        String out = content;
        
        Optional<ProcessedElement> result = fetchContentWithJsoup(url, out, params);
        result = removeContentWithJsoup(url, out, result, params);
        
        out = result.map(processedElement -> processedElement.getElement().map(e -> e.outerHtml()).orElse(""))
                .orElse(out);
        
        // Extract start-pattern
        TemplateModel param = params.get(Parameters.start.toString());
        if (param != null) {
            String regex = ((TemplateScalarModel) param).getAsString();
            out = Pattern.compile("^.*?(" + regex + ")", Pattern.DOTALL | Pattern.MULTILINE).matcher(out).replaceFirst("$1");
        }

        // Extract end-pattern
        param = params.get(Parameters.end.toString());
        if (param != null) {
            String regex = ((TemplateScalarModel) param).getAsString();
            out = Pattern.compile("(" + regex + ").*$", Pattern.DOTALL | Pattern.MULTILINE).matcher(out).replaceFirst("$1");
        }
        
        // Convert relative URLs
        param = params.get(Parameters.convertRelative.toString());
        if (param == null) {
            // Try with alternative syntax
            param = params.get(Parameters.convertrelative.toString());
        }
        if (param != null) {
            boolean convert = ((TemplateBooleanModel) param).getAsBoolean();
            if (convert) {
                Matcher m = CONVERT_RELATIVE_PATTERN.matcher(out);
                
                int lastStart = 0;
                int lastEnd = 0;
                StringBuffer replaced = new StringBuffer();
                URI baseURI = URI.create(url);
                while(m.find()) {
                    // Copy non-matched part
                    replaced.append(out.substring(lastStart, m.start()));
                    lastStart = m.end();
                    lastEnd = m.end();
                    
                    replaced.append("<")
                        .append(m.group(1)).append(m.group(2)).append("=\"");
                    try {
                        replaced.append(baseURI.resolve(m.group(4)));
                    } catch(Exception e) {
                        log.warn("Unable to resolve '"+m.group(4)+"' against URL " + baseURI, e);
                        // We failed, possibly the URL was invalid. Just use the original string
                        replaced.append(m.group(4));
                    }
                    
                    replaced.append("\"").append(m.group(5)).append(">");
                }

                // Copy last segment
                replaced.append(out.substring(lastEnd));
                out = replaced.toString();
            }
        }

        return out;
    }
     
    private Optional<List<String>> getListFromSingleStringOrSequence(TemplateModel singleStringOrSequence) throws TemplateModelException {
        if(singleStringOrSequence == null) return Optional.of(Collections.emptyList());
        if(singleStringOrSequence instanceof TemplateScalarModel) {
            return Optional.of(Arrays.asList(((TemplateScalarModel) singleStringOrSequence).getAsString()));
        }
        if(singleStringOrSequence instanceof SimpleSequence) {
            SimpleSequence seq = (SimpleSequence) singleStringOrSequence;
            List<String> values = new ArrayList<>();
            for(int i = 0; i < seq.size(); i++) {
                String facetsWanted = ((TemplateScalarModel) seq.get(i)).getAsString();
                values.add(facetsWanted);
            }
            return Optional.of(values);
        }
        
        return Optional.empty();
        
    }
    
    private boolean getBoolean(TemplateModel booleanValue, boolean defaultValue) throws TemplateModelException {
        if (booleanValue == null) {
            return defaultValue;
        }
        return ((TemplateBooleanModel) booleanValue).getAsBoolean();
    }

}
