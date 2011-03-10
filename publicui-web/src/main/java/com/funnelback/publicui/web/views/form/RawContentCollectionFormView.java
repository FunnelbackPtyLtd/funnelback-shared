package com.funnelback.publicui.web.views.form;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RawContentCollectionFormView extends AbstractCollectionFormView {

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType(getContentType());
		response.getWriter().write(getTemplateContent());
	}

}
