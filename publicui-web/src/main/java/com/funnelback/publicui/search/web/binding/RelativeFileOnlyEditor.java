package com.funnelback.publicui.search.web.binding;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.util.regex.Pattern;

/**
 * <p>Editor to convert a String parameter into a relative File</p>
 * 
 * <p>This only does very limited blacklist checks, so make sure you
 * use a filesystem based test at some point to determine if a file is contained
 * within a specific folder, rather than relying only on this class.</p>
 * 
 */
public class RelativeFileOnlyEditor extends PropertyEditorSupport {

    private static final Pattern NO_PARENT_PATTERN = Pattern.compile("\\.\\."); 
    
    @Override
    public void setAsText(String text) {
            setValue(transformToFile(text));
    }
    
    /**
     * Returns a File, checking the path is relative
     * 
     * @param relativePath
     * @return
     * @throws IllegalArgumentException when the path is not relative
     */
    public static File transformToFile(String relativePath) throws IllegalArgumentException {
        if (NO_PARENT_PATTERN.matcher(relativePath).find()
            || new File(relativePath).isAbsolute()) {
            throw new IllegalArgumentException("Invalid path '"+relativePath+"'");
        } else {
            return new File(relativePath);
        }
    }
    
}
