package com.funnelback.publicui.search.web.controllers.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DataBindingException;

import lombok.extern.log4j.Log4j2;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for session related controllers
 * 
 * @since 13.0
 */
@Log4j2
public abstract class SessionApiControllerBase extends SessionController {

    /** Key used in the JSON output for the status message */
    protected final static String STATUS = "status";
    /** Key used in the JSON output for the error message */
    protected final static String ERROR_MESSAGE = "error-message";
    
    /** Value for the OK status in the JSON output */
    protected final static String OK = "ok";
    /** Value for the KO status in the JSON output */
    protected final static String KO = "ko";
    
    /** Predefined success Map for the JSON output */
    protected final static Map<String, String> OK_STATUS_MAP = new HashMap<>();
    static { OK_STATUS_MAP.put(STATUS, OK); }
    
    /** Predefined error Map for the JSON output */
    protected final static Map<String, String> KO_STATUS_MAP = new HashMap<>();
    static { KO_STATUS_MAP.put(STATUS, KO); }
    
    private ObjectMapper jsonMapper = new ObjectMapper();
    
    protected void sendResponse(HttpServletResponse response, int status, Object json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        if (json != null) {
            jsonMapper.writeValue(response.getOutputStream(), json);
        }
    }
    
    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    private void daeExceptionHandler(Exception e, HttpServletResponse response) throws IOException {
        log.error("Error while accessing session data", e);
        sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Error while accessing session data: " + e.getMessage());
    }
    
    @ExceptionHandler(value={DataBindingException.class, BindException.class})
    private void dbeExceptionHandler(Exception e, HttpServletResponse response) throws IOException {
        log.error("Data binding error", e);
        sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Bad request: " + e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    private void exceptionHandler(Exception e, HttpServletResponse response) throws IOException {
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
