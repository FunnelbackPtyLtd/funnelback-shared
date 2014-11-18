package com.funnelback.publicui.test.search.web.views.freemarker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.service.auth.AuthTokenManager;
import com.funnelback.publicui.search.service.auth.DefaultAuthTokenManager;
import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.AuthTokenMethod;
import com.funnelback.publicui.test.mock.MockConfigRepository;

import freemarker.template.TemplateModelException;

public class AuthTokenMethodTest extends AbstractMethodTest {

    private AuthTokenManager authTokenManager;
    
    private MockConfigRepository configRepository;
    
    @Before
    @Override
    public void before() {
        configRepository = new MockConfigRepository();
        configRepository.setGlobalConfiguration(new NoOptionsConfig("dummy")
            .setValue(Keys.SERVER_SECRET, "server-secret"));
        
        authTokenManager = new DefaultAuthTokenManager();

        super.before();
    }
    
    @Override
    protected AbstractTemplateMethod buildMethod() {
        AuthTokenMethod method = new AuthTokenMethod();
        method.setConfigRepository(configRepository);
        method.setAuthTokenManager(authTokenManager);
        
        return method;
    }

    @Override
    protected int getRequiredArgumentsCount() {
        return 1;
    }

    @Override
    protected int getOptionalArgumentsCount() {
        return 0;
    }
    
    @Test
    public void test() throws TemplateModelException {
        String actual = (String) method.exec(buildStringArguments("test-data"));
        
        String expected = authTokenManager.getToken("test-data", "server-secret");
        
        Assert.assertNotNull(actual);
        Assert.assertNotSame("", actual);
        
        Assert.assertEquals(expected, actual);
    }

}
