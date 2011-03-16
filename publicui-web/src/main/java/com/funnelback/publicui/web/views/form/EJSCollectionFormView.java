package com.funnelback.publicui.web.views.form;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.funnelback.publicui.web.controllers.SearchController;

public class EJSCollectionFormView extends AbstractCollectionFormView {
	
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		Context ctx = ContextFactory.getGlobal().enterContext();
		ctx.setOptimizationLevel(-1);
		ctx.setLanguageVersion(Context.VERSION_1_5);
		
		Global global = new Global();
		global.init(ctx);
		
		ScriptableObject scope = (ScriptableObject) ctx.initStandardObjects(global);

		ctx.evaluateReader(scope,
				new InputStreamReader(new ClassPathResource("ejs_production.js").getInputStream()), "ejs_production", 1, null);

		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
		ctx.evaluateString(scope, "var data = " + mapper.writeValueAsString(model.get(SearchController.MODEL_KEY_SEARCH_TRANSACTION)) + ";", "pushData", 1, null);

		// Push template
		ScriptableObject.putProperty(scope, "tpl", Context.javaToJS(getTemplateContent(), scope));		
		
		String rendered = (String) Context
		.jsToJava(
				ctx.evaluateString(
						scope,
						"new EJS({text: tpl}).render(data);",
						"render", 1, null), String.class);

		response.setContentType("text/html");
		response.getWriter().write(rendered);

		Context.exit();

	}

}
