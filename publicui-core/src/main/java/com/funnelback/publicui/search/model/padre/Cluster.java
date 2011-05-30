package com.funnelback.publicui.search.model.padre;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * Contextual navigation cluster (belongs to a {@link Category})
 */
public class Cluster {

	private static final Pattern QUERY_PATTERN = Pattern.compile("query=`([^`\"]+)`");

	public Cluster(String href, Integer count, String label) {
		setHref(href);
		this.count = count;
		this.label = label;
	}
	
	@Getter private String href;
	@Getter @Setter private Integer count;
	@Getter @Setter private String label;
	@Getter @Setter private String query;
	
	@SneakyThrows(UnsupportedEncodingException.class)
	public void setHref(String href) {
		this.href = href;
		
		// Extract the query from the query string parameters
		Matcher m = QUERY_PATTERN.matcher(URLDecoder.decode(href, "UTF-8"));
		if (m.find()) {
			query = m.group(1);
		}
	}
	
	
	public final static class Schema {
		public static final String CLUSTER = "cluster";
		
		public static final String HREF = "href";
		public static final String COUNT = "count";
	}
}
