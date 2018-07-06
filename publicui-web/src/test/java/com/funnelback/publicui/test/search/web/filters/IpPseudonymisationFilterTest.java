package com.funnelback.publicui.test.search.web.filters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.ServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.config.configtypes.service.ServiceConfigOption;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.filters.IpPseudonymisationFilter;

public class IpPseudonymisationFilterTest {

    @Test
    public void test() throws Exception {
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ServiceConfigReadOnly scro = mock(ServiceConfigReadOnly.class);
        when(scro.get((ServiceConfigOption<Boolean>)Mockito.anyObject())).thenReturn(true);
        
        ArgumentCaptor<String> collectionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> profileCaptor = ArgumentCaptor.forClass(String.class);
        
        when(configRepository.getServiceConfig(collectionCaptor.capture(), profileCaptor.capture())).thenReturn(scro);
        
        IpPseudonymisationFilter filter = new IpPseudonymisationFilter();
        filter.setConfigRepository(configRepository);
        
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("1.2.3.4");
        req.setRequestURI("http://example.com/s/search.html?collection=foo&profile=bar&query=goo");
        req.setParameter("collection", "foo");
        req.setParameter("profile", "bar");
        
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(req, new MockHttpServletResponse(), chain);
        
        ServletRequest wrappedRequest = chain.getRequest();
        
        Assert.assertEquals("1.2.3.0", wrappedRequest.getRemoteAddr());
        Assert.assertEquals("foo", collectionCaptor.getValue());
        Assert.assertEquals("bar", profileCaptor.getValue());
    }

    @Test
    public void testAbsentCollectionAndProfile() throws Exception {
        IpPseudonymisationFilter filter = new IpPseudonymisationFilter();
        
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("1.2.3.4");
        req.setRequestURI("http://example.com/s/search.html");
        
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(req, new MockHttpServletResponse(), chain);
        
        ServletRequest wrappedRequest = chain.getRequest();
        
        Assert.assertEquals("1.2.3.0", wrappedRequest.getRemoteAddr());
    }

    @Test
    public void testAbsentProfile() throws Exception {
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ServiceConfigReadOnly scro = mock(ServiceConfigReadOnly.class);
        when(scro.get((ServiceConfigOption<Boolean>)Mockito.anyObject())).thenReturn(true);
        
        ArgumentCaptor<String> collectionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> profileCaptor = ArgumentCaptor.forClass(String.class);
        
        when(configRepository.getServiceConfig(collectionCaptor.capture(), profileCaptor.capture())).thenReturn(scro);
        
        IpPseudonymisationFilter filter = new IpPseudonymisationFilter();
        filter.setConfigRepository(configRepository);
        
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("1.2.3.4");
        req.setRequestURI("http://example.com/s/search.html?collection=foo&query=goo");
        req.setParameter("collection", "foo");
        
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(req, new MockHttpServletResponse(), chain);
        
        ServletRequest wrappedRequest = chain.getRequest();
        
        Assert.assertEquals("1.2.3.0", wrappedRequest.getRemoteAddr());
        Assert.assertEquals("foo", collectionCaptor.getValue());
        Assert.assertEquals("_default", profileCaptor.getValue());
    }

}
