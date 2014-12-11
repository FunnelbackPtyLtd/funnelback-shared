package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.File;
import java.io.IOException;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.utils.cgirunner.CgiRunner;
import com.funnelback.common.utils.cgirunner.CgiRunnerException;
import com.funnelback.contentoptimiser.UrlStatus;
import com.funnelback.contentoptimiser.fetchers.UrlStatusFetcher;
import com.funnelback.contentoptimiser.utils.CgiRunnerFactory;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * This class obtains the "status" of the URL, as seen by the crawler and url-status.pl. This is used to determine
 * why we may not have the document (eg, the crawler never saw it, or exclude rules prevented this document being downloaded). 
 * 
 * @author tim
 *
 */
@Log4j
@Component
public class DefaultUrlStatusFetcher implements UrlStatusFetcher {

    @Autowired @Setter File searchHome;
    
    @Autowired @Setter ConfigRepository configRepository;
    
    @Autowired @Setter CgiRunnerFactory cgiRunnerFactory;

    @Override
    public UrlStatus fetch(String optimiserUrl, String collection) {

        UrlStatus status = null;
        if(! optimiserUrl.contains("://")) {
            optimiserUrl = "http://" + optimiserUrl; 
        }
        
        File perlBin = new File(configRepository.getExecutablePath(Keys.Executables.PERL));
        CgiRunner runner = cgiRunnerFactory.create(
                new File(searchHome, DefaultValues.FOLDER_WEB + File.separator +  DefaultValues.FOLDER_ADMIN  + File.separator + "url-status.cgi"),
                perlBin)
            .addRequestParameter("u", optimiserUrl)
            .addRequestParameter("c", collection)        
            .addRequestParameter("f", "json")
            .setTaint()
            .setEnvironmentVariable("REMOTE_USER", "admin")
            .setEnvironmentVariable("SEARCH_HOME", searchHome.getAbsolutePath());
        String json = "";
        try {
            json = runner.runToString();
        } catch (CgiRunnerException e1) {
            log.error("Unable to run the CGI command to get URL status ",e1);
            return null;
        }
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            // strip headers
            json = json.replaceAll("(?m)^[A-Z][^:]+:\\s.+$", "");
            status = mapper.readValue(json, UrlStatus.class);
        } catch (JsonParseException e) {
            log.error("Error obtaining info about URL",e);
        } catch (JsonMappingException e) {
            log.error("Error obtaining info about URL",e);
        } catch (IOException e) {
            log.error("Error obtaining info about URL",e);
        }
        
        return status;
    }

}

