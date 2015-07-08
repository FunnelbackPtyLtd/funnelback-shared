package com.funnelback.publicui.search.web.controllers.session;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.search.web.binding.CollectionEditor;

/**
 * Controller for the user search and click history
 * 
 * @since 13.0
 */
@Controller
public class SearchHistoryController extends SessionApiControllerBase {
    
    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private SearchHistoryRepository historyRepository;

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
    }

    /**
     * Clear the search history of a user
     * 
     * @param collectionId Collection to clear history for
     * @param user User to clear history for
     * @param response HTTP response
     * @throws IOException 
     */
    @RequestMapping(value="/search-history.json", method=RequestMethod.DELETE)
    public void searchHistoryClear(
            @RequestParam("collection") Collection collection,
            @ModelAttribute SearchUser user,
            HttpServletResponse response) throws IOException {

        if (collection != null && user != null) {
            historyRepository.clearSearchHistory(user, collection);
            sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
        } else {
            sendResponse(response, HttpServletResponse.SC_NOT_FOUND, KO_STATUS_MAP);
        }
    }
    
    /**
     * Clear the click history of a user
     * @param collectionId Collection to clear history for
     * @param user User to clear history for
     * @param response HTTP response
     * @throws IOException 
     */
    @RequestMapping(value="/click-history.json", method=RequestMethod.DELETE)
    public void clickHistoryClear(
            @RequestParam("collection") Collection collection,
            @ModelAttribute SearchUser user,
            HttpServletResponse response) throws IOException {

        if (collection != null && user != null) {
            historyRepository.clearClickHistory(user, collection);
            sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
        } else {
            sendResponse(response, HttpServletResponse.SC_NOT_FOUND, KO_STATUS_MAP);
        }
    }
    

}
