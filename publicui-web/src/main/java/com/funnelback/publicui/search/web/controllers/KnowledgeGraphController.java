package com.funnelback.publicui.search.web.controllers;

import com.funnelback.common.config.CollectionId;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.profile.ProfileId;
import com.funnelback.publicui.knowledgegraph.exception.InvalidInputException;
import com.funnelback.publicui.knowledgegraph.model.KnowledgeGraphLabels;
import com.funnelback.publicui.knowledgegraph.model.KnowledgeGraphTemplate;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.SampleCollectionUrlService;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.search.web.binding.StringArrayFirstSlotEditor;
import com.funnelback.publicui.utils.web.ProfilePicker;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.funnelback.publicui.search.web.controllers.SearchController.GLOBAL_RESOURCES_LOCATION;

/**
 * Provides the basic knowledge-graph endpoints for presenting the
 * widget UI and for serving up the necessary translation data etc.
 */
@Controller
@RequestMapping("knowledge-graph")
public class KnowledgeGraphController {

    @Autowired
    private SampleCollectionUrlService sampleCollectionUrlService;

    @Autowired
    private ConfigRepository configRepository;

    /**
     * <p>Configures the binder to:</p>
     * <ul>
     *     <li>Convert a collection ID into a proper collection object</li>
     * </ul>
     * @param binder
     * @see "http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/mvc.html#mvc-ann-webdatabinder"
     */
    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
    }

    @RequestMapping({"/","/index.html"})
    public ModelAndView knowledgeGraphHtml(
            @RequestParam(required = true)
            Collection collection,
            @Pattern(regexp = "[\\w-]+")
            @RequestParam(name = "profile", required = true)
            String profileId,
            String targetUrl,
            HttpServletResponse response) throws IOException {

        if (collection == null) {
            // Seems to happen even with required=true when the collection is one that does not exist
            response.setContentType("text/plain");
            response.setStatus(404);
            try (Writer writer = response.getWriter()) {
                writer.append("Invalid collection");
            }
            return null;
        }

        ProfileId profile = new ProfileId(new ProfilePicker().existingProfileForCollection(collection, profileId));

        if (targetUrl == null) {
            // We select some URL from the collection.
            try {
                targetUrl = sampleCollectionUrlService.getSampleUrl(collection, profile);
            } catch (SampleCollectionUrlService.CouldNotFindAnyUrlException e) {
                response.setContentType("text/plain");
                response.setStatus(404);
                try (Writer writer = response.getWriter()) {
                    writer.append(e.getMessage());
                }
                return null;
            }
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("collectionId", collection.getId());
        model.put("profileId", profile.getId());
        model.put("targetUrl", targetUrl);

        return new ModelAndView(DefaultValues.FOLDER_WEB+"/"
            +DefaultValues.FOLDER_TEMPLATES+"/"
            +DefaultValues.FOLDER_MODERNUI+"/knowledge-graph", model);
    }

    @Autowired
    private File searchHome;

    @RequestMapping("/templates.json")
    public ModelAndView knowledgeGraphTemplates(
        @Pattern(regexp = "[\\w-]+")
        @RequestParam(required = true)
        String collection,
        @Pattern(regexp = "[\\w-]+")
        @RequestParam(required = true)
        String profile,
        HttpServletResponse response) throws IOException {

        File jsonTemplatesFile = new File(searchHome,
            DefaultValues.FOLDER_CONF + "/" + collection + "/" + profile + "/" + com.funnelback.common.config.Files.FKG_TEMPLATES);

        try (FileInputStream fis = new FileInputStream(jsonTemplatesFile)) {
            Map<String, KnowledgeGraphTemplate> result = KnowledgeGraphTemplate.fromConfigFile(fis);
            return prepareJsonModelAndViewForSingleObject(result);
        } catch (InvalidInputException | FileNotFoundException e) {
            returnErrorPage(response, e);
            return null;
        }
    }

    @RequestMapping("/labels.json")
    public ModelAndView knowledgeGraphLabels(
        @Pattern(regexp = "[\\w-]+")
        String collection,
        @Pattern(regexp = "[\\w-]+")
        String profile,
        HttpServletResponse response) throws IOException {

        File jsonLabelsFile = new File(searchHome,
            DefaultValues.FOLDER_CONF + "/" + collection + "/" + profile + "/" + com.funnelback.common.config.Files.FKG_LABELS);

        try (FileInputStream fis = new FileInputStream(jsonLabelsFile)) {
            KnowledgeGraphLabels result = KnowledgeGraphLabels.fromConfigFile(fis);
            return prepareJsonModelAndViewForSingleObject(result);
        } catch (InvalidInputException | FileNotFoundException e) {
            returnErrorPage(response, e);
            return null;
        }
    }

    private ModelAndView prepareJsonModelAndViewForSingleObject(Object object) {
        MappingJackson2JsonView view = new MappingJackson2JsonView();
        view.setExtractValueFromSingleKeyModel(true);
        ModelAndView mav = new ModelAndView(view);
        mav.addObject(object);
        return mav;
    }

    private void returnErrorPage(HttpServletResponse response, Exception e) throws IOException {
        response.setContentType("text/plain");
        response.setStatus(404);
        try (Writer writer = response.getWriter()) {
            if (e instanceof FileNotFoundException) {
                writer.append("No config file available.");
            } else {
                writer.append(e.getMessage());
            }
        }
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
