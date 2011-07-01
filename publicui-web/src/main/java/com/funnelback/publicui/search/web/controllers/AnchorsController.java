package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.anchors.AnchorDescription;
import com.funnelback.publicui.search.model.anchors.AnchorDetail;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.anchors.AnchorsFetcher;
import com.funnelback.publicui.search.service.anchors.DefaultAnchorsFetcher;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.search.web.controllers.SearchController.ModelAttributes;


@Controller
public class AnchorsController {
	
	@Autowired
	ConfigRepository configRepository;
	
	@InitBinder
	public void initBinder(DataBinder binder) {
		binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
	}
	
	@Autowired
	AnchorsFetcher fetcher;
	
	@RequestMapping(value="/anchors.html",params={RequestParameters.COLLECTION,"docnum","!anchortext"})
	public ModelAndView anchors(HttpServletResponse response,
			@RequestParam(RequestParameters.COLLECTION) Collection collection, String docnum) throws IOException {
		
		AnchorModel anchors = fetcher.fetchGeneral(Integer.parseInt(docnum),collection);
		
		Map<String,Object> model = new HashMap<String,Object>(); 
		model.put("anchors", anchors);
		return new ModelAndView(
				DefaultValues.FOLDER_WEB+"/"
				+DefaultValues.FOLDER_TEMPLATES+"/"
				+DefaultValues.FOLDER_PUBLICUI+"/anchors/anchors",model);
	}

	@RequestMapping(value="/anchors.html",params={RequestParameters.COLLECTION,"docnum","anchortext"})
	public ModelAndView anchorsDetail(HttpServletResponse response,
			@RequestParam(RequestParameters.COLLECTION) Collection collection,
			String docnum, 
			String anchortext,
			String start) throws IOException {
		
		int startInt = 0;
		if(start != null){ 
			startInt = Integer.parseInt(start);
		}
		AnchorModel anchors = fetcher.fetchDetail(Integer.parseInt(docnum),collection,anchortext,startInt);
		
		Map<String,Object> model = new HashMap<String,Object>(); 
		model.put("anchors", anchors);
		model.put("max_urls_per_page",AnchorDetail.MAX_URLS_PER_PAGE);
		return new ModelAndView(
				DefaultValues.FOLDER_WEB+"/"
				+DefaultValues.FOLDER_TEMPLATES+"/"
				+DefaultValues.FOLDER_PUBLICUI+"/anchors/anchors-detail",model);
	}
	
	
	
	/**
	 * Called when no collection has been specified.
	 * @return a list of all available collections.
	 */
	@RequestMapping(value="/anchors.html",params="!"+RequestParameters.COLLECTION)
	public ModelAndView noCollection() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(ModelAttributes.AllCollections.toString(), configRepository.getAllCollections());

		return new ModelAndView(
				DefaultValues.FOLDER_WEB+"/"
				+DefaultValues.FOLDER_TEMPLATES+"/"
				+DefaultValues.FOLDER_PUBLICUI+"/no-collection", model);
	}
	
}
