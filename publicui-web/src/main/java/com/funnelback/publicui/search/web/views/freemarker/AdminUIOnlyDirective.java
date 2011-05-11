package com.funnelback.publicui.search.web.views.freemarker;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.service.ConfigRepository;

import freemarker.core.Environment;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class AdminUIOnlyDirective implements TemplateDirectiveModel {

	public static final String NAME = "AdminUIOnly"; 
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
			throws TemplateException, IOException {
		
		HttpRequestHashModel requestModel = (HttpRequestHashModel) env.getGlobalVariable("Request");
		
		int adminPort = configRepository.getGlobalConfiguration().valueAsInt(Keys.Urls.ADMIN_PORT, -1);
		
		if (adminPort == requestModel.getRequest().getServerPort()) {
			body.render(env.getOut());
		}		
	}

}
