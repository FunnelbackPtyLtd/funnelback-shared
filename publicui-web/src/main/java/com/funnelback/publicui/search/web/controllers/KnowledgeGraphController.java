package com.funnelback.publicui.search.web.controllers;

import com.funnelback.common.config.CollectionId;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.profile.ProfileId;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the basic knowledge-graph endpoints for presenting the
 * widget UI and for serving up the necessary translation data etc.
 */
@Controller
@RequestMapping("knowledge-graph")
public class KnowledgeGraphController {

    @RequestMapping({"/","/index.html"})
    public ModelAndView knowledgeGraphHtml(
            @Pattern(regexp = "[\\w-]+")
            @RequestParam(required = true)
            String collection,
            @Pattern(regexp = "[\\w-]+")
            @RequestParam(required = true)
            String profile,
            String targetUrl,
            HttpServletResponse response) throws IOException {

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("collectionId", collection);
        model.put("profileId", profile);
        model.put("targetUrl", targetUrl);
        model.put("GlobalResourcesPrefix", "/s/resources-global/");

        return new ModelAndView(DefaultValues.FOLDER_WEB+"/"
            +DefaultValues.FOLDER_TEMPLATES+"/"
            +DefaultValues.FOLDER_MODERNUI+"/knowledge-graph", model);
    }
}
