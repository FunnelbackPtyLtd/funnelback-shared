package com.funnelback.publicui.search.web.views.freemarker;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * Parses a range expression (used to build &lt;select&gt;)
 */
@Log4j
public class ParseRangeMethod extends AbstractTemplateMethod {

    public static final String NAME = "parseRange";
    
    public static final String START = "start";
    public static final String END = "end";
    
    private static final String CURRENT_YEAR = "CURRENT_YEAR";

    private static final Pattern RANGE_PATTERN = Pattern.compile("^\\s*(\\d+)\\s*\\.\\.\\s*(\\d+)\\s*$");
    private static final Pattern CURRENT_YEAR_OP_PATTERN = Pattern.compile(CURRENT_YEAR + "\\s*(\\+|\\-)\\s*(\\d+)");
    
    
    public ParseRangeMethod() {
        super(1, 0, false);
    }
    
    @Override
    public Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        HashMap<String, TemplateNumberModel> map = new HashMap<String, TemplateNumberModel>();
        String range = ((TemplateScalarModel) arguments.get(0)).getAsString();
    
        log.trace("Incoming range is '" + range + "'");
        
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Matcher m = CURRENT_YEAR_OP_PATTERN.matcher(range);
        StringBuffer buf = new StringBuffer();
        while (m.find()) {
            if ("+".equals(m.group(1))) {
                m.appendReplacement(buf, Integer.toString(year+Integer.parseInt(m.group(2))));
            } else if ("-".equals(m.group(1))) {
                m.appendReplacement(buf, Integer.toString(year-Integer.parseInt(m.group(2))));
            }
        }
        m.appendTail(buf);
        range = buf.toString();
        
        range = range.replace(CURRENT_YEAR, Integer.toString(year));
        
        log.trace("After " + CURRENT_YEAR + " processing: '" + range + "'");
        
        m = RANGE_PATTERN.matcher(range);
        if (m.find()) {    
            map.put(START, new SimpleNumber(Integer.parseInt(m.group(1))));
            map.put(END, new SimpleNumber(Integer.parseInt(m.group(2))));
        }
        return new SimpleHash(map);
    }

}
