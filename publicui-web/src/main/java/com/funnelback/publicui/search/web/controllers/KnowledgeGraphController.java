package com.funnelback.publicui.search.web.controllers;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.profile.ProfileAndView;
import com.funnelback.publicui.knowledgegraph.exception.InvalidInputException;
import com.funnelback.publicui.knowledgegraph.model.KnowledgeGraphLabels;
import com.funnelback.publicui.knowledgegraph.model.KnowledgeGraphTemplate;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.SampleCollectionUrlService;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.utils.web.ProfilePicker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

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
            HttpServletRequest request,
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

        ProfileAndView profileAndView = ProfileAndView.fromFolder(new ProfilePicker().existingProfileForCollection(collection, profileId));

        if (targetUrl == null) {
            // We select some URL from the collection.
            try {
                targetUrl = sampleCollectionUrlService.getSampleUrl(collection, profileAndView);
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
        model.put("profileId", profileAndView.asFolderName());
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
            return prepareJsonModelAndViewForErrorMessage(response, e);
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
            return prepareJsonModelAndViewForErrorMessage(response, e);
        }
    }

    private ModelAndView prepareJsonModelAndViewForSingleObject(Object object) {
        MappingJackson2JsonView view = new MappingJackson2JsonView();
        view.setExtractValueFromSingleKeyModel(true);
        ModelAndView mav = new ModelAndView(view);
        mav.addObject(object);
        return mav;
    }

    private ModelAndView prepareJsonModelAndViewForErrorMessage(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(404);
        Map<String, String> model = new HashMap<>();
        if (e instanceof FileNotFoundException) {
            model.put("message", "No config file available.");
        } else {
            model.put("message", e.getMessage());
        }

        return prepareJsonModelAndViewForSingleObject(model);
    }
}
