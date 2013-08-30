package com.funnelback.publicui.search.web.controllers.session;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

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
            @RequestParam("collection") String collectionId,
            @ModelAttribute SearchUser user,
            HttpServletResponse response) throws IOException {

        Collection c = configRepository.getCollection(collectionId);
        if (c != null && user != null) {
            historyRepository.clearSearchHistory(user, c);
            sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, KO_STATUS_MAP);
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
            @RequestParam("collection") String collectionId,
            @ModelAttribute SearchUser user,
            HttpServletResponse response) throws IOException {

        Collection c = configRepository.getCollection(collectionId);
        if (c != null && user != null) {
            historyRepository.clearClickHistory(user, c);
            sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, KO_STATUS_MAP);
        }
    }
    

}
