package com.funnelback.publicui.web.views.form;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.Log;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.core.io.ClassPathResource;

import com.funnelback.publicui.web.controllers.SearchController;
import com.funnelback.publicui.web.views.form.js.CustomWrapFactory;

@Log
public class JavascriptCollectionFormView extends AbstractCollectionFormView {

	private Script jsonTemplateScript;
	private Script jsonTemplateExtensionsScript;

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Context ctx = ContextFactory.getGlobal().enterContext();
		ctx.setOptimizationLevel(-1);
		ctx.setLanguageVersion(Context.VERSION_1_7);
		ctx.setWrapFactory(new CustomWrapFactory());

		ClassPathResource scriptResource = new ClassPathResource("json-template.js");
		ClassPathResource scriptExtensionsResource = new ClassPathResource("json-template-extensions.js");

		
		/* TODO COMPILE SCRIPT ONE TIME
		 * and keep it somewhere
		InputStreamReader inScript = null;
		InputStreamReader inScriptExtensions = null;
		try {
			log.debug("Compiling JS script from '" + scriptResource + "'");
			inScript = new InputStreamReader(scriptResource.getInputStream());
			jsonTemplateScript = ctx.compileReader(inScript,
			scriptResource.getFilename(), 1, null);

			log.debug("Compiling JS script extensions from '" + scriptExtensionsResource + "'");
			inScriptExtensions = new InputStreamReader(scriptExtensionsResource.getInputStream());
			jsonTemplateExtensionsScript = ctx.compileReader(inScriptExtensions,
			scriptExtensionsResource.getFilename(), 1, null);

		} finally {
			Context.exit();
			IOUtils.closeQuietly(inScript);
			IOUtils.closeQuietly(inScriptExtensions);
		}
		*/

		ScriptableObject scope = ctx.initStandardObjects();

		String printFunction = "function print(message) {java.lang.System.out.println(message);}";
		ctx.evaluateString(scope, printFunction, "print", 1, null);

		// Load json-template.js
		// TODO use precompiled script: ctx.executeScriptWithContinuations(jsonTemplateScript, scope);
		ctx.evaluateString(scope, FileUtils.readFileToString(scriptResource.getFile()), "json-template", 1, null);

		// Load json-template-extensions.js
		// TODO use precompiled script: ctx.executeScriptWithContinuations(jsonTemplateExtensionsScript,scope);
		ctx.evaluateString(scope, FileUtils.readFileToString(scriptExtensionsResource.getFile()), "json-template-extension", 1, null);

		// Push template
		ScriptableObject.putProperty(scope, "tpl", Context.javaToJS(templateContent, scope));

		// Push model
		/*
		ScriptableObject.putProperty(scope, "data",
				Context.javaToJS(model.get(SearchController.MODEL_KEY_SEARCH_TRANSACTION), scope));
		*/
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
		ctx.evaluateString(scope, "var data = " + mapper.writeValueAsString(model.get(SearchController.MODEL_KEY_SEARCH_TRANSACTION)) + ";", "pushData", 1, null);

		// Augment model
		ctx.evaluateString(scope, "augment_model(data)", "augmentModel", 1, null);
		
		// Render
		String rendered = (String) Context
				.jsToJava(
						ctx.evaluateString(
								scope,
								"jsontemplate.Template(tpl, {more_formatters: more_formatters, more_predicates: more_predicates}).expand(data);",
								"render", 1, null), String.class);

		response.setContentType("text/html");
		response.getWriter().write(rendered);

		Context.exit();
	}

}
