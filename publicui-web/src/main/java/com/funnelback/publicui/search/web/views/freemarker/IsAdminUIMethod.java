package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.service.ConfigRepository;

import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateModelException;

@Log4j
public class IsAdminUIMethod extends AbstractTemplateMethod {

    public static final String NAME = "isAdminUI"; 
    
    @Autowired
    private ConfigRepository configRepository;
    
    public IsAdminUIMethod() {
        super(1, 0, false);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object execMethod(List arguments) throws TemplateModelException {
        HttpRequestHashModel model = (HttpRequestHashModel) arguments.get(0);
        if (model == null || model.getRequest() == null) {
            log.warn(HttpRequestHashModel.class.getSimpleName() + " is null or the contained request is null");
            return Boolean.FALSE;
        }

        int adminPort = configRepository.getGlobalConfiguration().valueAsInt(Keys.Urls.ADMIN_PORT, -1);
        
        if (adminPort == model.getRequest().getServerPort()) {
            return Boolean.TRUE;
        } else {         
            return Boolean.FALSE;
        }
    }

}
