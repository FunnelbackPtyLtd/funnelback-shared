package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import javax.annotation.PostConstruct;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

/**
 * <p>Formats a file size to display KB</p>  
 */
public class FilesizeFormatMethod extends AbstractTemplateMethod {

    public static final String NAME = "filesize";
    
    /**
     * This is unlikely to change, but still ...
     */
    private static final int ONE_KB = 1024;
    
    private String kbUnit = "k";

    public FilesizeFormatMethod() {
        super(1, 0, false);
    }
    
    @PostConstruct
    private void setI18nKbUnit() {
        kbUnit = i18n.tr("freemarker.method.FilesizeFormatMethod.kilobyte");
    }

    @Override
    protected Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        int size = ((TemplateNumberModel) arguments.get(0)).getAsNumber().intValue();
        
        if (size < ONE_KB) {
            return "1"+kbUnit;
        }
        
        return (size /= ONE_KB) + kbUnit;
    }

}
