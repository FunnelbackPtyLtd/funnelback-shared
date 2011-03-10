package com.funnelback.publicui.web.views.form;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.support.ServletContextResource;

import com.funnelback.publicui.web.controllers.SearchController;

@Log
public class JavascriptCollectionFormView extends AbstractCollectionFormView {

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		log.debug("10");
		Context ctx = ContextFactory.getGlobal().enterContext();
		ctx.setOptimizationLevel(-1);
		ctx.setLanguageVersion(Context.VERSION_1_7);
		
		ScriptableObject scope = ctx.initStandardObjects();
		
		String envJs = IOUtils.toString(new ClassPathResource("env.rhino.1.2.js").getInputStream());
		
		String printFunction = "function print(message) {java.lang.System.out.println(message);}";
		ctx.evaluateString(scope, printFunction, "print", 1, null);
		// ctx.evaluateString(scope, envJs, "env.rhino.js", 1, null);
		
		// Load libraries
/*		ctx.evaluateString(scope,
				FileUtils.readFileToString(new File("C:/Data/dev/eclipse-workspace/publicui-parent/publicui-web/src/main/webapp/js/jquery/jquery-1.5.1.js")),
				"jquery", 1, null);
*/
		log.debug("30");
		ctx.evaluateString(scope,
				FileUtils.readFileToString(new File("C:/Data/dev/eclipse-workspace/publicui-parent/publicui-web/src/main/webapp/js/json-template/json-template.js")),
				"json-tmpl", 1, null);

		String template = FileUtils.readFileToString(new File("C:/Data/dev/funnelback/conf/shakespeare-lear/_default/web/template.txt"));
		
		log.debug("40");
		template = template.replace("\r", "");
		template = template.replace("\n", "\\n");
		ctx.evaluateString(scope, "var tplStr = '" + template + "';", "setTplStr", 1, null);
		ctx.evaluateString(scope, "var tpl = jsontemplate.Template(tplStr);", "setTpl", 1, null);
		// ctx.evaluateString(scope, "var tpl = jsontemplate.Template('{.section SearchTransaction}{.section response}b{.section resultPacket}There is a result packet: {details.collectionSize}{.end}{.end}{.end}');", "setTpl", 1, null);
		
		log.debug("50");
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
		String dataStr = "var data = {SearchTransaction: " + mapper.writeValueAsString(model.get(SearchController.MODEL_KEY_SEARCH_TRANSACTION)) + "};";
		log.debug("60");
		ctx.evaluateString(scope, dataStr, "setData", 1, null);

		log.debug("BEFORE EXPAND");
		String content = (String) ctx.evaluateString(scope, "tpl.expand(data);", "renderTpl", 1, null);
		log.debug("AFTER EXPAND");
		
		response.setContentType("text/html");
		response.getWriter().write(content);
		log.debug("DONE");

	}

}
