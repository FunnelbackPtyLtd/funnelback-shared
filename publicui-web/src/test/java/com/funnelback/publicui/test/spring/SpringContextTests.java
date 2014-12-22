package com.funnelback.publicui.test.spring;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.Fail;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.ProfileValueUtils;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.statements.RunAfterTestClassCallbacks;
import org.springframework.test.context.junit4.statements.RunAfterTestMethodCallbacks;
import org.springframework.test.context.junit4.statements.RunBeforeTestClassCallbacks;
import org.springframework.test.context.junit4.statements.RunBeforeTestMethodCallbacks;
import org.springframework.test.context.junit4.statements.SpringFailOnTimeout;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.ReflectionUtils;

/**
 * A test intended to ensure that the public UI's spring context (the real one, not the test one)
 * can be loaded (i.e. it doesn't reference any missing classes etc).
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
    "file:src/main/webapp/WEB-INF/applicationContext.xml",
    "file:src/main/webapp/WEB-INF/publicui-servlet.xml"
})
public class SpringContextTests {
    // Note - This test expects funnelback.installdir to be set (because the applicationContext.xml spring config does).
    // Without it it will fail with a NullPointer while trying to construct a file (for searchHome).
    
    /** Mock request provided by the WebAppConfiguration annotation */
    @Autowired
    private MockHttpServletRequest request;

    /** Must set execution context (normally done in the jetty context) */
    @Before
    public void before() {
        request.setAttribute("ExecutionContext", "Public");
    }
    
    /**
     * Test doesn't actually need to do anything (will fail before this is
     * called if the context doesn't load) but a test method must be present for
     * JUnit.
     */
    @Test
    public void testThatContextLoadedSuccessfully() {
    }
    
    
}
