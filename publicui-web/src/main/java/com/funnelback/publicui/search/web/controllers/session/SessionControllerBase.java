package com.funnelback.publicui.search.web.controllers.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Base class for session related controllers
 * 
 * @since 12.4
 */
@Log4j
public class SessionControllerBase {

    protected final static String STATUS = "status";
    protected final static String ERROR_MESSAGE = "error-message";
    protected final static String OK = "ok";
    protected final static String KO = "ko";
    
    protected final static Map<String, String> OK_STATUS_MAP = new HashMap<>();
    static { OK_STATUS_MAP.put(STATUS, OK); }
    
    private ObjectMapper jsonMapper = new ObjectMapper();
    
    protected void sendResponse(HttpServletResponse response, int status, Object json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        if (json != null) {
            jsonMapper.writeValue(response.getOutputStream(), json);
        }
    }
    
    @ExceptionHandler(DataAccessException.class)
    public void daeExceptionHandler(DataAccessException dae, HttpServletResponse response) throws IOException {
        log.error("Error while accessing session data", dae);
        sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getJsonErrorMap(dae.toString()));
    }
    
    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception e, HttpServletResponse response) throws IOException {
        log.error("Unknown session error", e);
        sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getJsonErrorMap(e.toString()));
    }
    
    protected Map<String, String> getJsonErrorMap(String errorMessage) {
        Map<String, String> json = new HashMap<>();
        json.put(STATUS, KO);
        json.put(ERROR_MESSAGE, errorMessage);
        
        return json;
    }
    
}
