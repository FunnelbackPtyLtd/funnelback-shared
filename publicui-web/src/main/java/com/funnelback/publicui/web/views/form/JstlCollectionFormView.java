package com.funnelback.publicui.web.views.form;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.Log;

import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.view.JstlView;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.web.controllers.SearchController;

@Log
public class JstlCollectionFormView extends JstlView {

	@Override
	protected String prepareForRendering(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		SearchTransaction st = (SearchTransaction) request.getAttribute(SearchController.MODEL_KEY_SEARCH_TRANSACTION);
		
		if (st != null && SearchTransactionUtils.hasCollection(st)) {
			String path = "forms" + File.separator + st.getQuestion().getCollection().getId()
				+ ((st.getQuestion().getProfile() != null) ? "_"+st.getQuestion().getProfile() : "")
				+ ".jsp";
				
			String sourcePath = st.getQuestion().getCollection().getConfiguration().getConfigDirectory().getAbsolutePath();
			if (st.getQuestion().getProfile() != null) {
				sourcePath += File.separator + st.getQuestion().getProfile();
			}
			
			sourcePath += File.separator + "simple.form2";
			
			File target = new ServletContextResource(getServletContext(), path).getFile();
			log.debug("Target file:" + target.getAbsolutePath());
			org.apache.commons.io.FileUtils.copyFile(new File(sourcePath), target);			
						
		}
	
		return super.prepareForRendering(request, response);
	}

}
