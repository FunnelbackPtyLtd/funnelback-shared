package com.funnelback.publicui.search.web.views.freemarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

/**
 * <p>Utility method used in faceted navigation when building
 * facets breadcrumbs.</p>
 * 
 * <p>It's used to remove a given list of facet constraint from
 * the <tt>facetScope</tt> parameter of the query string.
 *
 */
@Log4j
public class FacetScopeRemoveMethod extends AbstractTemplateMethod {

    public static final String NAME = "facetScopeRemove"; 
    
    private static final Pattern FACET_SCOPE_PATTERN = Pattern.compile("(^|[&\\?;])("+RequestParameters.FACET_SCOPE+"=)(.*?)(&|$)");
    
    public FacetScopeRemoveMethod() {
        super(2, 0, false);
    }

    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    protected Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        String qs = ((TemplateScalarModel) arguments.get(0)).getAsString();
        TemplateSequenceModel paramNames;
        try {
            // Try with a list
            paramNames = (TemplateSequenceModel) arguments.get(1);
        } catch (ClassCastException cce) {
            // Fall back to a single string
            paramNames = new SimpleSequence();
            ((SimpleSequence) paramNames).add( ((SimpleScalar) arguments.get(1)).getAsString());
        }
        
        Matcher m = FACET_SCOPE_PATTERN.matcher(qs);
        if (m.find()) {
            String facetScope = URLDecoder.decode(m.group(3), "UTF-8");
            
            for (int i=0; i<paramNames.size(); i++) {
                Matcher paramMatcher = buildRegexp(paramNames.get(i).toString()).matcher(facetScope);
                facetScope = paramMatcher.replaceAll("");
                log.debug("Removed '"+paramNames.get(i)+"' from '" + qs + "'");
            }
            
            qs = m.replaceAll("$1$2"+URLEncoder.encode(facetScope, "UTF-8")+"$4");
        }

        return qs;
    }
    
    /**
     * <p>Builds a regular expression to find the facet constraint parameter
     * within the <tt>facetScope</tt> query string parameter.</p>
     * 
     * <p>Also spaces are sometimes encoded as <tt>+</tt> or <tt>%20</tt>, this
     * is taken care of.</p>
     * 
     * @param paramName Name of the facet constraint parameter, such as <tt>f.Location|X</tt>
     * @return
     */
    @SneakyThrows(UnsupportedEncodingException.class)
    private Pattern buildRegexp(String paramName) {
        return Pattern.compile(
                "(^|&|\\?)"
                + URLEncoder.encode(paramName, "UTF-8").replace("+", "(\\+|%20)") + "="
                + "[^&]*"
        );
    }

}
