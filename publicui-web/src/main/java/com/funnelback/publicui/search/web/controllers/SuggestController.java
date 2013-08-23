package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.dataapi.connector.padre.suggest.SuggestQuery.Sort;
import com.funnelback.dataapi.connector.padre.suggest.Suggestion;
import com.funnelback.dataapi.connector.padre.suggest.Suggestion.ActionType;
import com.funnelback.dataapi.connector.padre.suggest.Suggestion.DisplayType;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.search.service.Suggester;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;

/**
 * Query completion / suggestion controller.
 */
@Controller
@Log4j
public class SuggestController extends AbstractRunPadreBinaryController {

    private static final String PADRE_QS = "padre-qs";
    
    /** Template token for the query terms */
    private static final String HISTORY_SUGGEST_QUERY = "{query}";
    
    /** Template token for the number of results */
    private static final String HISTORY_SUGGEST_TOTAL_MATCHING = "{totalMatching}";
    
    /** Default weight to assign to suggestions coming from the search history */
    private static final float HISTORY_SUGGEST_DEFAULT_WEIGHT = 0.5f;
    
    @Autowired
    @Setter private ConfigRepository configRepository;
    
    /** Format to return suggestions */
    private enum Format {
        /** "simple" Json format (array of strings) */
        Json("json"),
        /** Complex format where each suggestion is a JSON object */
        JsonPlus("json++"),
        /** Account for <code>+</code> being an encoded space */
        JsonPlusBad("json  ");
        
        public final String value;
        
        private Format(String value) {
            this.value = value;
        }
        
        public static Format fromValue(String value) {
            for (Format f: Format.values()) {
                if (f.value.equals(value)) {
                    return f;
                }
            }
            throw new IllegalArgumentException(value);
        }
    }

    @Autowired
    private File searchHome;
    
    @Autowired
    private Suggester suggester;
    
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    
    @Autowired
    private ExecutionContextHolder executionContextHolder;
    
    @Resource(name="suggestViewSimple")
    private View simpleView;
    
    @Resource(name="suggestViewRich")
    private View richView;

    /**
     * Simple Wrapper around <code>padre-qs.cgi</code>
     * @param request HTTP request
     * @param response HTTP response
     * @throws Exception  
     * @deprecated Use {@link #suggestJava(String, String, String, int, int, String, String)} instead
     */
    @Deprecated
    @RequestMapping(value="/padre-qs.cgi", params=RequestParameters.COLLECTION)
    public void suggest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            runPadreBinary(PADRE_QS, null, request, response, true);
        } catch (PadreForkingException e) {
            SuggestController.log.error("Unable to run " + PADRE_QS, e);
            throw new ServletException(e);
        }
    }

    /**
     * Get suggestions using LibQS
     * @param response HTTP response
     */
    @RequestMapping(value="/suggest.json", params="!"+RequestParameters.COLLECTION)
    public void noCollection(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    
    /**
     * Use the default suggester service, usually backed by LibQS.
     * 
     * @param collectionId Id of the collection
     * @param profile Profile
     * @param partialQuery First letters of a query
     * @param show Number of items to show
     * @param sort Order for suggestions (See LibQS code for possible values)
     * @param format JSON or XML
     * @param alpha Alpha value to tune the suggestions
     * @param category Category to restrict the suggestions to
     * @param callback Name of a JSONP callback if needed
     * @param user Current user from the session
     * @param request 
     * @param response 
     * @return Model containing suggestions 
     * @throws IOException 
     */
    @RequestMapping(value="/suggest.json", params=RequestParameters.COLLECTION)
    public ModelAndView suggestJava(@RequestParam("collection") String collectionId,
            @RequestParam(defaultValue=DefaultValues.DEFAULT_PROFILE) String profile,
            @RequestParam("partial_query") String partialQuery,
            @RequestParam(defaultValue="10") int show,
            @RequestParam(defaultValue="0") int sort,
            @RequestParam(value="fmt", defaultValue="json") String format,
            @RequestParam(defaultValue="0.5") double alpha,
            @RequestParam(required=false) String category,
            String callback,
            @ModelAttribute SearchUser user,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        Collection c = configRepository.getCollection(collectionId);
        if (c != null) {
            // Get organic/CSV suggestions from PADRE
            List<Suggestion> suggestions = suggester.suggest(c, profile, partialQuery, show, Sort.valueOf(sort), alpha, category);

            // Augment them with search history if needed
            augmentSuggestionsWithHistory(suggestions, c, user, getSearchUrl(request, c.getConfiguration()));
            
            ModelAndView mav = new ModelAndView();
            mav.addObject("suggestions", suggestions);
            mav.addObject("callback", callback);
            
            switch(Format.fromValue(format)) {
            case Json:
                mav.setView(simpleView);
                break;
            case JsonPlus:
            case JsonPlusBad:
                mav.setView(richView);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized format " + format);
            }
            
            return mav;
        } else {
            // Collection not found
            log.warn("Collection '"+collectionId+"' not found");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    /**
     * Augment suggestions with the recent queries from the search history
     * @param suggestions List of suggestions to augment
     * @param c Collection
     * @param user Current user from the session
     * @param searchUrl Base URL to generate a link to the previous searches
     */
    private void augmentSuggestionsWithHistory(List<Suggestion> suggestions, Collection c,
        SearchUser user, String searchUrl) {
        
        if (c.getConfiguration().valueAsBoolean(Keys.ModernUI.SESSION, DefaultValues.ModernUI.SESSION)
            && c.getConfiguration().valueAsBoolean(Keys.ModernUI.Session.SearchHistory.SUGGEST,
                DefaultValues.ModernUI.Session.SearchHistory.SUGGEST)) {
            
            // Get search history
            List<SearchHistory> history = searchHistoryRepository.getSearchHistory(user, c,
                c.getConfiguration().valueAsInt(Keys.ModernUI.Session.SearchHistory.SIZE,
                    DefaultValues.ModernUI.Session.SearchHistory.SIZE));
            
            for (SearchHistory h: history) {
                // Build a suggestion for each history
                Suggestion s = new Suggestion();
                s.setKey(h.getOriginalQuery());
                
                s.setAction(searchUrl + "?" + h.getSearchParams());
                s.setActionType(ActionType.URL);
                
                // Display is templated from a collection.cfg parameter
                s.setDisplay(c.getConfiguration().value(Keys.ModernUI.Session.SearchHistory.SUGGEST_DISPLAY_TEMPLATE,
                    DefaultValues.ModernUI.Session.SearchHistory.SUGGEST_DISPLAY_TEMPLATE)
                    .replace(HISTORY_SUGGEST_QUERY, h.getOriginalQuery())
                    .replace(HISTORY_SUGGEST_TOTAL_MATCHING, Integer.toString(h.getTotalMatching())));
                s.setDisplayType(DisplayType.HTML);
                
                s.setCategory(c.getConfiguration().value(Keys.ModernUI.Session.SearchHistory.SUGGEST_CATEGORY,
                    DefaultValues.ModernUI.Session.SearchHistory.SUGGEST_CATEGORY));
                s.setCategoryType("");
                
                s.setWeight(HISTORY_SUGGEST_DEFAULT_WEIGHT);
                
                suggestions.add(s);
            }
        }
    }
    
    /**
     * Get the base URL to perform a search
     * @param request HTTP Request to get the server information
     * @param c Collection configuration to get the search_link
     * @return The search URL
     */
    @SneakyThrows(MalformedURLException.class)
    private String getSearchUrl(HttpServletRequest request, Config c) {
        URL url = new URL(request.getRequestURL().toString());
        StringBuilder out = new StringBuilder();
        
        out.append(url.getProtocol()).append("://")
            .append(url.getAuthority())
            .append(executionContextHolder.getContextPath())
            .append("/")
            .append(c.value(Keys.ModernUI.SEARCH_LINK, DefaultValues.ModernUI.SEARCH_LINK));
        
        return out.toString();
    }
    
    @Override
    protected File getSearchHome() {
        return searchHome;
    }

}
