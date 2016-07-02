package com.funnelback.publicui.search.web.controllers.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

/**
 * Controller for maintenance operations on the session features such
 * as purging old data
 * 
 * @author nguillaumin@funnelback.com
 *
 */
@Controller
public class SessionMaintenanceController extends SessionApiControllerBase {

    @Autowired
    private ResultsCartRepository cartRepository;
    
    @Autowired
    private SearchHistoryRepository historyRepository;
    
    /**
     * Purge old session data
     * @param daysToKeep How many days of session data to keep
     * @param request HTTP request, needed for <code>@PreAuthorize</code> to access <code>#request</code>
     * @param response HTTP response to return the purging status as JSON
     * @throws IOException
     */
    @RequestMapping("/maintenance/purge-sessions.json")
    @PreAuthorize("T(java.net.InetAddress).getByName(#request.getRemoteAddr()).isLoopbackAddress()")
    public void purgeSessions(@RequestParam(required=true) int daysToKeep, HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        Map<String, Integer> map = new HashMap<>();
        map.put("cartRemoved", cartRepository.purgeCartResults(daysToKeep));
        map.put("historyRemoved", historyRepository.purgeHistory(daysToKeep));
        
        sendResponse(response, HttpServletResponse.SC_OK, map);
    }
    
}
