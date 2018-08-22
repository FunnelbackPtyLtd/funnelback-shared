package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.web.interceptors.helpers.IntercepterHelper;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.ProfileId;
import com.funnelback.dataapi.connector.padre.suggest.SuggestQuery.Sort;
import com.funnelback.dataapi.connector.padre.suggest.Suggestion;
import com.funnelback.dataapi.connector.padre.suggest.Suggestion.ActionType;
import com.funnelback.dataapi.connector.padre.suggest.Suggestion.DisplayType;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.search.service.Suggester;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.search.web.binding.ProfileEditor;
import com.funnelback.publicui.utils.JsonPCallbackParam;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.funnelback.springmvc.web.binder.GenericEditor;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

/**
 * Query completion / suggestion controller.
 */
@Controller
@Log4j2
public class SuggestController extends AbstractRunPadreBinaryController {

    /** Template token for the query terms */
    private static final String HISTORY_SUGGEST_QUERY = "{query}";
    
    /** Template token for the number of results */
    private static final String HISTORY_SUGGEST_TOTAL_MATCHING = "{totalMatching}";
    
    /** Default weight to assign to suggestions coming from the search history */
    private static final float HISTORY_SUGGEST_DEFAULT_WEIGHT = 0.5f;

    private IntercepterHelper intercepterHelper = new IntercepterHelper();

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
    @Setter private Suggester suggester;
    
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    
    @Autowired
    @Setter private ExecutionContextHolder executionContextHolder;
    
    @Resource(name="suggestViewSimple")
    private View simpleView;
    
    @Resource(name="suggestViewRich")
    private View richView;

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
        binder.registerCustomEditor(ProfileId.class, new ProfileEditor(DefaultValues.DEFAULT_PROFILE));
        binder.registerCustomEditor(JsonPCallbackParam.class, new GenericEditor(JsonPCallbackParam::new));
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
     * @param collection collection
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
    public ModelAndView suggestJava(@RequestParam("collection") Collection collection,
            @RequestParam(defaultValue=DefaultValues.DEFAULT_PROFILE) ProfileId profile,
            @RequestParam("partial_query") String partialQuery,
            @RequestParam(defaultValue="10") int show,
            @RequestParam(defaultValue="0") int sort,
            @RequestParam(value="fmt", defaultValue="json") String format,
            @RequestParam(defaultValue="0.5") double alpha,
            @RequestParam(required=false) String category,
            @RequestParam(required=false) JsonPCallbackParam callback,
            @ModelAttribute SearchUser user,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        if (collection != null) {
            ServiceConfigReadOnly serviceConfig = getServiceConfigOrDefault(configRepository, collection, Optional.of(profile.getId()));

            // Get organic/CSV suggestions from PADRE
            List<Suggestion> suggestions = suggester.suggest(collection, profile.getId(), partialQuery, show, Sort.valueOf(sort), alpha, category);

            // Augment them with search history if needed
            augmentSuggestionsWithHistory(suggestions, collection, serviceConfig, user, getSearchUrl(request, collection.getId(), profile.getId()));

            ModelAndView mav = new ModelAndView();
            mav.addObject("suggestions", suggestions);
            mav.addObject("callback", Optional.ofNullable(callback).map(c -> c.getCallback()).orElse(null));
            
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
            
            if (callback != null) {
                // JSONP request: Change the content type to application/javascript
                // required by JSONP (SUPPORT-2099)
                response.setContentType("application/javascript");
            }
            return mav;
        } else {
            // Collection not found
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    /**
     * Augment suggestions with the recent queries from the search history
     * @param suggestions List of suggestions to augment
     * @param serviceConfig ServiceConfig
     * @param user Current user from the session
     * @param searchUrl Base URL to generate a link to the previous searches
     */
    private void augmentSuggestionsWithHistory(List<Suggestion> suggestions, Collection c, ServiceConfigReadOnly serviceConfig,
        SearchUser user, String searchUrl) {

        if(serviceConfig.get(FrontEndKeys.ModernUi.Session.SESSION)
            && serviceConfig.get(FrontEndKeys.ModernUi.Session.SearchHistory.Suggest.SUGGEST)) {

            // Get search history
            List<SearchHistory> history = searchHistoryRepository.getSearchHistory(user, c,
                serviceConfig.get(FrontEndKeys.ModernUi.Session.SearchHistory.SIZE));
            
            for (SearchHistory h: history) {
                // Build a suggestion for each history
                Suggestion s = new Suggestion();
                s.setKey(h.getOriginalQuery());
                
                s.setAction(searchUrl + "?" + h.getSearchParams());
                s.setActionType(ActionType.URL);
                
                // Display is templated from a collection.cfg parameter
                s.setDisplay(serviceConfig.get(FrontEndKeys.ModernUi.Session.SearchHistory.Suggest.DISPLAY_TEMPLATE)
                    .replace(HISTORY_SUGGEST_QUERY, h.getOriginalQuery())
                    .replace(HISTORY_SUGGEST_TOTAL_MATCHING, Integer.toString(h.getTotalMatching())));
                s.setDisplayType(DisplayType.HTML);

                s.setCategory(serviceConfig.get(FrontEndKeys.ModernUi.Session.SearchHistory.Suggest.CATEGORY));
                s.setCategoryType("");
                
                s.setWeight(HISTORY_SUGGEST_DEFAULT_WEIGHT);
                
                suggestions.add(s);
            }
        }
    }
    
    /**
     * Get the base URL to perform a search
     * @param request HTTP Request to get the server information
     * @param collectionId CollectionId to get the search_link
     * @param profileId CollectionId to get the search_link
     * @return The search URL
     */
    @SneakyThrows(MalformedURLException.class)
    private String getSearchUrl(HttpServletRequest request, String collectionId, String profileId) {
        URL url = new URL(request.getRequestURL().toString());
        StringBuilder out = new StringBuilder();
        ServiceConfigReadOnly serviceConfig;
        try {
            serviceConfig = configRepository.getServiceConfig(collectionId, profileId);
            out.append(url.getProtocol()).append("://")
                .append(url.getAuthority())
                .append(executionContextHolder.getContextPath())
                .append("/")
                .append(serviceConfig.get(FrontEndKeys.ModernUi.SEARCH_LINK));
        } catch (ProfileNotFoundException e) {
            log.error("Couldn't find profile '" + profileId + "' in " + collectionId, e);
        }
        return out.toString();
    }
    
    @Override
    protected File getSearchHome() {
        return searchHome;
    }

}
