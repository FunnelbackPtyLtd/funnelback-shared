package com.funnelback.publicui.search.web.controllers;

import com.funnelback.common.config.CollectionId;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.profile.ProfileId;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.funnelback.publicui.search.web.controllers.SearchController.GLOBAL_RESOURCES_LOCATION;

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
            String targetUrl) throws IOException {

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("collectionId", collection);
        model.put("profileId", profile);
        model.put("targetUrl", targetUrl);

        return new ModelAndView(DefaultValues.FOLDER_WEB+"/"
            +DefaultValues.FOLDER_TEMPLATES+"/"
            +DefaultValues.FOLDER_MODERNUI+"/knowledge-graph", model);
    }

    @Autowired
    private File searchHome;

    @RequestMapping("/templates.json")
    public void knowledgeGraphTemplates(
        @Pattern(regexp = "[\\w-]+")
        @RequestParam(required = true)
        String collection,
        @Pattern(regexp = "[\\w-]+")
        @RequestParam(required = true)
        String profile,
        HttpServletResponse response) throws IOException {

        File jsonTemplatesFile = new File(searchHome, DefaultValues.FOLDER_CONF + "/" + collection + "/" + profile + "/" + "fkg-templates.json");

        serveJsonFile(response, jsonTemplatesFile);
    }

    @RequestMapping("/labels.json")
    public void knowledgeGraphLabels(
        @Pattern(regexp = "[\\w-]+")
        String collection,
        @Pattern(regexp = "[\\w-]+")
        String profile,
        HttpServletResponse response) throws IOException {

        File jsonLabelsFile = new File(searchHome, DefaultValues.FOLDER_CONF + "/" + collection + "/" + profile + "/" + "fkg-labels.json");

        serveJsonFile(response, jsonLabelsFile);
    }

    private void serveJsonFile(HttpServletResponse response, File file) throws IOException {
        if (file.exists()) {
            response.setContentType("application/json");

            try (OutputStream os = response.getOutputStream()) {
                Files.copy(file.toPath(), os);
            }
        } else {
            response.setContentType("text/plain");
            response.setStatus(404);
            try (Writer writer = response.getWriter()) {
                writer.append("No config file available.");
            }
        }
    }
}
