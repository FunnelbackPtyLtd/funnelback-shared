package com.funnelback.publicui.search.web.filters;

import javax.servlet.Filter;

import com.funnelback.springmvc.web.filter.AcceptOriginCorsFilter;

/**
 * Filter to add a CORS allow origin header
 * 
 * @author nguillaumin@funnelback.com
 */
public class ConfigurableCorsFilter extends AcceptOriginCorsFilter implements Filter {

}
