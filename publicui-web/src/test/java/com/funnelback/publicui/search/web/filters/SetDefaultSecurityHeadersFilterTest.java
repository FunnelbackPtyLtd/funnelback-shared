package com.funnelback.publicui.search.web.filters;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SetDefaultSecurityHeadersFilterTest {

    /**
     * This test is intended to check what default headers spring-security is setting
     * in our current version so that we learn about any new ones they think are
     * good defaults (so we can consider using them ourselves).
     *
     * We do this because we need to turn them all off by default and reintroduce them
     * ourselves as some can't be overridden per-request otherwise (see RNDSUPPORT-3048).
     */
    @Test
    public void test() throws Exception {
        Map<String, String> springDefaultHeaders = getTheDefaultHeadersSpringSecuritySets();
        Map<String, String> funnelbackHeaders = getTheHeadersFunnelbackSets();

        Assert.assertEquals("Spring Security is not sending the response headers we expected - maybe an upgrade changed them?"
                + " Consider whether publicui ought to be returning any new headers (or removing old ones), and if so add them to "
                + SetDefaultSecurityHeadersFilter.class.getName() + " and PublicUIHeadersIT before updating this test's expected list.",
            funnelbackHeaders, springDefaultHeaders);
    }

    private Map<String, String> getTheHeadersFunnelbackSets() throws IOException, ServletException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new SetDefaultSecurityHeadersFilter().doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        Map<String, String> funnelbackHeaders = new HashMap<>();
        for (String name : response.getHeaderNames()) {
            funnelbackHeaders.put(name, response.getHeaderValue(name).toString());
        }
        return funnelbackHeaders;
    }

    private Map<String, String> getTheDefaultHeadersSpringSecuritySets() throws Exception {
        ObjectPostProcessor mockObjectPostProcessor = Mockito.mock(ObjectPostProcessor.class);
        AuthenticationManagerBuilder authenticationManagerBuilder = new AuthenticationManagerBuilder(mockObjectPostProcessor);
        Map<Class<? extends Object>, Object> sharedObjects = new HashMap<>();

        HeadersConfigurer hc = new HeadersConfigurer<>();
        HttpSecurity httpSecurity = new HttpSecurity(mockObjectPostProcessor, authenticationManagerBuilder, sharedObjects);
        hc.configure(httpSecurity);

        Method performBuildMethod = HttpSecurity.class.getDeclaredMethod("performBuild");
        performBuildMethod.setAccessible(true);
        DefaultSecurityFilterChain filterChain = (DefaultSecurityFilterChain) performBuildMethod.invoke(httpSecurity);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        for (Filter f : filterChain.getFilters()) {
            f.doFilter(request, response, chain);
        }

        Map<String, String> springDefaultHeaders = new HashMap<>();
        for (String headerName : response.getHeaderNames()) {
            springDefaultHeaders.put(headerName, response.getHeaderValue(headerName).toString());
        }

        // I noticed that Strict-Transport-Security is missing from the default list.
        // https://docs.spring.io/spring-security/site/docs/current/reference/html/default-security-headers-2.html
        // notes that it's included only on https requests, which I guess is why.
        // We don't want it anyhow, since we don't force search to be available on https.
        return springDefaultHeaders;
    }
}