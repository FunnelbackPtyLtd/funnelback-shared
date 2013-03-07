package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.log4j.Log4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import com.funnelback.publicui.search.service.resource.impl.SimpleFileResource.RemoveCommentsPredicate;

/**
 * Reads a file and returns a Set of String containing
 * each unique line, skipping comments
 */
@Log4j
public class UniqueLinesResource extends AbstractSingleFileResource<Set<String>> {
    
    public UniqueLinesResource(File file) {
        super(file);
    }

    @Override
    public Set<String> parse() throws IOException {
        log.debug("Loading file '"+file.getAbsolutePath()+"'");
        @SuppressWarnings("unchecked")
        List<String> lines = FileUtils.readLines(file);                
        CollectionUtils.filter(lines, new RemoveCommentsPredicate());
        return new HashSet<String>(lines);
    }

    
}
