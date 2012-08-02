package com.funnelback.publicui.search.model.padre;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * <p>A contextual navigation cluster (belongs to a {@link Category}).</p>
 * 
 * <p>For example if the query is "King", a cluster could be "King Richard".
 * It's usually displayed "... Richard" on search results.</p>
 * 
 * @since 11.0
 * @see ContextualNavigation
 * @see Category
 * @see ClusterNav
 */
@NoArgsConstructor
public class Cluster {

	/**
	 * Pattern used to find the query for this cluster
	 * from the query string.
	 */
	private static final Pattern QUERY_PATTERN = Pattern.compile("query=`([^`\"]+)`");

	public Cluster(String href, Integer count, String label) {
		setHref(href);
		this.count = count;
		this.label = label;
	}
	
	/** Link to run a query using this cluster as query terms. */
	@Getter private String href;
	
	/** Number of results for this suggestion */
	@Getter @Setter private Integer count;
	
	/**
	 * Label of this suggestion
	 * (Ex: "... Richard" for the query "King").
	 */
	@Getter @Setter private String label;
	
	/**
	 * Query term for this suggestion
	 * (Ex: "King Richard" for the query "King").
	 */
	@Getter @Setter private String query;
	
	/**
	 * Sets the {@link #href} of this suggestion and
	 * extracts the query terms into {@link #query}.
	 * 
	 * @param href URL to use for this suggestion.
	 */
	@SneakyThrows(UnsupportedEncodingException.class)
	public void setHref(String href) {
		this.href = href;
		
		// Extract the query from the query string parameters
		if (href != null) {
			Matcher m = QUERY_PATTERN.matcher(URLDecoder.decode(href, "UTF-8"));
			if (m.find()) {
				query = m.group(1);
			}
	}
	}
	
	/** Constants for the PADRE XML result packet tags. */
	public final static class Schema {
		public static final String CLUSTER = "cluster";
		
		public static final String HREF = "href";
		public static final String COUNT = "count";
	}
}
