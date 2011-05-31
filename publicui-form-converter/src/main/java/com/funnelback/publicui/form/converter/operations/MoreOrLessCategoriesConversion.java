package com.funnelback.publicui.form.converter.operations;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Log;

import org.apache.commons.io.IOUtils;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts <s:MoreOrLessCategories> tags.
 */
@Log
public class MoreOrLessCategoriesConversion implements Operation {

	private static final Pattern JQUERY_JS_PATTERN = Pattern.compile("src=['\"]?.*?jquery.*?\\.js", Pattern.CASE_INSENSITIVE);
	private static final Pattern MORE_LESS_TAG_PATTERN = Pattern.compile("<s:MoreOrLessCategories\\s*(class=['\"](.*?)['\"])?\\s*>");
	
	private final static String DEFAULT_LINK_CLASS_NAME = "moreOrLessCategories";
	private final static String DEFAULT_FACET_CLASS_NAME = "facet";
	private final static String DEFAULT_CATEGORY_CLASS_NAME = "category";
	
	private final static String DEFAULT_DISPLAYED_CATEGORIES = "8";
	
	@Override
	public String process(String in) {
		String out = in;
		
		Matcher m = MORE_LESS_TAG_PATTERN.matcher(out);
		if (m.find()) {
			log.info("Processing <s:MoreOrLessCategories> tags");
			
			if (! JQUERY_JS_PATTERN.matcher(out).find()) {
				log.warn("The converted form requires jQuery for faceted navigation 'more.../less...' categories links, "
						+ "but your form file doesn't seem to include the jQuery javascript library. Please ensure that "
						+ "the converted form file includes jQuery if you're using faceted navigation.");
			}
			
			// Try to find a custom class name for the link
			String linkClassName = DEFAULT_LINK_CLASS_NAME;
			if (m.group(2) != null) {
				linkClassName = m.group(2);
			}
	
			// Replace opening tag
			out = m.replaceAll("<@s.MoreOrLessCategories>");
			// Replace closing tag
			out = out.replace("s:MoreOrLessCategories", "@s.MoreOrLessCategories");

			// Try to find a custom class name for the facets
			String facetClassName = DEFAULT_FACET_CLASS_NAME;
			Matcher mFacet = Pattern.compile("<.*?Facet[^>]*?class=['\"](.*?)['\"].*?>").matcher(out);
			if (mFacet.find() && mFacet.group(1) != null) {
				facetClassName = mFacet.group(1);
			}

			// Try to find a custom class name for the categories
			String categoryClassName = DEFAULT_CATEGORY_CLASS_NAME;
			Matcher mCategory = Pattern.compile("<.*?Category[^>]*?class=['\"](.*?)['\"].*?>").matcher(out);
			if (mCategory.find() && mCategory.group(1) != null) {
				categoryClassName = mCategory.group(1);
			}
			
			// Try to find a custom number of displayed categories
			String displayedCategories = DEFAULT_DISPLAYED_CATEGORIES;
			Matcher mDisplayedCategories = Pattern.compile("<s:DefaultMaximumDisplayedCategoriesPerFacet>(.*?)</s:DefaultMaximumDisplayedCategoriesPerFacet>").matcher(out);
			if (mDisplayedCategories.find() && mDisplayedCategories.group(1) != null) {
				displayedCategories = mDisplayedCategories.group(1);
				// Remove tag
				out = mDisplayedCategories.replaceAll("");
			}
			
			// Insert Javascript snippet
			String jsSnippet = "";
			try {
				jsSnippet = IOUtils.toString(getClass().getResourceAsStream("/faceted-navigation.js"))
					.replace("{facetClassName}", facetClassName)
					.replace("{categoryClassName}", categoryClassName)
					.replace("{linkClassName}", linkClassName)
					.replace("{defaultDisplayedCategories}", displayedCategories);
			} catch (IOException ioe) {
				log.error("Unable to insert required Javascript snipped for faceted navigation 'more.../less...' categories links", ioe);
			}

			// Try to replace closing head tag
			Matcher mHead = Pattern.compile("</head>", Pattern.CASE_INSENSITIVE).matcher(out);
			if (mHead.find()) {
				out = mHead.replaceAll("\n"+jsSnippet+ "\n</head>");
			} else {
				log.warn("Unable to find closing </head> tag to insert Javascript snippet for faceted navigation 'more.../less...' categories links."
						+ " Please insert the snippet manually");
			}
		}
		
		// Try to deal with custom <s:AbsoluteMaximumDisplayedCategoriesPerFacet>
		m = Pattern.compile("<s:AbsoluteMaximumDisplayedCategoriesPerFacet>(.*?)</s:AbsoluteMaximumDisplayedCategoriesPerFacet>").matcher(out);
		if (m.find() && m.group(1) != null) {
			log.info("Processing <s:AbsoluteMaximumDisplayedCategoriesPerFacet> tag");
			String absoluteMaximum = m.group(1);

			// Remove tag
			out = m.replaceAll("");
			// Try to find previously converted <@s.Category> tags.
			Matcher mCategories = Pattern.compile("<@s.Category(\\s+.*?)?>").matcher(out);
			
			StringBuffer buf = new StringBuffer();
			while (mCategories.find()) {
				if (mCategories.group(1) == null || ! mCategories.group(1).contains("name=")) {
					// Plain <@s.Category> tag, add a "max=" parameter
					mCategories.appendReplacement(buf, mCategories.group(0).replace(">", " max="+absoluteMaximum + ">"));
				} else if (mCategories.group(1).contains("name=")) {
					// This is a contextual navigation category, ignore it
					mCategories.appendReplacement(buf, mCategories.group(0));
				}
			}
			mCategories.appendTail(buf);
			out = buf.toString();
		}
		
		
		return out;
	}

}
