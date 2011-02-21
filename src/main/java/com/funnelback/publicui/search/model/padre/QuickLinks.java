package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A quick link, associated to a result.
 */
@RequiredArgsConstructor
public class QuickLinks {

	/**
	 * Domain of the quick links. Note: This is not a domain only, but could be
	 * a partial URL such as: www.domain.com/folder1/folder2/
	 */
	@Getter
	private final String domain;

	/**
	 * List of quick links
	 */
	@Getter
	private final List<QuickLink> quickLinks = new ArrayList<QuickLink>();

	@RequiredArgsConstructor
	public static class QuickLink {
		@Getter
		private final String text;
		@Getter
		private final String url;
	}

	public static final class Schema {

		public static final String QUICKLINKS = "quicklinks";
		public static final String DOMAIN = "domain";
		
		public static final String QUICKLINK = "quicklink";		
		public static final String QLTEXT = "qltext";
		public static final String QLURL = "qlurl";	}
}
