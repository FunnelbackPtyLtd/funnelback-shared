package com.funnelback.publicui.web.views.form;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.Log;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;

import com.funnelback.publicui.web.controllers.SearchController;
import com.funnelback.publicui.web.views.form.js.CustomWrapFactory;
import com.funnelback.publicui.web.views.form.soy.FunnelbackFunctionsModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.SoyModule;
import com.google.template.soy.jssrc.SoyJsSrcOptions;

@Log
public class SoyCollectionFormView extends AbstractCollectionFormView {

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		Injector injector = Guice.createInjector(new FunnelbackFunctionsModule(), new SoyModule());
		SoyFileSet.Builder builder = injector.getInstance(SoyFileSet.Builder.class);
		
		SoyFileSet sfs = builder.add(getTemplateContent(), "Soy template for collection '" + getCollectionId() + "'").build();		
		List<String> scripts = sfs.compileToJsSrc(new SoyJsSrcOptions(), null);
		
		sfs = builder.add(new ClassPathResource("funnelback.soy").getURL(), "Funnelback Soy library").build();
		scripts.addAll(sfs.compileToJsSrc(new SoyJsSrcOptions(), null));
		
		// Map<String, Object> data = new HashMap<String, Object>();
		// data.put(SearchController.MODEL_KEY_SEARCH_TRANSACTION, model.get(SearchController.MODEL_KEY_SEARCH_TRANSACTION));
		
		Context ctx = ContextFactory.getGlobal().enterContext();
		ctx.setOptimizationLevel(-1);
		ctx.setLanguageVersion(Context.VERSION_1_7);
		ctx.setWrapFactory(new CustomWrapFactory());
		ScriptableObject scope = ctx.initStandardObjects();
		
		String printFunction = "function print(message) {java.lang.System.out.println(message);}";
		ctx.evaluateString(scope, printFunction, "print", 1, null);

		ctx.evaluateString(scope, "var navigator = {'userAgent': 'MSIE'};", "fake-ua", 1, null);
		
		for (int i=0; i<scripts.size(); i++) {
			ctx.evaluateString(scope, scripts.get(i), "soy-script-"+i, 1, null);
		}

		ctx.evaluateString(scope, IOUtils.toString(getClass().getResourceAsStream("/soyutils.js")), "soy-utils", 1, null);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
		ctx.evaluateString(scope, "var data = " + mapper.writeValueAsString(model.get(SearchController.MODEL_KEY_SEARCH_TRANSACTION)) + ";", "pushData", 1, null);

		
		String rendered = (String) Context
		.jsToJava(
				ctx.evaluateString(
						scope,
						"collection.search(data);",
						"render", 1, null), String.class);

		response.setContentType("text/html");
		response.getWriter().write(rendered);
		

	}

}
