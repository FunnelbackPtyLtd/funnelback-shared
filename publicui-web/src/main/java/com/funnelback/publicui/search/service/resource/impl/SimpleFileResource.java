package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;

/**
 * Parses a key=value config files and returns an array of
 * Strings for each line, skipping comments
 */
@Log4j
public class SimpleFileResource extends AbstractSingleFileResource<String[]> {
	
	public SimpleFileResource(File file) {
		super(file);
	}

	@Override
	public String[] parse() throws IOException {
		log.debug("Loading file '"+file.getAbsolutePath()+"'");
		@SuppressWarnings("unchecked")
		List<String> lines = FileUtils.readLines(file);				
		CollectionUtils.filter(lines, new RemoveCommentsPredicate());
		return lines.toArray(new String[0]);
	}

	/**
	 * Remove comments from config files
	 */
	public static class RemoveCommentsPredicate implements Predicate {
		/** A comment line in a config file starts with a hash */
		private static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*#.*");

		@Override
		public boolean evaluate(Object o) {
			String line = (String) o;
			return ! COMMENT_PATTERN.matcher(line).matches();
		}
	};
	
	
}
