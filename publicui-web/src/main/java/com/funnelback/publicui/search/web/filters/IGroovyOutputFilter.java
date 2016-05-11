package com.funnelback.publicui.search.web.filters;

import java.util.Optional;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public interface IGroovyOutputFilter {

    Optional<byte[]> filterResponse(ServletRequest request, byte[] bytes, ServletResponse wrappedResponse);

}
