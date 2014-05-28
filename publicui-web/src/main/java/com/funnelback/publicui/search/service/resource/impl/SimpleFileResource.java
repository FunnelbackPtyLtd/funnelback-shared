package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lombok.extern.log4j.Log4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import com.funnelback.common.config.RemoveCommentsPredicate;
import com.funnelback.springmvc.service.resource.impl.AbstractSingleFileResource;

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
        List<String> lines = FileUtils.readLines(file);                
        CollectionUtils.filter(lines, new RemoveCommentsPredicate());
        return lines.toArray(new String[0]);
    }


    
    
}
