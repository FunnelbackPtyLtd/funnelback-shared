package com.funnelback.publicui.search.web.controllers;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.knowledgegraph.labels.GenericKnowledgeGraphLabelsMarshaller;
import com.funnelback.common.knowledgegraph.labels.KnowledgeGraphLabel;
import com.funnelback.common.knowledgegraph.templates.GenericKnowledgeGraphTemplatesMarshaller;
import com.funnelback.common.knowledgegraph.templates.KnowledgeGraphTemplate;
import com.funnelback.common.profile.ProfileAndView;
import com.funnelback.common.utils.MissingDateSupplier;
import com.funnelback.publicui.knowledgegraph.exception.InvalidInputException;
import com.funnelback.publicui.knowledgegraph.model.KnowledgeGraphLabelsModel;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.SampleCollectionUrlService;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.utils.web.ProfilePicker;

import org.apache.commons.io.FileUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private String NAMES_METADATA_CLASS = "FUNkgNodeNames";
    private String LABEL_METADATA_CLASS = "FUNkgNodeLabel";
    /**
     * This query finds all documents which satisfy the most basic criteria for being nodes in
     * the knowledge graph (i.e. have a label and a name). It's possible that knowledge graph
     * will later reject them for another reason (in which case you'll be sent to a non-existant
     * node), but this should suffice for 99+% of cases.
     */
    private String KG_NODES_QUERY = "|" + NAMES_METADATA_CLASS + ":$++ |" + LABEL_METADATA_CLASS + ":$++";

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
                targetUrl = sampleCollectionUrlService.getSampleUrl(collection, profileAndView, KG_NODES_QUERY);
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
            DefaultValues.FOLDER_CONF + "/" + collection + "/" + profile + "/" + com.funnelback.common.config.Files.KG_TEMPLATES);

        try (FileInputStream fis = new FileInputStream(jsonTemplatesFile)) {
            GenericKnowledgeGraphTemplatesMarshaller marshaller = new GenericKnowledgeGraphTemplatesMarshaller();
            List<KnowledgeGraphTemplate> templates = marshaller.unMarshal(
                Optional.of(FileUtils.readFileToByteArray(jsonTemplatesFile)),
                MissingDateSupplier.lastModifiedDate(jsonTemplatesFile));
            Map<String, KnowledgeGraphTemplate> results = templates
                .stream()
                .collect(Collectors.toMap(KnowledgeGraphTemplate::getType, Function.identity()));
            return prepareJsonModelAndViewForSingleObject(results);
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
            DefaultValues.FOLDER_CONF + "/" + collection + "/" + profile + "/" + com.funnelback.common.config.Files.KG_LABELS);

        try {
            GenericKnowledgeGraphLabelsMarshaller marshaller = new GenericKnowledgeGraphLabelsMarshaller();
            List<KnowledgeGraphLabel> results = marshaller.unMarshal(
                Optional.of(FileUtils.readFileToByteArray(jsonLabelsFile)),
                MissingDateSupplier.lastModifiedDate(jsonLabelsFile));
            return prepareJsonModelAndViewForSingleObject(KnowledgeGraphLabelsModel.fromConfigFile(results));
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
