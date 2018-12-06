package com.funnelback.publicui.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Includes an external URL.
 * Replacement for NickScript's IncludeUrl plugin
 * Supports HTTP redirects, however 'meta-refresh' tags/redirects within the included url are not supported.
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
 * the element and its attributes. When this is option is enabled the document may be slightly modified
 * to be a valid HTML document before the cssSelector is applied this includes wrapping in
 * <pre>html</pre> tags and <pre>body</pre> tags. This may need to be taken into account when
 * creating the selector. The resulting HTML will only include the <pre>html</pre> if that element 
 * is selected. This is run before regex modifications and before removeByCssSelector.
 * - removeByCssSelectors: A list of CSS selectors which match elements which should be removed
 * from the included HTML. The HTML may be slightly modified to be a valid HTML document before elements are
 * removed. The modification includes wrapping in <pre>html</pre> tags and adding <pre>body</pre> as well as
 *  <pre>header</pre> tags. As this runs after <pre>cssSelector</pre>, the modification will still be applied
 *  before elements are removed. The resulting HTML that will be returned, to be possible modified by <pre>regex</pre>
 *  or <pre>convertrelative</pre>, will by default be the HTML that is in inside of the <pre>body</pre> tag. See
 *  <pre>keepBodyAndHeader</pre> for how to modify this behaviour.
 * - keepBodyAndHeader: When <pre>removeByCssSelectors</pre> is used, the included HTML will be from
 * the HTML that is within the <pre>body</pre>, which may be automatically added. To instead return
 * the <pre>header</pre> and <pre>body</pre> tags and their contents this should be set to <pre>true</pre>.
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
        cssSelector,removeByCssSelectors, keepBodyAndHeader
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
            
            if ((elt.getLastUpdateTime() + (expiry*1000)) < System.currentTimeMillis()) {
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
    
    protected String fetchContentWithJsoup(String url, String content, Map<String, TemplateModel> params) throws TemplateModelException {
        
        TemplateModel param = params.get(Parameters.cssSelector.toString());
        if (param == null) {
            return content;
        }
        
        String cssSelector = ((TemplateScalarModel) param).getAsString();
        
        Document doc = Jsoup.parse(content);
        // TODO Should we try catch the css selector rather than let the exception bubble up?
        String out = doc.select(cssSelector).stream().findFirst()
            .map(e -> e.outerHtml())
            .orElse(null);
        
        if(out == null) {
            log.warn("No elements found matching css selector {}", cssSelector);
            return "";
        }
        
        return out;
    }
    
    protected String removeContentWithJsoup(String url, String content, Map<String, TemplateModel> params) 
            throws TemplateModelException {
        
        TemplateModel param = params.get(Parameters.removeByCssSelectors.toString());
        if (param == null) {
            return content;
        }
        
        Optional<List<String>> cssSelectors = getListFromSingleStringOrSequence(param);
        
        if(!cssSelectors.isPresent()) {
            log.warn("Could not understand argument for '{}'", Parameters.removeByCssSelectors.toString());
            return content;
        }
        
        if(cssSelectors.get().isEmpty()) return content;
        
        Document doc = Jsoup.parse(content);
        
        
        for(String cssSelector : cssSelectors.get()) {
            for(org.jsoup.nodes.Element e : doc.select(cssSelector)) {
                e.remove();
            }
            
        }
        
        // Do we have a <html>
        if(doc.childNodeSize() == 0) {
            return "";
        }
        
        boolean keepHeaderAndBody = getBoolean(params.get(Parameters.keepBodyAndHeader.toString()), false);
        
        if(keepHeaderAndBody) {
            return doc.children().get(0).html();
        }
        
        // Just extract what is in the body
        for(org.jsoup.nodes.Element e : doc.children().get(0).children()) {
            if("body".equals(e.tagName())) {
                return e.html();
            }
        }
        
        log.debug("Could not find body tag in html, was it excluded by a selector?");
        
        return "";
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
        
        out = fetchContentWithJsoup(url, out, params);
        out = removeContentWithJsoup(url, out, params);
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
