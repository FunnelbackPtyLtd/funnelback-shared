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
        if (NO_PARENT_PATTERN.matcher(text).find()
            || new File(text).isAbsolute()) {
            throw new IllegalArgumentException("Invalid path '"+text+"'");
        } else {
            setValue(new File(text));
        }
    }
    
}
